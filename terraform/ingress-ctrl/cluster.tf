terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 2.42"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">=2.0.3"
    }
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.1.0"
    }
    tls = {

    }
  }
}

provider "azurerm" {
  features {
  }
}

provider "kubernetes" {
  host                   = azurerm_kubernetes_cluster.default.kube_config.0.host
  client_certificate     = base64decode(azurerm_kubernetes_cluster.default.kube_config.0.client_certificate)
  client_key             = base64decode(azurerm_kubernetes_cluster.default.kube_config.0.client_key)
  cluster_ca_certificate = base64decode(azurerm_kubernetes_cluster.default.kube_config.0.cluster_ca_certificate)
}

provider "helm" {
  kubernetes {
    host                   = azurerm_kubernetes_cluster.default.kube_config.0.host
    client_certificate     = base64decode(azurerm_kubernetes_cluster.default.kube_config.0.client_certificate)
    client_key             = base64decode(azurerm_kubernetes_cluster.default.kube_config.0.client_key)
    cluster_ca_certificate = base64decode(azurerm_kubernetes_cluster.default.kube_config.0.cluster_ca_certificate)
  }
}

provider "tls" {

}
### VARIABLES
variable "location" {
  type    = string
  default = "westeurope"
}
variable "kub_namespace" {
  type    = string
  default = "ingress-basic"
}

variable "resources_rg" {
  type    = string
  default = "aks-test-cluster-resources"
}

variable "cluster_rg" {
  type    = string
  default = "aks-test-cluster"
}

variable "atlas_service_name" {
  type    = string
  default = "foobar"
}

variable "atlas_ingress_cert_name" {
  default = "atlas-ingress-tls"
}

### RESOURCES
resource "tls_private_key" "atlas-ingress" {
  algorithm = "ECDSA"
}

resource "tls_self_signed_cert" "atlas-ingress" {
  allowed_uses = [
    "server_auth",
    "digital_signature"
  ]
  key_algorithm         = tls_private_key.atlas-ingress.algorithm
  private_key_pem       = tls_private_key.atlas-ingress.private_key_pem
  validity_period_hours = 72
  early_renewal_hours   = 12
  subject {
    common_name  = azurerm_public_ip.aks-cluster-public-ip.fqdn
    organization = "Gaia-X Data Appliance"
  }
  dns_names = [
  azurerm_public_ip.aks-cluster-public-ip.fqdn]
}

resource "azurerm_resource_group" "resources" {
  location = var.location
  name     = var.resources_rg
}

resource "azurerm_public_ip" "aks-cluster-public-ip" {
  resource_group_name = azurerm_kubernetes_cluster.default.node_resource_group
  location            = azurerm_resource_group.resources.location
  domain_name_label   = "dagx-atlas"
  allocation_method   = "Static"
  name                = "atlasPublicIp"
  sku                 = "Standard"
}

resource "azurerm_kubernetes_cluster" "default" {
  name                = var.cluster_rg
  location            = azurerm_resource_group.resources.location
  resource_group_name = azurerm_resource_group.resources.name
  dns_prefix          = var.cluster_rg

  default_node_pool {
    name            = "agentpool"
    node_count      = 2
    vm_size         = "Standard_D2_v2"
    os_disk_size_gb = 30
    max_pods        = 110
    type            = "VirtualMachineScaleSets"
    os_disk_type    = "Managed"
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    load_balancer_sku = "standard"
    network_plugin    = "kubenet"
  }
}

resource "kubernetes_namespace" "ingress-basic" {
  metadata {
    name = "ingress-basic"
  }
}

resource "helm_release" "ingress-controller" {
  chart      = "ingress-nginx"
  name       = "nginx-ingress-controller"
  namespace  = kubernetes_namespace.ingress-basic.metadata[0].name
  repository = "https://kubernetes.github.io/ingress-nginx"

  set {
    name  = "controller.replicaCount"
    value = "2"
  }
  //  set {
  //    name  = "controller.nodeSelector.beta.kubernetes.io/os"
  //    value = "linux"
  //  }
  //  set {
  //    name  = "defaultBackend.nodeSelector.beta.kubernetes.io/os"
  //    value = "linux"
  //  }
  //  set {
  //    name  = "controller.admissionWebhooks.patch.nodeSelector.beta.kubernetes.io/os"
  //    value = "linux"
  //  }
  set {
    name  = "controller.service.loadBalancerIP"
    value = azurerm_public_ip.aks-cluster-public-ip.ip_address
  }
  set {
    name  = "controller.service.annotations.service.beta.kubernetes.io/azure-dns-label-name"
    value = azurerm_public_ip.aks-cluster-public-ip.domain_name_label
  }
}

resource "helm_release" "atlas" {
  name            = var.atlas_service_name
  chart           = "./atlas-chart"
  values          = []
  namespace       = kubernetes_namespace.ingress-basic.metadata[0].name
  cleanup_on_fail = true
  set {
    name  = "service.type"
    value = "ClusterIP"
  }
  //  set {
  //    name  = "ingress.enabled"
  //    value = "false"
  //  }
  //  set {
  //    name  = "ingress.hosts[0]"
  //    value = azurerm_public_ip.aks-cluster-public-ip.fqdn
  //  }
}

resource "kubernetes_secret" "atlas-ingress-tls" {
  metadata {
    namespace = kubernetes_namespace.ingress-basic.metadata[0].name
    name      = var.atlas_ingress_cert_name
  }
  data = {
    "tls.crt" = tls_private_key.atlas-ingress.public_key_pem
    "tls.key" = tls_private_key.atlas-ingress.private_key_pem
  }
}

resource "kubernetes_ingress" "ingress-route" {
  metadata {
    name      = "atlas-ingress"
    namespace = kubernetes_namespace.ingress-basic.metadata[0].name
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
            service_name = "${var.atlas_service_name}-atlas"
            service_port = 21000
          }
          path = "/"
        }
      }
    }
    tls {
      hosts       = [azurerm_public_ip.aks-cluster-public-ip.fqdn]
      secret_name =  kubernetes_secret.atlas-ingress-tls.metadata[0].name
    }
  }
}

output "aks-domain-name-label" {
  value = azurerm_public_ip.aks-cluster-public-ip.domain_name_label
}
output "aks-fqdn" {
  value = azurerm_public_ip.aks-cluster-public-ip.fqdn
}
output "aks-public-ip" {
  value = azurerm_public_ip.aks-cluster-public-ip.ip_address
}

