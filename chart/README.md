# Dataspace Issuer Helm Chart

A Helm Chart for the Dataspace Issuer. The Issuer deployment consists
of the issuer, a `PostgreSQL` database and a `HashiCorp Vault`.

It will be deployed:

- Issuer (IdentityHub with Issuance dependencies)
- `PostgreSQL`
- `HashiCorp Vault`

## Configuration

The configuration of the `PostgreSQL` specific fields can be found in the CloudPirates Helm Chart [Documentation](https://github.com/CloudPirates-io/helm-charts/tree/postgres-0.18.3/charts/postgres).
The `HashiCorp Vault` deployment is specifically adjusted for this scenario as it allows combination of 
a persistent backend with the developer server. This needs to be considered when the Issuer is deployed productively.

| Key                                          | Default                        | Description                                                                   | Override |
|----------------------------------------------|--------------------------------|-------------------------------------------------------------------------------|----------|
| install.postgres                             | true                           | Toggle the deployment of a `PostgreSQL`                                       |          |
| install.vault                                | true                           | Toggle the deployment of a `HashiCorp Vault`                                  |          |
|                                              |                                |                                                                               |          |
| participantId                                | did:web:dataspace:issuer       | The id of this issuer, represented as DID:WEB                                 | X        |
| namespaceOverride                            | ""                             | Override the namespace                                                        |          |
|                                              |                                |                                                                               |          |
| issuer.image.repository                      | ghcr.io/re4de/dataspace-issuer | Repository URL of the Docker Image                                            |          |
| issuer.image.tag                             | 1.1.0-edc0.14.0                | Tag of the Docker Image                                                       |          |
| issuer.scheduling.label                      | null                           | Configure where the issuer should be schedule, e.g. `node: ssd`               |          | 
| issuer.endpoints.default.path                | /api                           | Default api path                                                              |          |
| issuer.endpoints.default.port                | 8080                           | Default api port                                                              |          |
| issuer.endpoints.default.public              | false                          | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.identity.path               | /api/identity                  | Identity api path                                                             |          |
| issuer.endpoints.identity.port               | 8082                           | Identity api port                                                             |          |
| issuer.endpoints.identity.public             | false                          | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.sts.path                    | /api/sts                       | STS token api path                                                            |          |
| issuer.endpoints.sts.port                    | 8083                           | STS token api port                                                            |          |
| issuer.endpoints.sts.public                  | false                          | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.version.path                | /api/version                   | Version api path                                                              |          |
| issuer.endpoints.version.port                | 8084                           | Version api port                                                              |          |
| issuer.endpoints.version.public              | true                           | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.did.path                    | /                              | DID api path                                                                  |          |
| issuer.endpoints.did.port                    | 8085                           | DID api port                                                                  |          |
| issuer.endpoints.did.public                  | true                           | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.issuance.path               | /api/issuance                  | VC issuance path                                                              |          |
| issuer.endpoints.issuance.port               | 8086                           | VC issuance port                                                              |          |
| issuer.endpoints.issuance.public             | true                           | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.admin.path                  | /api/admin                     | Admin path                                                                    |          |
| issuer.endpoints.admin.port                  | 8087                           | Admin port                                                                    |          |
| issuer.endpoints.admin.public                | false                          | Toggle to create a Ingress Rule                                               |          |
| issuer.endpoints.statuslist.path             | /api/statuslist                | Status list path                                                              |          |
| issuer.endpoints.statuslist.port             | 8090                           | Status list port                                                              |          |
| issuer.endpoints.statuslist.public           | true                           | Toggle to create a Ingress Rule                                               |          |
| issuer.service.enabled                       | true                           | Toggle to create a service                                                    |          |
| issuer.ingress.enabled                       | true                           | Toggle to create the ingress rules                                            |          |
| issuer.ingress.host                          | changeme                       | DNS for the issuer                                                            | X        |
| issuer.ingress.tls.enabled                   | false                          | Enables TLS on the ingress rule                                               |          |
| issuer.ingress.tls.issuer                    | cert-issuer                    | The name of the cluster issuer used by the cert-manager to issue Certificates |          | 
| issuer.startupProbe.enabled                  | true                           | Toggle to active startup probes                                               |          |
| issuer.startupProbe.initialDelaySeconds      | 1                              | Time to wait before first probe                                               |          |
| issuer.startupProbe.periodSeconds            | 3                              | Time to wait to wait until the next probe                                     |          |
| issuer.readinessProbe.enabled                | true                           | Toggle to active readiness probes                                             |          |
| issuer.readinessProbe.initialDelaySeconds    | 1                              | Time to wait before first probe                                               |          |
| issuer.readinessProbe.periodSeconds          | 5                              | Time to wait to wait until the next probe                                     |          |
| issuer.livenessProbe.enabled                 | true                           | Toggle to active liveness probes                                              |          |
| issuer.livenessProbe.initialDelaySeconds     | 3                              | Time to wait before first probe                                               |          |
| issuer.livenessProbe.periodSeconds           | 5                              | Time to wait to wait until the next probe                                     |          |
| issuer.edc.core.retriesMax                   | 5                              | Maximum retries before a failure is propagated                                |          |
| issuer.edc.core.backoffMin                   | 500                            | Minimum number of milliseconds for exponential backoff                        |          |
| issuer.edc.core.backoffMax                   | 10000                          | Maximum number of milliseconds for exponential backoff                        |          |
| issuer.edc.core.logOnRetry                   | false                          | Log onRetry events                                                            |          |
| issuer.edc.core.logOnRetryScheduled          | false                          | Log onRetryScheduled events                                                   |          |
| issuer.edc.core.logOnRetriesExceeded         | false                          | Log onRetriesExceeded events                                                  |          |
| issuer.edc.core.logOnRetryFailedAttempt      | false                          | Log onFailedAttempt events                                                    |          |
| issuer.edc.core.logOnRetryAbort              | false                          | Log onAbort events                                                            |          |
| issuer.edc.http.clientHttpsEnforce           | false                          | Enforce that the connector can only call https endpoints                      |          |
| issuer.edc.http.clientTimeoutConnect         | 30                             | Connect timeout of the connector to other endpoints in seconds                |          | 
| issuer.edc.http.clientTimeoutRead            | 30                             | Read timeout of the connector to other endpoints in seconds                   |          | 
| issuer.edc.http.clientSendBuffer             | 0                              | Size of buffer of sent data, if 0 no buffer used                              |          |
| issuer.edc.http.clientReceiveBuffer          | 0                              | Size of buffer of received data, if 0 no buffer used                          |          |
| issuer.edc.vault.healthCheck                 | true                           | Whether or not the `HasiCorp Vault` health check is enabled                   |          |
| issuer.edc.vault.healthCheckStandBy          | false                          | Standby should still return the active status code instead of the standby     |          |
| issuer.edc.vault.healthCheckPath             | /v1/sys/health                 | The URL path of the `HasiCorp Vault's` /health endpoint                       |          |
| issuer.edc.vault.tokenScheduledRenew         | true                           | Whether the automatic token renewal process will be triggered or not          |          |
| issuer.edc.vault.tokenTTL                    | 300                            | The time-to-live (ttl) value of the `HasiCorp Vault` token in seconds         |          |
| issuer.edc.vault.tokenRenewBuffer            | 30                             | The renew buffer of the `HasiCorp Vault` token in seconds                     |          |
| issuer.edc.vault.secretPath                  | /v1/secret                     | The URL path of the vault's /secret endpoint                                  |          |
| issuer.edc.sql.fetchSize                     | 5000                           | Fetch size value used in SQL queries                                          |          |
| issuer.edc.issuer.statuslist.signingKeyAlias | statuslist-signing-key         | Key alias of the statuslist signing key in the `HasiCorp Vault`               |          | 
| issuer.edc.issuer.iam.didWebHttps            | true                           | Switch between https and http for did providing, should be always true        |          |
| issuer.edc.issuer.seed.superUserKey          | c3VwZXItdXNlcg==.changeme      | The super user api key in form of base64(<participantId>).<random-string>     | X        |
|                                              |                                |                                                                               |          |
| vault.image.repository                       | hashicorp/vault                | Repository URL of the Docker Image                                            |          |                      
| vault.image.tag                              | 1.18.1                         | Tag of the Docker Image                                                       |          |
| vault.dev.devRootToken                       | changeme                       | The `HashiCorp Vault` access token                                            | X        |
| vault.persistence.size                       | 8Gi                            | Size of the volume                                                            |          |
| vault.persistence.volumeName                 | vault-vol                      | Name of the volume                                                            |          |
| vault.persistence.storageClass               | changeme                       | The name of the storage class                                                 | X        |
|                                              |                                |                                                                               |          |
| postgres.persistence.storageClass            | changeme                       | The name of the storage class                                                 | X        |