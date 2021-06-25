resource "azurerm_cosmosdb_account" "dagx-cosmos" {
  location            = azurerm_resource_group.rg.location
  name                = "dagx-cosmos"
  resource_group_name = azurerm_resource_group.rg.name
  offer_type          = "Standard"
  consistency_policy {
    consistency_level = "Session"
  }
  geo_location {
    failover_priority = 0
    location          = azurerm_resource_group.rg.location
  }

}

//resource "azurerm_cosmosdb_sql_container" "example" {
//  name                  = "example-container"
//  resource_group_name   = azurerm_cosmosdb_account.dagx-cosmos.resource_group_name
//  account_name          = azurerm_cosmosdb_account.dagx-cosmos.name
//  database_name         = azurerm_cosmosdb_sql_database.dagx-database.name
//  partition_key_path    = "/definition/id"
//  partition_key_version = 1
//  throughput            = 400
//
//  indexing_policy {
//    indexing_mode = "Consistent"
//
//    included_path {
//      path = "/*"
//    }
//
//    included_path {
//      path = "/included/?"
//    }
//
//    excluded_path {
//      path = "/excluded/?"
//    }
//  }
//
//  unique_key {
//    paths = ["/definition/idlong", "/definition/idshort"]
//  }
//}

resource "azurerm_cosmosdb_sql_database" "dagx-database" {
  account_name        = azurerm_cosmosdb_account.dagx-cosmos.name
  name                = "dagx-database"
  resource_group_name = azurerm_resource_group.rg.name
  throughput          = 400
}

resource "azurerm_cosmosdb_sql_container" "transferprocess" {
  name                = "dagx-transferprocess"
  resource_group_name = azurerm_cosmosdb_account.dagx-cosmos.resource_group_name
  account_name        = azurerm_cosmosdb_account.dagx-cosmos.name
  database_name       = azurerm_cosmosdb_sql_database.dagx-database.name
  partition_key_path  = "/partitionKey"
}

resource "azurerm_cosmosdb_sql_stored_procedure" "nextForState" {
  name                = "nextForState"
  resource_group_name = azurerm_cosmosdb_account.dagx-cosmos.resource_group_name
  account_name        = azurerm_cosmosdb_account.dagx-cosmos.name
  database_name       = azurerm_cosmosdb_sql_database.dagx-database.name
  container_name      = azurerm_cosmosdb_sql_container.transferprocess.name

  body = file("nextForState.js")
}

resource "azurerm_key_vault_secret" "cosmos_db_master_key" {
  key_vault_id = azurerm_key_vault.dagx-terraform-vault.id
  name         = azurerm_cosmosdb_account.dagx-cosmos.name
  value        = azurerm_cosmosdb_account.dagx-cosmos.primary_master_key
}

output "cosmos-config" {
  value = {
    account-name = azurerm_cosmosdb_account.dagx-cosmos.name
    db-name      = azurerm_cosmosdb_sql_database.dagx-database.name
  }
}