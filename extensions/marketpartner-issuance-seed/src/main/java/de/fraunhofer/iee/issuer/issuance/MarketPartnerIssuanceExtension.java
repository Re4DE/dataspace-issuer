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

package de.fraunhofer.iee.issuer.issuance;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.CredentialFormat;
import org.eclipse.edc.issuerservice.spi.issuance.attestation.AttestationDefinitionService;
import org.eclipse.edc.issuerservice.spi.issuance.credentialdefinition.CredentialDefinitionService;
import org.eclipse.edc.issuerservice.spi.issuance.model.AttestationDefinition;
import org.eclipse.edc.issuerservice.spi.issuance.model.CredentialDefinition;
import org.eclipse.edc.issuerservice.spi.issuance.model.MappingDefinition;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Extension(value = "Creates attestations and credential descriptions for the MarketpartnerCredential")
public class MarketPartnerIssuanceExtension implements ServiceExtension {

    private static final String DEFAULT_PARTICIPANT_CONTEXT = "super-user";

    private static final String ATTESTATION_DEFAULT_ID = "db-marketpartner-attestation-def-1";
    private static final String ATTESTATION_DEFAULT_TYPE = "database";
    private static final String ATTESTATION_DEFAULT_TABLE_NAME = "marketpartner_attestations";
    private static final String ATTESTATION_DEFAULT_DATA_SOURCE_NAME = "default";
    private static final String ATTESTATION_DEFAULT_ID_COLUMN = "holder_id";

    private static final String ATTESTATION_CONFIG_TABLE_NAME = "tableName";
    private static final String ATTESTATION_CONFIG_DATA_SOURCE_NAME = "dataSourceName";
    private static final String ATTESTATION_CONFIG_ID_COLUMN = "idColumn";

    private static final String CREDENTIAL_DEFAULT_ID = "marketpartner-credential-def-1";
    private static final String CREDENTIAL_DEFAULT_TYPE = "MarketPartnerCredential";
    private static final String CREDENTIAL_DEFAULT_FORMAT = "VC1_0_JWT";
    private static final String CREDENTIAL_DEFAULT_JSON_SCHEMA = "{}";
    private static final String CREDENTIAL_DEFAULT_JSON_SCHEMA_URL = "";

    @Setting(key = "edc.issuer.issuance.marketpartner.attestation.table.name", description = "Table name used to extract the market partner attestations", required = false, defaultValue = ATTESTATION_DEFAULT_TABLE_NAME)
    private String tableName;

    @Setting(key = "edc.issuer.issuance.marketpartner.attestation.data.source.name", description = "The name of the data source context", required = false, defaultValue = ATTESTATION_DEFAULT_DATA_SOURCE_NAME)
    private String dataSourceName;

    @Setting(key = "edc.issuer.issuance.marketpartner.attestation.id.column", description = "The column name used to identify the holder of the requested credential", required = false, defaultValue = ATTESTATION_DEFAULT_ID_COLUMN)
    private String idColumn;

    @Inject
    private AttestationDefinitionService attestationDefinitionService;

    @Inject
    CredentialDefinitionService credentialDefinitionService;

    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.monitor = context.getMonitor();
    }

    @Override
    public void start() {
        // Seed the market partner attestation
        this.seedMarketPartnerAttestationDefinition();

        // Seed the market partner credential definition
        this.seedMarketPartnerCredentialDefinition();
    }

    private void seedMarketPartnerAttestationDefinition() {
        // If the market partner attestation definition is already present, skip
        if (this.attestationDefinitionService.getAttestationById(ATTESTATION_DEFAULT_ID).succeeded()) {
            this.monitor.info("Market partner attestation already seeded, skipped.");
            return;
        }

        var attestationDef = AttestationDefinition.Builder.newInstance()
                .id(ATTESTATION_DEFAULT_ID)
                .attestationType(ATTESTATION_DEFAULT_TYPE)
                .configuration(Map.of(
                        ATTESTATION_CONFIG_TABLE_NAME, this.tableName,
                        ATTESTATION_CONFIG_DATA_SOURCE_NAME, this.dataSourceName,
                        ATTESTATION_CONFIG_ID_COLUMN, this.idColumn
                ))
                .participantContextId(DEFAULT_PARTICIPANT_CONTEXT)
                .build();
        this.attestationDefinitionService.createAttestation(attestationDef)
                .onSuccess((v) -> this.monitor.info("Market partner attestation definition created."))
                .orElseThrow(f -> new EdcException("Error creating market partner attestation definition: " + f.getFailureDetail()));
    }

    private void seedMarketPartnerCredentialDefinition() {
        // If the market partner credential definition is already present, skip
        if (this.credentialDefinitionService.findCredentialDefinitionById(CREDENTIAL_DEFAULT_ID).succeeded()) {
            this.monitor.info("Credential definition already seeded, skipped.");
            return;
        }
        var credentialDef =  CredentialDefinition.Builder.newInstance()
                .id(CREDENTIAL_DEFAULT_ID)
                .credentialType(CREDENTIAL_DEFAULT_TYPE)
                .attestations(Set.of(ATTESTATION_DEFAULT_ID))
                .participantContextId(DEFAULT_PARTICIPANT_CONTEXT)
                .validity(60 * 60 * 24 * 365) // one year in seconds
                .rules(Set.of())
                .jsonSchema(CREDENTIAL_DEFAULT_JSON_SCHEMA)
                .jsonSchemaUrl(CREDENTIAL_DEFAULT_JSON_SCHEMA_URL)
                .formatFrom(CredentialFormat.valueOf(CREDENTIAL_DEFAULT_FORMAT.toUpperCase()))
                .mappings(this.buildCredentialMappings())
                .build();
        this.credentialDefinitionService.createCredentialDefinition(credentialDef)
                .onSuccess((v) -> this.monitor.info("Market partner credential definition created."))
                .orElseThrow(f -> new EdcException("Error creating market partner credential definition: " + f.getFailureDetail()));
    }

    private Set<MappingDefinition> buildCredentialMappings() {
        var mappings = new LinkedHashSet<MappingDefinition>();
        mappings.add(new MappingDefinition("company_name", "credentialSubject.companyName", true));
        mappings.add(new MappingDefinition("company_uid", "credentialSubject.companyUID", true));
        mappings.add(new MappingDefinition("sector", "credentialSubject.sector", true));
        mappings.add(new MappingDefinition("code_issuing_body", "credentialSubject.codeIssuingBody", true));
        mappings.add(new MappingDefinition("market_role_mp_id", "credentialSubject.marketRole.mpId", true));
        mappings.add(new MappingDefinition("market_role_name", "credentialSubject.marketRole.roleName", true));
        mappings.add(new MappingDefinition("market_role_abbrv", "credentialSubject.marketRole.roleAbbreviation", true));
        return mappings;
    }
}
