# output "client_certificate" {
#   value = data.azurerm_kubernetes_cluster.default.kube_config.0.client_certificate
# }

# output "kube_config" {
#   value     = data.azurerm_kubernetes_cluster.default.kube_config_raw
#   sensitive = true
# }

# output "nifi_client_secret" {
#   value     = azuread_application_password.dagx-terraform-nifi-app-secret.value
#   sensitive = true
# }

# output "nifi_client_id" {
#   value = azuread_application.dagx-terraform-nifi-app.application_id
# }

output "primary_client_id" {
  value = azuread_application.dagx-terraform-app.application_id
}

output "primary_id_certfile" {
  value = abspath("${path.root}/cert.pfx")
}

output "atlas-url"{
  value = "https://${module.atlas-cluster.public-ip.fqdn}"
}
