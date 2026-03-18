# MarketpartnerCredential issuance seed Extension

This extension creates the market partner attestation and credential definition.
With the market partner attestation definition, the data space issuer specifies the data a new participant needs to provide
during the onboarding process in case he is part of the regulated market communication.
With the market partner credential definition, a mapping between attested properties and credential properties is created.

## Settings

| Setting                                                        | Example Value              | Description                                                             |
|----------------------------------------------------------------|----------------------------|-------------------------------------------------------------------------|
| edc.issuer.issuance.marketpartner.attestation.table.name       | marketpartner_attestations | Table name used to extract the market partner attestations              |
| edc.issuer.issuance.marketpartner.attestation.data.source.name | default                    | The name of the data source context                                     |
| edc.issuer.issuance.marketpartner.attestation.id.column        | holder_id                  | The column name used to identify the holder of the requested credential |