package de.fraunhofer.iee.issuer.issuance;

import org.eclipse.edc.boot.system.injection.ObjectFactory;
import org.eclipse.edc.issuerservice.spi.issuance.attestation.AttestationDefinitionService;
import org.eclipse.edc.issuerservice.spi.issuance.credentialdefinition.CredentialDefinitionService;
import org.eclipse.edc.issuerservice.spi.issuance.model.AttestationDefinition;
import org.eclipse.edc.issuerservice.spi.issuance.model.CredentialDefinition;
import org.eclipse.edc.issuerservice.spi.issuance.model.MappingDefinition;
import org.eclipse.edc.junit.extensions.DependencyInjectionExtension;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(DependencyInjectionExtension.class)
class MembershipIssuanceExtensionTest {

    private static final String DEFAULT_PARTICIPANT_CONTEXT = "super-user";
    private static final String ATTESTATION_DEFAULT_ID = "db-membership-attestation-def-1";
    private static final String ATTESTATION_DEFAULT_TYPE = "database";
    private static final String ATTESTATION_DEFAULT_TABLE_NAME = "membership_attestations";
    private static final String ATTESTATION_DEFAULT_DATA_SOURCE_NAME = "default";
    private static final String ATTESTATION_DEFAULT_ID_COLUMN = "holder_id";
    private static final String ATTESTATION_CONFIG_TABLE_NAME = "tableName";
    private static final String ATTESTATION_CONFIG_DATA_SOURCE_NAME = "dataSourceName";
    private static final String ATTESTATION_CONFIG_ID_COLUMN = "idColumn";
    private static final String CREDENTIAL_DEFAULT_ID = "membership-credential-def-1";
    private static final String CREDENTIAL_DEFAULT_TYPE = "MembershipCredential";
    private static final String CREDENTIAL_DEFAULT_FORMAT = "VC1_0_JWT";
    private static final String CREDENTIAL_DEFAULT_JSON_SCHEMA = "{}";
    private static final String CREDENTIAL_DEFAULT_JSON_SCHEMA_URL = "";
    private static final String TABLE_NAME = "testTable";
    private static final String DATA_SOURCE_NAME = "testDataSource";
    private static final String ID_COLUMN = "testIdColumn";

    private final AttestationDefinitionService attestationDefinitionService = mock();
    private final CredentialDefinitionService credentialDefinitionService = mock();
    private final Monitor monitor = mock();
    private final AttestationDefinition attestationDefinition = mock();
    private final CredentialDefinition credentialDefinition = mock();

    private ServiceExtensionContext context;
    private ObjectFactory factory;

    @BeforeEach
    void setUp(ServiceExtensionContext context, ObjectFactory factory) {
        this.context = context;
        this.factory = factory;

        when(context.getMonitor()).thenReturn(monitor);
        context.registerService(AttestationDefinitionService.class, attestationDefinitionService);
        context.registerService(CredentialDefinitionService.class, credentialDefinitionService);
    }

    @AfterEach
    void tearDown() {
    }

    @Nested
    class SkipSeeding {

        @Test
        void shouldSkip_whenAttestationAndCredentialAlreadyExist() {
            when(attestationDefinitionService.getAttestationById(ATTESTATION_DEFAULT_ID)).thenReturn(ServiceResult.success(attestationDefinition));
            when(credentialDefinitionService.findCredentialDefinitionById(CREDENTIAL_DEFAULT_ID)).thenReturn(ServiceResult.success(credentialDefinition));

            var extension = extension(factory, context, null, null, null);
            extension.start();

            verify(monitor).info("Membership attestation already seeded, skipped.");
            verify(monitor).info("Credential definition already seeded, skipped.");
            verify(attestationDefinitionService, never()).createAttestation(any());
            verify(credentialDefinitionService, never()).createCredentialDefinition(any());
        }
    }

    @Nested
    class MarketPartnerAttestationSeeding {

        @BeforeEach
        void setUp() {
            when(credentialDefinitionService.findCredentialDefinitionById(CREDENTIAL_DEFAULT_ID)).thenReturn(ServiceResult.success(credentialDefinition));
        }

        @Test
        void shouldCreateCorrectAttestation_whenAttestationDoesNotExist() {
            when(attestationDefinitionService.getAttestationById(ATTESTATION_DEFAULT_ID)).thenReturn(ServiceResult.notFound("not found"));
            when(attestationDefinitionService.createAttestation(any())).thenReturn(ServiceResult.success());

            var extension = extension(factory, context, TABLE_NAME, DATA_SOURCE_NAME, ID_COLUMN);
            extension.start();

            var captor = ArgumentCaptor.forClass(AttestationDefinition.class);
            verify(attestationDefinitionService).createAttestation(captor.capture());

            var createdAttestation = captor.getValue();
            assertThat(createdAttestation.getId()).isEqualTo(ATTESTATION_DEFAULT_ID);
            assertThat(createdAttestation.getAttestationType()).isEqualTo(ATTESTATION_DEFAULT_TYPE);
            assertThat(createdAttestation.getConfiguration().get(ATTESTATION_CONFIG_TABLE_NAME)).isEqualTo(TABLE_NAME);
            assertThat(createdAttestation.getConfiguration().get(ATTESTATION_CONFIG_DATA_SOURCE_NAME)).isEqualTo(DATA_SOURCE_NAME);
            assertThat(createdAttestation.getConfiguration().get(ATTESTATION_CONFIG_ID_COLUMN)).isEqualTo(ID_COLUMN);
            assertThat(createdAttestation.getParticipantContextId()).isEqualTo(DEFAULT_PARTICIPANT_CONTEXT);

            verify(monitor).info("Membership attestation definition created.");
        }

        @Test
        void shouldCreateCorrectAttestation_whenAttestationDoesNotExistAndNoConfigProvided() {
            when(attestationDefinitionService.getAttestationById(ATTESTATION_DEFAULT_ID)).thenReturn(ServiceResult.notFound("not found"));
            when(attestationDefinitionService.createAttestation(any())).thenReturn(ServiceResult.success());

            var extension = extension(factory, context, null, null, null);
            extension.start();

            var captor = ArgumentCaptor.forClass(AttestationDefinition.class);
            verify(attestationDefinitionService).createAttestation(captor.capture());

            var createdAttestation = captor.getValue();
            assertThat(createdAttestation.getId()).isEqualTo(ATTESTATION_DEFAULT_ID);
            assertThat(createdAttestation.getAttestationType()).isEqualTo(ATTESTATION_DEFAULT_TYPE);
            assertThat(createdAttestation.getConfiguration().get(ATTESTATION_CONFIG_TABLE_NAME)).isEqualTo(ATTESTATION_DEFAULT_TABLE_NAME);
            assertThat(createdAttestation.getConfiguration().get(ATTESTATION_CONFIG_DATA_SOURCE_NAME)).isEqualTo(ATTESTATION_DEFAULT_DATA_SOURCE_NAME);
            assertThat(createdAttestation.getConfiguration().get(ATTESTATION_CONFIG_ID_COLUMN)).isEqualTo(ATTESTATION_DEFAULT_ID_COLUMN);
            assertThat(createdAttestation.getParticipantContextId()).isEqualTo(DEFAULT_PARTICIPANT_CONTEXT);

            verify(monitor).info("Membership attestation definition created.");
        }

        @Test
        void shouldThrow_whenAttestationCreationFails() {
            when(attestationDefinitionService.getAttestationById(ATTESTATION_DEFAULT_ID)).thenReturn(ServiceResult.notFound("not found"));
            when(attestationDefinitionService.createAttestation(any())).thenReturn(ServiceResult.badRequest("creation failed"));

            var extension = extension(factory, context, TABLE_NAME, DATA_SOURCE_NAME, ID_COLUMN);

            var exception = assertThrows(EdcException.class, extension::start);
            assertThat(exception.getMessage()).contains("Error creating membership attestation definition: creation failed");
        }
    }

    @Nested
    class MarketPartnerCredentialSeeding {

        @BeforeEach
        void setUp() {
            when(attestationDefinitionService.getAttestationById(ATTESTATION_DEFAULT_ID)).thenReturn(ServiceResult.success(attestationDefinition));
        }

        @Test
        void shouldCreateCorrectCredential_whenCredentialDoesNotExist() {
            when(credentialDefinitionService.findCredentialDefinitionById(CREDENTIAL_DEFAULT_ID)).thenReturn(ServiceResult.notFound("not found"));
            when(credentialDefinitionService.createCredentialDefinition(any())).thenReturn(ServiceResult.success());

            var extension = extension(factory, context, null, null, null);
            extension.start();

            var captor = ArgumentCaptor.forClass(CredentialDefinition.class);
            verify(credentialDefinitionService).createCredentialDefinition(captor.capture());

            var createdCredential = captor.getValue();
            assertThat(createdCredential.getId()).isEqualTo(CREDENTIAL_DEFAULT_ID);
            assertThat(createdCredential.getCredentialType()).isEqualTo(CREDENTIAL_DEFAULT_TYPE);
            assertThat(createdCredential.getAttestations()).containsExactly(ATTESTATION_DEFAULT_ID);
            assertThat(createdCredential.getParticipantContextId()).isEqualTo(DEFAULT_PARTICIPANT_CONTEXT);
            assertThat(createdCredential.getValidity()).isEqualTo(60 * 60 * 24 * 365);
            assertThat(createdCredential.getRules()).isEmpty();
            assertThat(createdCredential.getJsonSchema()).isEqualTo(CREDENTIAL_DEFAULT_JSON_SCHEMA);
            assertThat(createdCredential.getJsonSchemaUrl()).isEqualTo(CREDENTIAL_DEFAULT_JSON_SCHEMA_URL);
            assertThat(createdCredential.getFormat()).isEqualTo(CREDENTIAL_DEFAULT_FORMAT);

            var mappings = createdCredential.getMappings();
            assertThat(mappings).isNotEmpty();
            assertThat(isCredentialMappingPresent(mappings)).isTrue();
            verify(monitor).info("Membership credential definition created.");
        }

        @Test
        void shouldThrow_whenCredentialCreationFails() {
            when(credentialDefinitionService.findCredentialDefinitionById(CREDENTIAL_DEFAULT_ID)).thenReturn(ServiceResult.notFound("not found"));
            when(credentialDefinitionService.createCredentialDefinition(any())).thenReturn(ServiceResult.badRequest("creation failed"));

            var extension = extension(factory, context, null, null, null);

            var exception = assertThrows(EdcException.class, extension::start);
            assertThat(exception.getMessage()).contains("Error creating membership credential definition: creation failed");
        }
    }

    private boolean isCredentialMappingPresent(List<MappingDefinition> mappings) {
        return mappings.stream()
                .anyMatch(mapping -> mapping.input().equals("holder_id") && mapping.output().equals("credentialSubject.holderIdentifier"));

    }

    private MembershipIssuanceExtension extension(ObjectFactory factory, ServiceExtensionContext context, String tableName, String dataSourceName, String idColumn) {
        Map<String, String> configMap = new HashMap<>();
        if (tableName != null) {
            configMap.put("edc.issuer.issuance.membership.attestation.table.name", tableName);
        }
        if (dataSourceName != null) {
            configMap.put("edc.issuer.issuance.membership.attestation.data.source.name", dataSourceName);
        }
        if (idColumn != null) {
            configMap.put("edc.issuer.issuance.membership.attestation.id.column", idColumn);
        }
        when(context.getConfig()).thenReturn(ConfigFactory.fromMap(configMap));

        var extension = factory.constructInstance(MembershipIssuanceExtension.class);
        extension.initialize(context);
        return extension;
    }
}