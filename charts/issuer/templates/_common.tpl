{{/*
Allow the release namespace to be overridden for multi-namespace deployments in combined charts
*/}}
{{- define "common.namespace" -}}
{{- default .Release.Namespace .Values.namespaceOverride | trimSuffix "-" }}
{{- end -}}

{{/*
Generates the issuer labels
*/}}
{{- define "issuer.labels" -}}
app.kubernetes.io/name: issuer
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/component: issuer
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{/*
Generates issuer labels to match immutable field like deployment templates or services
*/}}
{{- define "issuer.matchLabels" -}}
app.kubernetes.io/name: issuer
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: issuer
{{- end -}}

{{- define "issuer.statuslistUrl" -}}
{{- printf "https://%s%s" .Values.issuer.ingress.host .Values.issuer.endpoints.statuslist.path | quote }}
{{- end -}}

{{/*
Generates the vault labels
*/}}
{{- define "vault.labels" -}}
app.kubernetes.io/name: issuer
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.vault.image.tag | quote }}
app.kubernetes.io/component: vault
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{/*
Generates vault labels to match immutable field like deployment templates or services
*/}}
{{- define "vault.matchLabels" -}}
app.kubernetes.io/name: issuer
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: vault
{{- end -}}
