# nifi kubernetes namespace

variable "kubernetes-namespace" {
  description = "The namespace for the kubernetes deployment"
  default     = "dagx"
}

variable "chart-dir" {
  description = "The directory where the local nifi helm chart is located"
  default     = "./nifi-chart"
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
  name      = "dagx-nifi-release"
  chart     = var.chart-dir
  values    = ["${var.chart-dir}/openid-values.yaml", "${var.chart-dir}/secured-values-with-nifi-toolkit.yaml"]
  namespace = kubernetes_namespace.nifi.metadata[0].name
}
