# nifi kubernetes namespace

variable "kubernetes-namespace" {
  description = "The namespace for the kubernetes deployment"
  default     = "dagx"
}

variable "chart-dir" {
  description = "The directory where the local nifi helm chart is located"
  default     = "nifi-chart"
}

provider "kubernetes" {
  config_path = "~/.kube/config"
}

provider "helm" {
  kubernetes {
    config_path = "~/.kube/config"
  }
}

resource "kubernetes_namespace" "nifi" {
  metadata {
    name = var.kubernetes-namespace
    labels = {
      "environment" = "test"
    }
  }
}

resource "helm_release" "nifi" {
  name  = "dagx-nifi-release"
  chart = var.chart-dir
  values = [
  "${file("nifi-chart/openid-values.yaml")}",    
  "${file("nifi-chart/secured-values-with-nifi-toolkit.yaml")}"]
  namespace       = kubernetes_namespace.nifi.metadata[0].name
  cleanup_on_fail = true
  set {
    name  = "nifi.authentication.openid.clientId"
    value = azuread_application.dagx-terraform-nifi-app.application_id
    type  = "string"
  }
  set {
    name  = "nifi.authentication.openid.clientSecret"
    value = azuread_application_password.dagx-terraform-nifi-app-secret.value
    # value = "73P7-cmFG1X~c8DHAK527o85C_kaSN-qkMD"
  }
  set{
      name = "nifi.authentication.openid.discoveryUrl"
      value = "https://login.microsoftonline.com/${data.azurerm_client_config.current.tenant_id}/v2.0/.well-known/openid-configuration"
  }
  set {
    name  = "nifi.bootstrapConf.jvmMinMemory"
    value = "3g"
  }
  set {
    name  = "nifi.bootstrapConf.jvmMaxMemory"
    value = "3g"
  }
  set {
    name  = "resources.requests.memory"
    value = "1Gi"
  }
  set {
    name  = "ingress.enabled"
    value = true
  }
  set {
    name  = "nifi.properties.webProxyHost"
    value = "dagx-${var.resourcesuffix}.${var.location}.cloudapp.azure.com"
  }
  set {
    name  = "initUsers.enabled"
    value = true
  }
  set {
    name  = "admins"
    value = "paul.latzelsperger@beardyinc.com"
  }
  set {
    name  = "uiUsers"
    value = "paul.latzelsperger@beardyinc.com"
  }
}
