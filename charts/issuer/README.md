# Data Space Issuer Helm Chart

A Helm Chart for the Data Space Issuer. The Issuer deployment consists
of the issuer, a Postgresql database and a Vault.

It will be deployed:

- Issuer (IdentityHub with Issuance dependencies)
- Postgresql
- Vault

## Configuration

If a local deployment is needed, create a `overwrite.yaml` file and
overwrite the marked keys. The configuration of the `postgresql` specific
fields can be found in the bitnami Helm Chart [Documentation](https://github.com/bitnami/charts/tree/postgresql/16.4.3/bitnami/postgresql).
The Vault deployment is specifically adjusted for this scenario as it allows combination of 
a persistent backend with the developer server.

| Key                                            | Default                                | Description                                                                   | Override |
|------------------------------------------------|----------------------------------------|-------------------------------------------------------------------------------|----------|
| install.postgresql                             | true                                   | Toggle the deployment of a Postgresql                                         |          |
|                                                |                                        |                                                                               |          |
| global.participantId                           | did:web:dataspace:issuer               | The id of this issuer, represented as DID:WEB                                 | x        |
| global.defaultStorageClass                     | harvester                              | Used storage class to create PVCs                                             | x        |
| global.pullSecret                              | gitlab                                 | Name of the Docker Registry credential secret                                 |          |
|                                                |                                        |                                                                               |          |
| namespaceOverride                              | ""                                     | Override the namespace                                                        |          |
|                                                |                                        |                                                                               |          |
| issuer.image.repository                        | .../.../issuer                         | Repository URL of the Docker Image                                            |          |
| issuer.image.tag                               | 0.8.0                                  | Tag of the Docker Image                                                       |          |
| issuer.scheduling.label                        | null                                   | Configure where the issuer should be schedule, e.g. `node: ssd`               |          | 
| issuer.endpoints.default.path                  | /api                                   | Default api path                                                              |          |
| issuer.endpoints.default.port                  | 8080                                   | Default api port                                                              |          |
| issuer.endpoints.default.public                | false                                  | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.identity.path                 | /api/identity                          | Identity api path                                                             |          |
| issuer.endpoints.identity.port                 | 8082                                   | Identity api port                                                             |          |
| issuer.endpoints.identity.public               | false                                  | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.sts.path                      | /api/sts                               | STS token api path                                                            |          |
| issuer.endpoints.sts.port                      | 8083                                   | STS token api port                                                            |          |
| issuer.endpoints.sts.public                    | false                                  | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.version.path                  | /api/version                           | Version api path                                                              |          |
| issuer.endpoints.version.port                  | 8084                                   | Version api port                                                              |          |
| issuer.endpoints.version.public                | true                                   | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.did.path                      | /                                      | DID api path                                                                  |          |
| issuer.endpoints.did.port                      | 8085                                   | DID api port                                                                  |          |
| issuer.endpoints.did.public                    | true                                   | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.issuance.path                 | /api/issuance                          | VC issuance catalog path                                                      |          |
| issuer.endpoints.issuance.port                 | 8086                                   | VC issuance catalog port                                                      |          |
| issuer.endpoints.issuance.public               | true                                   | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.admin.path                    | /api/admin                             | Admin catalog path                                                            |          |
| issuer.endpoints.admin.port                    | 8087                                   | Admin catalog port                                                            |          |
| issuer.endpoints.admin.public                  | false                                  | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.statuslist.path               | /api/statuslist                        | Status list path                                                              |          |
| issuer.endpoints.statuslist.port               | 8090                                   | Status list port                                                              |          |
| issuer.endpoints.statuslist.public             | true                                   | Toggle to create a Ingress Rule                                               |          |
| issuer.service.enabled                         | true                                   | Toggle to create a service                                                    |          |
| issuer.ingress.enabled                         | true                                   | Toggle to create the ingress rules                                            |          |
| issuer.ingress.host                            | issuer-dena.services.iee.fraunhofer.de | DNS for the issuer                                                            | x        |
| issuer.ingress.tls.enabled                     | false                                  | Enables TLS on the ingress rule                                               |          |
| issuer.ingress.tls.issuer                      | cert-issuer                            | The name of the cluster issuer used by the cert-manager to issue Certificates |          | 
| issuer.startupProbe.enabled                    | true                                   | Toggle to active startup probes                                               |          |
| issuer.startupProbe.initialDelaySeconds        | 1                                      | Time to wait before first probe                                               |          |
| issuer.startupProbe.periodSeconds              | 3                                      | Time to wait to wait until the next probe                                     |          |
| issuer.readinessProbe.enabled                  | true                                   | Toggle to active readiness probes                                             |          |
| issuer.readinessProbe.initialDelaySeconds      | 1                                      | Time to wait before first probe                                               |          |
| issuer.readinessProbe.periodSeconds            | 5                                      | Time to wait to wait until the next probe                                     |          |
| issuer.livenessProbe.enabled                   | true                                   | Toggle to active liveness probes                                              |          |
| issuer.livenessProbe.initialDelaySeconds       | 3                                      | Time to wait before first probe                                               |          |
| issuer.livenessProbe.periodSeconds             | 5                                      | Time to wait to wait until the next probe                                     |          |
| issuer.edc.core.retriesMax                     | 5                                      | Maximum retries before a failure is propagated                                |          |
| issuer.edc.core.backoffMin                     | 500                                    | Minimum number of milliseconds for exponential backoff                        |          |
| issuer.edc.core.backoffMax                     | 10000                                  | Maximum number of milliseconds for exponential backoff                        |          |
| issuer.edc.core.logOnRetry                     | false                                  | Log onRetry events                                                            |          |
| issuer.edc.core.logOnRetryScheduled            | false                                  | Log onRetryScheduled events                                                   |          |
| issuer.edc.core.logOnRetriesExceeded           | false                                  | Log onRetriesExceeded events                                                  |          |
| issuer.edc.core.logOnRetryFailedAttempt        | false                                  | Log onFailedAttempt events                                                    |          |
| issuer.edc.core.logOnRetryAbort                | false                                  | Log onAbort events                                                            |          |
| issuer.edc.http.clientHttpsEnforce             | false                                  | Enforce that the connector can only call https endpoints                      |          |
| issuer.edc.http.clientTimeoutConnect           | 30                                     | Connect timeout of the connector to other endpoints in seconds                |          | 
| issuer.edc.http.clientTimeoutRead              | 30                                     | Read timeout of the connector to other endpoints in seconds                   |          | 
| issuer.edc.http.clientSendBuffer               | 0                                      | Size of buffer of sent data, if 0 no buffer used                              |          |
| issuer.edc.http.clientReceiveBuffer            | 0                                      | Size of buffer of received data, if 0 no buffer used                          |          |
| issuer.edc.vault.healthCheck                   | true                                   | Whether or not the vault health check is enabled                              |          |
| issuer.edc.vault.healthCheckStandBy            | false                                  | Standby should still return the active status code instead of the standby     |          |
| issuer.edc.vault.healthCheckPath               | /v1/sys/health                         | The URL path of the vault's /health endpoint                                  |          |
| issuer.edc.vault.tokenScheduledRenew           | true                                   | Whether the automatic token renewal process will be triggered or not          |          |
| issuer.edc.vault.tokenTTL                      | 300                                    | The time-to-live (ttl) value of the Hashicorp Vault token in seconds          |          |
| issuer.edc.vault.tokenRenewBuffer              | 30                                     | The renew buffer of the Hashicorp Vault token in seconds                      |          |
| issuer.edc.vault.secretPath                    | /v1/secret                             | The URL path of the vault's /secret endpoint                                  |          |
| issuer.edc.sql.fetchSize                       | 5000                                   | Fetch size value used in SQL queries                                          |          |
| issuer.edc.issuer.statuslist.signingKeyAlias   | statuslist-signing-key                 | The url where to find all participants that are available in the data space   |          | 
| issuer.edc.issuer.iam.didWebHttps              | true                                   | Switch between https and http for did providing, should be always true        |          |
| issuer.edc.issuer.seed.superUserKey            | c3VwZXItdXNlcg==.changeme              | The super user api key in form of base64(<participantId>).<random-string>     | x        |