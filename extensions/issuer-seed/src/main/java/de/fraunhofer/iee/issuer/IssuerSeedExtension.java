/*
 *  Copyright (c) 2025 Fraunhofer Institute for Energy Economics and Energy System Technology (IEE)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer IEE - initial API and implementation
 *
 */

package de.fraunhofer.iee.issuer;

import org.eclipse.edc.iam.did.spi.document.Service;
import org.eclipse.edc.identityhub.spi.authentication.ServicePrincipal;
import org.eclipse.edc.identityhub.spi.participantcontext.ParticipantContextService;
import org.eclipse.edc.identityhub.spi.participantcontext.model.KeyDescriptor;
import org.eclipse.edc.identityhub.spi.participantcontext.model.ParticipantManifest;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Extension(value = "Super User Seed Extension")
public class IssuerSeedExtension implements ServiceExtension {
    public static final String DEFAULT_SUPER_USER_PARTICIPANT_ID = "super-user";

    @Setting(description = "Explicitly set the initial API key for the Super User, if empty autogenerate")
    public static final String SUPERUSER_APIKEY = "edc.issuer.api.superuser.key";

    @Setting(description = "The base url of the data space issuer")
    public static final String ISSUER_BASE_URL = "edc.issuer.base.url";

    @Setting(description = "The did of the data space issuer")
    public static final String ISSUER_DID = "edc.issuer.did";

    @Inject
    private ParticipantContextService participantContextService;

    @Inject
    private Hostname hostname;

    @Inject
    private Vault vault;

    private String superUserApiKey;
    private Monitor monitor;
    private String issuerDid;
    private String issuancePath;
    private String issuerBaseUrl;

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.superUserApiKey = context.getSetting(SUPERUSER_APIKEY, "");
        this.issuerBaseUrl = ofNullable(context.getSetting(ISSUER_BASE_URL, ""))
                .orElseThrow(() -> new EdcException("Missing value for " + ISSUER_BASE_URL));
        this.issuerDid = ofNullable(context.getSetting(ISSUER_DID, ""))
                .orElseThrow(() -> new EdcException("Missing value for " + ISSUER_DID));
        this.monitor = context.getMonitor();
        this.issuancePath = context.getSetting("web.http.issuance.path", "/api/issuance");
    }

    @Override
    public void start() {
        // Seed the Super User
        this.seedSuperUser();

        // Seed the Issuer
        this.seedIssuer();
    }

    private void seedSuperUser() {
        // Only create the Super User if not present
        if (this.participantContextService.getParticipantContext(DEFAULT_SUPER_USER_PARTICIPANT_ID).succeeded()) {
            this.monitor.debug("Super User already created, skip seeding");
            return;
        }

        // Create the Super User
        this.participantContextService.createParticipantContext(ParticipantManifest.Builder.newInstance()
                        .participantId(DEFAULT_SUPER_USER_PARTICIPANT_ID)
                        .did("did:web:%s".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID))
                        .active(true)
                        .key(KeyDescriptor.Builder.newInstance()
                                .keyGeneratorParams(Map.of("algorithm", "EdDSA", "curve", "Ed25519"))
                                .keyId("%s#key-1".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID))
                                .privateKeyAlias("%s#key-1".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID))
                                .build())
                        .roles(List.of(ServicePrincipal.ROLE_ADMIN))
                        .build())
                // If the creation was successful, we set the api or generate it if not present
                .onSuccess(generatedKey -> {
                    ofNullable(this.superUserApiKey)
                            .map(key -> {
                                if (!key.contains(".")) {
                                    this.monitor.warning("Super-user key override: this key appears to have an invalid format, you may be unable to access some APIs. It must follow the structure: 'base64(<participantId>).<random-string>'");
                                }
                                this.participantContextService.getParticipantContext(DEFAULT_SUPER_USER_PARTICIPANT_ID)
                                        .onSuccess(pc -> this.vault.storeSecret(pc.getApiTokenAlias(), key)
                                                .onSuccess(v -> this.monitor.debug("Super User key override successful"))
                                                .onFailure(f -> this.monitor.warning("Error storing API key in vault: %s".formatted(f.getFailureDetail()))))
                                        .onFailure(f -> this.monitor.warning("Error overriding API key for '%s': %s".formatted(DEFAULT_SUPER_USER_PARTICIPANT_ID, f.getFailureDetail())));
                                return key;
                            })
                            .orElseGet(() -> {
                                this.monitor.info("Super User key not provided. Generated: %s".formatted(generatedKey));
                                return generatedKey.apiKey();
                            });
                })
                // If there was anything wrong, throw an error
                .orElseThrow(f -> new EdcException("Error creating Super-User: " + f.getFailureDetail()));
    }

    private void seedIssuer() {
        var didBase64 = Base64.getEncoder().encodeToString(this.issuerDid.getBytes());

        // Only create the Issuer if not present
        if (this.participantContextService.getParticipantContext(this.issuerDid).succeeded()) {
            this.monitor.debug("Issuer already created, skip seeding");
            return;
        }

        // Create the issuer
        var endpointUrl = "%s%s/v1alpha/participants/%s".formatted(this.issuerBaseUrl, this.issuancePath, didBase64);
        var serviceEndpoint = new Service("issuer-service-1", "IssuerService", endpointUrl);

        this.participantContextService.createParticipantContext(ParticipantManifest.Builder.newInstance()
                        .participantId(this.issuerDid)
                        .did(this.issuerDid)
                        .active(true)
                        .roles(List.of(ServicePrincipal.ROLE_ADMIN))
                        .key(KeyDescriptor.Builder.newInstance()
                                .keyGeneratorParams(Map.of("algorithm", "EdDSA"))
                                .keyId("%s#key-1".formatted(this.issuerDid))
                                .privateKeyAlias("%s#key-1".formatted(this.issuerDid))
                                .build())
                        .serviceEndpoints(Set.of(serviceEndpoint))
                        .build())
                .onSuccess(generatedKey -> {
                    this.monitor.info("Issuer created.");
                })
                .orElseThrow(f -> new EdcException("Error creating Issuer: " + f.getFailureDetail()));

    }
}
