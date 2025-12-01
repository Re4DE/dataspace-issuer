# Issuer seed Extension

This extension creates a Super User and an Issuer participant context. 

## Settings

| Setting                  | Example Value                       | Description                                                                                                                          |
|--------------------------|-------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| edc.ih.api.superuser.key | c3VwZXItdXNlcg==.changeme           | The api key of the super user in the form of 'base64(<participantId>).<random-string>'. If not present a random string will be used  |
| edc.issuer.base.url      | https://issuer.fraunhofer.de        | The base url of the data space issuer                                                                                                |
| edc.issuer.did           | did:web:issuer.fraunhofer.de:issuer | The did of the data space issuer                                                                                                     |