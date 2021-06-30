terraform {
  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = ">= 2.62.1"
    }
    kubernetes = {
      source = "hashicorp/kubernetes"
      version = ">= 2.0.3"
    }
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.1.0"
    }
    tls = {

    }
  }
}

resource "tls_private_key" "connector-ingress-key" {
  algorithm = "ECDSA"
}

resource "kubernetes_namespace" "connector" {
  metadata {
    name = "${var.resourcesuffix}-cons"
  }
}

resource "local_file" "kubeconfig" {
  content = var.kubeconfig
  filename = "${path.root}/kubeconfig"
}

resource "kubernetes_secret" "connector-cert-secret" {
  metadata {
    name = "blobstore-key"
    namespace = kubernetes_namespace.connector.metadata[0].name
  }
  type = "Opaque"
  data = {
    azurestorageaccountname = "dagxtstate"
    azurestorageaccountkey = "t9Vm9GKL0KAEMt9OokmEbTxIr++aN7wsbug4R52g3EuPy6GcZoYwxjWaXUw7I3JqhnXUuJoJW093+DNQh5YZgA=="
  }
}

//resource "kubernetes_deployment" "connector-deployment" {
//  metadata {
//    name = "connector-demo"
//    namespace = kubernetes_namespace.connector.id
//  }
//  spec {
//    replicas = 2
//    selector {
//      match_labels = {
//        connector-demo: "web"
//      }
//    }
//    template {
//      metadata {
//        labels = {
//          connector-demo: "web"
//        }
//      }
//      spec {
//        container {
//          name = "connector"
//          image = "ghcr.io/microsoft/data-appliance-gx/dagx-demo:latest"
//          image_pull_policy = "Always"
//          env {
//            name = "CLIENTID"
//            value = var.image_env.clientId
//          }
//          env {
//            name = "TENANTID"
//            value = var.image_env.tenantId
//          }
//          env {
//            name = "VAULTNAME"
//            value = var.image_env.vaultName
//          }
//          env {
//            name = "ATLAS_URL"
//            value = var.image_env.atlasUrl
//          }
//          env {
//            name = "NIFI_URL"
//            value = var.image_env.nifiUrl
//          }
//          env {
//            name = "NIFI_FLOW_URL"
//            value = var.image_env.nifiFlowUrl
//          }
//          env {
//            name = "COSMOS_ACCOUNT"
//            value = var.image_env.cosmosAccount
//          }
//          env {
//            name = "COSMOS_DB"
//            value = var.image_env.cosmosDb
//          }
//          port {
//            container_port = 8181
//            host_port = 8181
//            protocol = "TCP"
//          }
//          volume_mount {
//            mount_path = "/cert"
//            name = "certificates"
//            read_only = true
//          }
//        }
//        volume {
//          name = "certificates"
//          azure_file {
//            secret_name = kubernetes_secret.connector-cert-secret.metadata[0].name
//            share_name = "certificates"
//          }
//        }
//      }
//    }
//  }
//}

output "connector-cluster-namespace" {
  value = kubernetes_namespace.connector.metadata[0].name
}
