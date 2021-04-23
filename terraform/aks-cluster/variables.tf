
variable "kubernetes_version" {
  default = "1.19"
}

variable "cluster_name" {
  type = string
}

variable "location" {
  type = string
}

variable "enable_http_routing"{
  type = bool
  default = true
}
variable "dns" {
  type=string
}
