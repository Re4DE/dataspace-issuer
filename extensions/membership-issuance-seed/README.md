# MembershipCredential issuance seed Extension

This extension creates the membership attestation and credential definition.
With the membership attestation definition, the data issuer specifies the data a new participant needs to provide
during the onboarding process.
With the membership credential definition, a mapping between attested properties and credential properties is created.

## Settings

| Setting                                                     | Example Value           | Description                                                             |
|-------------------------------------------------------------|-------------------------|-------------------------------------------------------------------------|
| edc.issuer.issuance.membership.attestation.table.name       | membership_attestations | Table name used to extract the membership attestations                  |
| edc.issuer.issuance.membership.attestation.data.source.name | default                 | The name of the data source context                                     |
| edc.issuer.issuance.membership.attestation.id.column        | holder_id               | The column name used to identify the holder of the requested credential |