# Dataspace Issuer

[![license](https://img.shields.io/github/license/eclipse-edc/Connector?style=flat-square&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)

---

This Dataspace Issuer is based on the components from the [EDC Identity Hub](https://github.com/eclipse-edc/IdentityHub) in the version 0.14.0.
The  Dataspace Issuer issues `MembershipCredentials` for onboarded participants.
The Dataspace Issuer need a running instance of a `PostgreSQL` database and a `HashiCorp Vault`.

## Versioning

We use semantic versioning and add the Eclipse Dataspace Components (EDC) version as a label to indicate compatibility.
For example, version `1.0.0-edc0.14.0` means that version `1.0.0` of the Dataspace Issuer is compatible with all EDCs of version `0.14.0`.
If possible, we provide backports of fixes that affect older EDC versions as well.
To get the latest build of the Dataspace Issuer, use the version `SNAPSHOT`.

## Configuration

The configuration parameters in the following table are the minimum fields needed to set up a local startup.
Many more configuration parameters are inherent from the EDCs, compare [EDC](https://github.com/eclipse-edc/IdentityHub).

| Name                                    | Example Value                        | Description                                                                                                                         |
|-----------------------------------------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| edc.participant.id                      | did:web:localhost%3A20085:issuer     | The ID of this issuer, represented as DID:WEB                                                                                       |
| edc.component.id                        | issuer                               | The ID of this runtime component                                                                                                    |
| edc.hostname                            | localhost                            | Hostname of the Issuer                                                                                                              |
| edc.iam.did.web.use.https               | false                                | Switch between https and http for did providing, only `false` for local development                                                 |
| edc.issuer.api.superuser.key            | c3VwZXItdXNlcg==.devpass             | The api key of the super user in the form of 'base64(<participantId>).<random-string>'. If not present a random string will be used | 
| edc.issuer.did                          | did:web:localhost%3A20085:issuer     | The did of the data space issuer                                                                                                    |
| edc.issuer.base.url                     | http://localhost:20086               | The base url of the data space issuer                                                                                               |
| edc.issuer.statuslist.signing.key.alias | statuslist-signing-key               | Key alias of the statuslist signing key in the `HasiCorp Vault`                                                                     |
| web.http.path                           | /api                                 | Default api path                                                                                                                    |
| web.http.port                           | 20080                                | Default api port                                                                                                                    |
| web.http.identity.path                  | /api/identity                        | Identity api path                                                                                                                   |
| web.http.identity.port                  | 20082                                | Identity api port                                                                                                                   |
| web.http.sts.path                       | /api/sts                             | STS token api path                                                                                                                  |
| web.http.sts.port                       | 20083                                | STS token api port                                                                                                                  |
| web.http.version.path                   | /api/version                         | Version api path                                                                                                                    |
| web.http.version.port                   | 20084                                | Version api port                                                                                                                    |
| web.http.did.path                       | /                                    | DID api path                                                                                                                        |
| web.http.did.port                       | 20085                                | DID api port. If you change the port, the `edc.participant.id` needs to be updated accordingly                                      |
| web.http.issuance.path                  | /api/issuance                        | VC issuance path                                                                                                                    |
| web.http.issuance.port                  | 20086                                | VC issuance port                                                                                                                    |
| web.http.issueradmin.path               | /api/admin                           | Admin path                                                                                                                          |
| web.http.issueradmin.port               | 20087                                | Admin port                                                                                                                          |
| web.http.statuslist.path                | /api/statuslist                      | Status list path                                                                                                                    |
| web.http.statuslist.port                | 20090                                | Status list port                                                                                                                    |
| edc.vault.hashicorp.url                 | http://localhost:8200                | The URL of the `HashiCorp Vault`                                                                                                    |
| edc.vault.hashicorp.token               | devpass                              | The `HashiCorp Vault` access token                                                                                                  |
| edc.sql.schema.autocreate               | true                                 | Flag to autogenerate tables in the `PostgreSQL` if not already done                                                                 |
| edc.datasource.default.user             | edc                                  | Username to authenticate in the database                                                                                            |
| edc.datasource.default.password         | devpass                              | Password to authenticate in the database                                                                                            |
| edc.datasource.default.url              | jdbc:postgresql://localhost:5432/edc | Connection URL of the database                                                                                                      |

## Production Deployment

For a productive deployment, use the provided [Helm Chart](chart/README.md).
The readme also describes all configurable values.
The production deployment assumes that you use a Kubernetes cluster or a comparable system.

### 1. Create a overwrite.yaml

We recommend creating a `overwrite.yaml` file to overwrite the default values from the Helm Chart.
The deployment of the registry highly depends on your target infrastructure.
The following example file is an orientation only and needs to be adjusted by you to fit your infrastructure.
See the [Helm Chart](chart/README.md) documentation to see all values that can be configured.

```yaml
# overwrite.yaml

global:
  participantId: did:web:issuer.my-dataspace.de:issuer
  
issuer:
  ingress:
    host: issuer.my-dataspace.de
  edc:
    issuer:
      seed:
        superUserKey: c3VwZXItdXNlcg==.my-secret-pass-123!
        
vault:
  dev:
    devRootToken: my-secret-pass-123!
```

### 2. Deploy with Helm

```bash
$ helm dependency update
$ helm install dataspace-issuer -f overwrite.yaml . --namespace dataspace-issuer --create-namespace
```

## Local Development

Follow these step to use the `local-dev` runtime.

### 1. Start environment from the connector repository

As the Issuer cannot be used without a `PostgreSQL` database and a `HashiCorp Vault`, there must be a running instances of both.
You can use the [local-dev](...) runtime from the connector repository for that.

### 2. Start the Issuer

From project root execute:
```shell
.\gradlew.bat runtimes:local-dev:build
java '-Dedc.fs.config=runtimes/local-dev/src/main/resources/config.properties' -jar runtimes/local-dev/build/libs/local-dev.jar
```
The build command is only needed if you have made some changes to the source code.
Both can also be triggered directly from your IDE, such as IntelliJ.
