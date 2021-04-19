# Configure the Azure provider
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 2.26"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "1.4.0"
    }
  }

  required_version = ">= 0.14.9"
}

variable "resourcesuffix" {
  description = "identifying string that is used in all azure resources"
}
variable "location" {
  description = "geographic location of the Azure resources"
  default     = "westeurope"
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy = true
    }
  }
}

provider "azuread" {
  # Configuration options
}

resource "azurerm_resource_group" "rg" {
  name     = "dagx-${var.resourcesuffix}-resources"
  location = var.location
}

# App registration for the primary identity
resource "azuread_application" "dagx-terraform-app" {
  display_name               = "dagx-${var.resourcesuffix}-app"
  available_to_other_tenants = false
}

resource "azuread_application_certificate" "dagx-terraform-app-cert" {
  type                  = "AsymmetricX509Cert"
  application_object_id = azuread_application.dagx-terraform-app.id
  value                 = file("cert.pem")
  end_date_relative     = "2400h"
}

resource "azuread_service_principal" "dagx-terraform-app-sp" {
  application_id               = azuread_application.dagx-terraform-app.application_id
  app_role_assignment_required = false
  tags                         = ["terraform"]
}

data "azurerm_client_config" "current" {}


# Keyvault
resource "azurerm_key_vault" "dagx-terraform-vault" {
  name                        = "dagx-${var.resourcesuffix}-vault"
  location                    = azurerm_resource_group.rg.location
  resource_group_name         = azurerm_resource_group.rg.name
  enabled_for_disk_encryption = false
  tenant_id                   = data.azurerm_client_config.current.tenant_id
  soft_delete_retention_days  = 7
  purge_protection_enabled    = false

  sku_name                  = "standard"
  enable_rbac_authorization = true
}

# Role assignment so that the primary identity may access the vault
resource "azurerm_role_assignment" "assign_vault" {
  scope                = azurerm_key_vault.dagx-terraform-vault.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = azuread_service_principal.dagx-terraform-app-sp.object_id
}

#storage account
resource "azurerm_storage_account" "dagxblobstore" {
  name                     = "dagxtfblob"
  resource_group_name      = azurerm_resource_group.rg.name
  location                 = azurerm_resource_group.rg.location
  account_tier             = "Standard"
  account_replication_type = "GRS"
  account_kind             = "BlobStorage"
}

# resource "azurerm_key_vault_secret" "key1" {
#     name = "blobstore-key1"  
#     value= data.azurerm_storage_account.dagxblobstore.primary_access_key
#     key_vault_id = azurerm_key_vault.dagx-terraform-vault.id
# }

resource "azurerm_kubernetes_cluster" "dagx" {
  name                = "dagx-${var.resourcesuffix}-aks1"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix          = "dagx-${var.resourcesuffix}-aks"

  default_node_pool {
    name               = "agentpool"
    node_count         = 3
    vm_size            = "Standard_D2_v2"
    os_disk_size_gb    = 30
    availability_zones = ["1", "2", "3"]
    max_pods           = 110
    type               = "VirtualMachineScaleSets"
    os_disk_type       = "Managed"
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    load_balancer_sku = "standard"
    network_plugin    = "kubenet"
  }

  addon_profile {
    http_application_routing {
      enabled = true
    }
    azure_policy {
      enabled = false
    }
  }

  tags = {
    Environment = "Test"
  }
}

# App registration for the loadbalancer
resource "azuread_application" "dagx-terraform-nifi-app" {
  display_name               = "Dagx-${var.resourcesuffix}-Nifi"
  available_to_other_tenants = false
}

resource "random_password" "password" {
  length           = 16
  special          = true
  override_special = "_%@"
}

resource "azuread_application_password" "dagx-terraform-nifi-app-secret" {
  application_object_id = azuread_application.dagx-terraform-nifi-app.id
  description           = "Password for Nifi app registration"
  end_date              = "2099-01-01T01:02:03Z"
  value                 = random_password.password.result
}


resource "azuread_service_principal" "dagx-terraform-nifi-app-sp" {
  application_id               = azuread_application.dagx-terraform-nifi-app.application_id
  app_role_assignment_required = false
  tags                         = ["terraform"]
}

output "client_certificate" {
  value = azurerm_kubernetes_cluster.dagx.kube_config.0.client_certificate
}

output "kube_config" {
  value     = azurerm_kubernetes_cluster.dagx.kube_config_raw
  sensitive = true
}

output "nifi_client_secret" {
  value     = azuread_application_password.dagx-terraform-nifi-app-secret.value
  sensitive = true
}

output "nifi_client_id" {
  value = azuread_application.dagx-terraform-nifi-app.application_id
}
