package de.fraunhofer.iee.issuer;

import org.eclipse.edc.boot.system.injection.ObjectFactory;
import org.eclipse.edc.identityhub.spi.authentication.ServicePrincipal;
import org.eclipse.edc.identityhub.spi.participantcontext.ParticipantContextService;
import org.eclipse.edc.identityhub.spi.participantcontext.model.CreateParticipantContextResponse;
import org.eclipse.edc.identityhub.spi.participantcontext.model.ParticipantContext;
import org.eclipse.edc.identityhub.spi.participantcontext.model.ParticipantManifest;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.Hostname;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(DependencyInjectionExtension.class)
class IssuerSeedExtensionTest {

    private static final String SUPER_USER_PARTICIPANT_ID = "super-user";
    private static final String API_TOKEN_ALIAS = "super-user-apikey";
    private static final String SUPER_USER_API_KEY = "c3VwZXItdXNlcg==.devpass";
    private static final String ISSUER_BASE_URL = "http://localhost:8080";
    private static final String ISSUER_DID = "did:web:localhost:8080";

    private final ParticipantContextService participantContextService = mock();
    private final Hostname hostname = mock();
    private final Vault vault = mock();
    private final Monitor monitor = mock();
    private final ParticipantContext participantContext = mock();

    private ServiceExtensionContext context;
    private ObjectFactory factory;


    @BeforeEach
    void setUp(ServiceExtensionContext context, ObjectFactory factory) {
        this.context = context;
        this.factory = factory;

        when(context.getMonitor()).thenReturn(monitor);
        context.registerService(ParticipantContextService.class, participantContextService);
        context.registerService(Hostname.class, hostname);
        context.registerService(Vault.class, vault);
    }

    @AfterEach
    void tearDown() {
    }

    @Nested
    class SkipSeeding {

        @Test
        void shouldSkip_whenSuperUserAndIssuerAlreadyExists() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.success(participantContext));
            when(participantContextService.getParticipantContext(ISSUER_DID))
                    .thenReturn(ServiceResult.success(participantContext));

            var extension = extension(factory, context, null, ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            verify(monitor).debug("Super User already created, skip seeding");
            verify(monitor).debug("Issuer already created, skip seeding");
            verify(participantContextService, never()).createParticipantContext(any());
            verifyNoInteractions(vault);
        }
    }

    @Nested
    class SeedSuperUser {

        @BeforeEach
        void setUp() {
            when(participantContextService.getParticipantContext(ISSUER_DID))
                    .thenReturn(ServiceResult.success(participantContext));
        }

        @Test
        void shouldCreateWithCorrectManifest_whenSuperUserDoesNotExist() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.notFound("not found"));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));

            var extension = extension(factory, context, null, ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            var captor = ArgumentCaptor.forClass(ParticipantManifest.class);
            verify(participantContextService).createParticipantContext(captor.capture());

            var manifest = captor.getValue();
            assertThat(manifest.getParticipantId()).isEqualTo(SUPER_USER_PARTICIPANT_ID);
            assertThat(manifest.getDid()).isEqualTo("did:web:%s".formatted(SUPER_USER_PARTICIPANT_ID));
            assertThat(manifest.isActive()).isTrue();
            assertThat(manifest.getKey().getKeyId()).isEqualTo("super-user#key-1");
            assertThat(manifest.getKey().getPrivateKeyAlias()).isEqualTo("super-user#key-1");
            assertThat(manifest.getKey().getKeyGeneratorParams()).containsEntry("algorithm", "EdDSA").containsEntry("curve", "Ed25519");
            assertThat(manifest.getRoles()).containsExactly(ServicePrincipal.ROLE_ADMIN);

            verify(monitor).info(contains("Super User key not provided. Generated:"));
            verifyNoInteractions(vault);
        }

        @Test
        void shouldStoreApiKeyInVault_whenSuperUserDoesNotExistAndApiKeyProvided() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.success(participantContext));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));
            when(vault.storeSecret(any(), any())).thenReturn(Result.success());
            when(participantContext.getApiTokenAlias()).thenReturn(API_TOKEN_ALIAS);

            var extension = extension(factory, context, SUPER_USER_API_KEY, ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            verify(monitor).debug("Super User key override successful");
            verify(vault).storeSecret(API_TOKEN_ALIAS, SUPER_USER_API_KEY);
        }
        
        @Test
        void shouldWarn_whenApiKeyHasInvalidFormat() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.success(participantContext));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));
            when(vault.storeSecret(any(), any())).thenReturn(Result.success());
            when(participantContext.getApiTokenAlias()).thenReturn(API_TOKEN_ALIAS);

            var extension = extension(factory, context, "invalid-api-key-format", ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            verify(monitor).severe("Super-user key override: this key appears to have an invalid format, you may be unable to access some APIs. It must follow the structure: 'base64(<participantId>).<random-string>'");
            verifyNoInteractions(vault);
        }

        @Test
        void shouldWarn_whenVaultFailsToStoreApiKey() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.success(participantContext));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));
            when(vault.storeSecret(any(), any())).thenReturn(Result.failure("vault error"));
            when(participantContext.getApiTokenAlias()).thenReturn(API_TOKEN_ALIAS);

            var extension = extension(factory, context, SUPER_USER_API_KEY, ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            verify(monitor).warning("Error storing API key in vault: vault error");
        }

        @Test
        void shouldWarn_whenParticipantContextRetrievalFailsAfterCreation() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.notFound("not found")).thenReturn(ServiceResult.badRequest("retrieval error"));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));

            var extension = extension(factory, context, SUPER_USER_API_KEY, ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            verify(monitor).warning("Error overriding API key for '%s': retrieval error".formatted(SUPER_USER_PARTICIPANT_ID));
            verifyNoInteractions(vault);
        }

        @Test
        void shouldThrow_whenParticipantContextCreationFails() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.notFound("not found"));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.badRequest("invalid manifest"));

            var extension = extension(factory, context, SUPER_USER_API_KEY, ISSUER_BASE_URL, ISSUER_DID);

            var exception = assertThrows(EdcException.class, extension::start);
            assertThat(exception.getMessage()).contains("Error creating Super-User: invalid manifest");
        }

    }

    @Nested
    class SeedIssuer {

        @BeforeEach
        void setUp() {
            when(participantContextService.getParticipantContext(SUPER_USER_PARTICIPANT_ID))
                    .thenReturn(ServiceResult.success(participantContext));
        }

        @Test
        void shouldCreateWithCorrectManifest_whenIssuerDoesNotExist() {
            when(participantContextService.getParticipantContext(ISSUER_DID))
                    .thenReturn(ServiceResult.notFound("not found"));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));
            when(context.getSetting("web.http.issuance.path", "/api/issuance"))
                    .thenReturn("/api/another-issuance");

            var extension = extension(factory, context, null, ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            var captor = ArgumentCaptor.forClass(ParticipantManifest.class);
            verify(participantContextService).createParticipantContext(captor.capture());

            var didBase64 = Base64.getEncoder().encodeToString(ISSUER_DID.getBytes());

            var manifest = captor.getValue();
            assertThat(manifest.getParticipantId()).isEqualTo(ISSUER_DID);
            assertThat(manifest.getDid()).isEqualTo(ISSUER_DID);
            assertThat(manifest.isActive()).isTrue();
            assertThat(manifest.getRoles()).containsExactly(ServicePrincipal.ROLE_ADMIN);
            assertThat(manifest.getKey().getKeyId()).isEqualTo("did:web:localhost:8080#key-1");
            assertThat(manifest.getKey().getPrivateKeyAlias()).isEqualTo("did:web:localhost:8080#key-1");
            assertThat(manifest.getKey().getKeyGeneratorParams()).containsEntry("algorithm", "EdDSA");

            manifest.getServiceEndpoints().forEach(endpoint -> {
                assertThat(endpoint.getId()).isEqualTo("issuer-service-1");
                assertThat(endpoint.getType()).isEqualTo("IssuerService");
                assertThat(endpoint.getServiceEndpoint())
                        .isEqualTo(ISSUER_BASE_URL + "/api/another-issuance/v1alpha/participants/" + didBase64);
            });

            verify(monitor).info(contains("Issuer created."));
            verifyNoInteractions(vault);
        }

        @Test
        void shouldCreateWithCorrectManifest_whenIssuerDoesNotExistAndDefaultIssuancePath() {
            when(participantContextService.getParticipantContext(ISSUER_DID))
                    .thenReturn(ServiceResult.notFound("not found"));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.success(new CreateParticipantContextResponse("generated-key", null, null)));

            var extension = extension(factory, context, null, ISSUER_BASE_URL, ISSUER_DID);
            extension.start();

            var captor = ArgumentCaptor.forClass(ParticipantManifest.class);
            verify(participantContextService).createParticipantContext(captor.capture());

            var didBase64 = Base64.getEncoder().encodeToString(ISSUER_DID.getBytes());

            var manifest = captor.getValue();
            assertThat(manifest.getParticipantId()).isEqualTo(ISSUER_DID);
            assertThat(manifest.getDid()).isEqualTo(ISSUER_DID);
            assertThat(manifest.isActive()).isTrue();
            assertThat(manifest.getRoles()).containsExactly(ServicePrincipal.ROLE_ADMIN);
            assertThat(manifest.getKey().getKeyId()).isEqualTo("did:web:localhost:8080#key-1");
            assertThat(manifest.getKey().getPrivateKeyAlias()).isEqualTo("did:web:localhost:8080#key-1");
            assertThat(manifest.getKey().getKeyGeneratorParams()).containsEntry("algorithm", "EdDSA");

            manifest.getServiceEndpoints().forEach(endpoint -> {
                assertThat(endpoint.getId()).isEqualTo("issuer-service-1");
                assertThat(endpoint.getType()).isEqualTo("IssuerService");
                assertThat(endpoint.getServiceEndpoint())
                        .isEqualTo(ISSUER_BASE_URL + "/api/issuance/v1alpha/participants/" + didBase64);
            });

            verify(monitor).info("Issuer created.");
            verifyNoInteractions(vault);
        }

        @Test
        void shouldThrow_whenParticipantContextCreationFails() {
            when(participantContextService.getParticipantContext(ISSUER_DID))
                    .thenReturn(ServiceResult.notFound("not found"));
            when(participantContextService.createParticipantContext(any()))
                    .thenReturn(ServiceResult.badRequest("invalid manifest"));

            var extension = extension(factory, context, null, ISSUER_BASE_URL, ISSUER_DID);

            var exception = assertThrows(EdcException.class, extension::start);
            assertThat(exception.getMessage()).contains("Error creating Issuer: invalid manifest");
        }
    }

    private IssuerSeedExtension extension(ObjectFactory factory, ServiceExtensionContext context, String apiKey, String issuerBaseUrl, String issuerDid) {
        Map<String, String> configMap = new HashMap<>();
        if (apiKey != null) {
            configMap.put("edc.issuer.api.superuser.key", apiKey);
        }
        configMap.put("edc.issuer.base.url", issuerBaseUrl);
        configMap.put("edc.issuer.did", issuerDid);
        when(context.getConfig()).thenReturn(ConfigFactory.fromMap(configMap));

        var extension = factory.constructInstance(IssuerSeedExtension.class);
        extension.initialize(context);
        return extension;
    }
}