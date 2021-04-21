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

resource "helm_release" "default" {
  chart      = "ingress-nginx"
  name       = "nginx-ingress"
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

resource "kubernetes_ingress" "atlas-ingress" {
  metadata {
    name      = "atlas-ingress"
    namespace = kubernetes_namespace.ingress-basic.metadata[0].name
    annotations = {
      "nginx.ingress.kubernetes.io/ssl-redirect" : "false"
      "nginx.ingress.kubernetes.io/use-regex" : "true"
      "nginx.ingress.kubernetes.io/rewrite-target" : "/$1"
    }
  }
  spec {
    rule {
      http {
        path {
          backend {
            service_name = "aks-helloworld"
            service_port = 80
          }
          path = "/hello(.*)"
        }
        path {
          backend {
            service_name = "atlas"
            service_port = "2100"
          }
          path = "/atlas(/|$)(.*)"
        }
      }
    }
  }
}

resource "kubernetes_deployment" "deployment" {
  metadata {
    name      = "aks-helloworld"
    namespace = kubernetes_namespace.ingress-basic.metadata[0].name
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "aks-helloworld"
      }
    }
    template {
      metadata {
        labels = {
          app = "aks-helloworld"
        }
      }
      spec {
        container {
          name  = "aks-helloworld"
          image = "mcr.microsoft.com/azuredocs/aks-helloworld:v1"
          port {
            container_port = 80
          }
          env {
            name  = "TITLE"
            value = "I .... AM .... ATLAS!!!"
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "hello" {
  metadata {
    name      = "aks-helloworld"
    namespace = kubernetes_namespace.ingress-basic.metadata[0].name
  }
  spec {
    port {
      port = 80
    }
    selector = {
      app = "aks-helloworld"
    }
    type = "ClusterIP"
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

