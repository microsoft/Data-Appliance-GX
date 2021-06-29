terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 2.62.1"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.0.3"
    }
    tls = {

    }
  }
}

resource "tls_private_key" "connector-ingress-key" {
  algorithm = "ECDSA"
}

resource "tls_self_signed_cert" "connector-ingress-cert" {
  allowed_uses = [
    "server_auth",
    "digital_signature"
  ]
  key_algorithm         = tls_private_key.connector-ingress-key.algorithm
  private_key_pem       = tls_private_key.connector-ingress-key.private_key_pem
  validity_period_hours = 72
  early_renewal_hours   = 12
  subject {
    common_name  = var.public-ip.fqdn
    organization = "Gaia-X Data Appliance"
  }
  dns_names = [
  var.public-ip.fqdn]
}

resource "kubernetes_namespace" "connector" {
  metadata {
    name = "${var.resourcesuffix}-connector-ns"
  }
}

resource "kubernetes_secret" "connector-ingress-tls" {
  metadata {
    namespace = kubernetes_namespace.connector.metadata[0].name
    name      = var.connector_ingress_cert_name
  }
  data = {
    "tls.crt" = tls_private_key.connector-ingress-key.public_key_pem
    "tls.key" = tls_private_key.connector-ingress-key.private_key_pem
  }
}

resource "kubernetes_ingress" "ingress-route" {
  metadata {
    name      = "connector-ingress"
    namespace = kubernetes_namespace.connector.metadata[0].name
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
            service_name = var.connector_service_name
            service_port = 8181
          }
          path = "/"
        }
      }
    }
    tls {
      hosts       = [var.public-ip.fqdn]
      secret_name = kubernetes_secret.connector-ingress-tls.metadata[0].name
    }
  }
}

resource "local_file" "kubeconfig" {
  content  = var.kubeconfig
  filename = "${path.root}/kubeconfig"
}

output "connector-cluster-namespace" {
  value = kubernetes_namespace.connector.metadata[0].name
}
