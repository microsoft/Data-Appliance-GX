terraform {
  required_providers {
    kubernetes = {
      source = "hashicorp/kubernetes"
      version = ">= 2.0.3"
    }
    helm = {
      source = "hashicorp/helm"
      version = ">= 2.1.0"
    }
  }
}

resource "kubernetes_namespace" "nifi" {
  metadata {
    name = "${var.resourcesuffix}-nifi"
  }
}

# add aut-generated TLS certificate for the ingress
resource "tls_private_key" "nifi-ingress" {
  algorithm = "ECDSA"
}
resource "tls_self_signed_cert" "nifi-ingress" {
  allowed_uses = [
    "server_auth",
    "digital_signature"
  ]
  key_algorithm         = tls_private_key.nifi-ingress.algorithm
  private_key_pem       = tls_private_key.nifi-ingress.private_key_pem
  validity_period_hours = 72
  early_renewal_hours   = 12
  subject {
    common_name  = var.public-ip.fqdn
    organization = "Gaia-X Data Appliance"
  }
  dns_names = [
    var.public-ip.fqdn]
}
resource "kubernetes_secret" "atlas-ingress-tls" {
  metadata {
    namespace = kubernetes_namespace.nifi.metadata[0].name
    name      = var.nifi_ingress_cert_name
  }
  data = {
    "tls.crt" = tls_private_key.nifi-ingress.public_key_pem
    "tls.key" = tls_private_key.nifi-ingress.private_key_pem
  }
}

# the ingress + ingress route for the nifi cluster
resource "helm_release" "ingress-controller" {
  chart      = "ingress-nginx"
  name       = "nginx-ingress-controller"
  namespace  = kubernetes_namespace.nifi.metadata[0].name
  repository = "https://kubernetes.github.io/ingress-nginx"

  set {
    name  = "controller.replicaCount"
    value = "2"
  }
  set {
    name  = "controller.service.loadBalancerIP"
    value = var.public-ip.ip_address
  }
  set {
    name  = "controller.service.annotations.service.beta.kubernetes.io/azure-dns-label-name"
    value = var.public-ip.domain_name_label
  }
}
resource "kubernetes_ingress" "ingress-route" {
  metadata {
    name      = "nifi-ingress"
    namespace = kubernetes_namespace.nifi.metadata[0].name
    annotations = {
      "nginx.ingress.kubernetes.io/ssl-redirect" : "false"
      "nginx.ingress.kubernetes.io/use-regex" : "true"
      //      "nginx.ingress.kubernetes.io/rewrite-target" : "/$2"
    }
  }
  spec {
    rule {
      http {
        path {
          backend {
            service_name = var.nifi_service_name
            service_port = 21000
          }
          path = "/"
        }
      }
    }
    tls {
      hosts       = [var.public-ip.fqdn]
      secret_name = kubernetes_secret.atlas-ingress-tls.metadata[0].name
    }
  }
}

# App registration for the loadbalancer
resource "azuread_application" "dagx-terraform-nifi-app" {
  display_name = "Dagx-${var.resourcesuffix}-Nifi"
  available_to_other_tenants = false
  reply_urls = [
    "https://${var.public-ip.fqdn}/nifi-api/access/oidc/callback"]
}

resource "random_password" "password" {
  length = 16
  special = true
  override_special = "_%@"
}

resource "azuread_application_password" "dagx-terraform-nifi-app-secret" {
  application_object_id = azuread_application.dagx-terraform-nifi-app.id
  end_date = "2099-01-01T01:02:03Z"
  value = random_password.password.result
}

resource "azuread_service_principal" "dagx-terraform-nifi-app-sp" {
  application_id = azuread_application.dagx-terraform-nifi-app.application_id
  app_role_assignment_required = false
  tags = [
    "terraform"]
}

resource "helm_release" "nifi" {
  name = var.nifi_service_name
  chart = var.chart-dir
  values = [
    file("nifi-chart/openid-values.yaml"),
    file("nifi-chart/secured-values-with-nifi-toolkit.yaml")]
  namespace = kubernetes_namespace.nifi.metadata[0].name
  cleanup_on_fail = true
  set {
    name = "nifi.authentication.openid.clientId"
    value = azuread_application.dagx-terraform-nifi-app.application_id
    type = "string"
  }
  set {
    name = "nifi.authentication.openid.clientSecret"
    value = azuread_application_password.dagx-terraform-nifi-app-secret.value
  }
  set {
    name = "nifi.authentication.openid.discoveryUrl"
    value = "https://login.microsoftonline.com/${var.tenant_id}/v2.0/.well-known/openid-configuration"
  }
  set {
    name = "nifi.bootstrapConf.jvmMinMemory"
    value = "3g"
  }
  set {
    name = "nifi.bootstrapConf.jvmMaxMemory"
    value = "3g"
  }
  set {
    name = "resources.requests.memory"
    value = "1Gi"
  }
  set {
    name = "ingress.enabled"
    value = true
  }
  set {
    name = "nifi.properties.webProxyHost"
    value = "dagx-${var.resourcesuffix}.${var.location}.cloudapp.azure.com"
  }
  set {
    name = "initUsers.enabled"
    value = true
  }
  set {
    name = "admins"
    value = "paul.latzelsperger@beardyinc.com"
  }
  set {
    name = "uiUsers"
    value = "paul.latzelsperger@beardyinc.com"
  }
  set {
    name = "zookeeper.replicaCount"
    value = "1"
  }
  set{
    name = "nifi.replicaCount"
    value = "2"
  }
}

resource "local_file" "kubeconfig" {
  content = var.kubeconfig
  filename = "${path.root}/kubeconfig"
}

output "nifi-cluster-namespace" {
  value = kubernetes_namespace.nifi.metadata[0].name
}
