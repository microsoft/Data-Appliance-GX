#!/bin/bash
suffix=''
if [ -z "$1" ]
    then
        suffix=$(date +%s)
    else
        suffix=$1
fi

echo "suffix is $suffix"

if ! command -v openssl &> /dev/null
then 
    echo "openssl is not installed - aborting!"
fi

if ! command -v jq &> /dev/null
then
    echo "jq (Json Processor) is not installed - aborting!"
fi

DISPLAYNAME=PrimaryIdentity-$suffix
if test -f "cert.pem" ; then
    echo "certificate exists - reusing"
else
    echo "no certificate found - creating a new one"
    # create a certificate - will ask for some information!
    openssl req -newkey rsa:4096 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem

    # convert cert.pem to cert.pfx
    openssl pkcs12 -inkey key.pem -in cert.pem -export -out cert.pfx
fi

# provision an app registration for the primary identity
echo "provision app registration"
az ad app create --display-name "$DISPLAYNAME" --key-type AsymmetricX509Cert --available-to-other-tenants false > /dev/null
appJson=$(az ad app list --display-name $DISPLAYNAME)
objectId=$(echo $appJson | jq -r '.[0].objectId' )
appId=$(echo $appJson | jq -r '.[0].appId' )

# upload the public key to the app reg
echo "upload certificate"
credJson=$(az ad app credential reset --id $objectId --cert @cert.pem)
clientId=$(echo $credJson | jq -r '.appId')
tenantId=$(echo $credJson | jq -r '.tenant')

# create a service principial for the App Reg - THIS IS THE IMPORTANT PART!
echo "create service principal"
spJson=$(az ad sp create --id $objectId)
spId=$(echo $spJson | jq -r '.objectId')

# request User.Read access
# az ad app update --id $objectId --required-resource-access @manifest.json

# create a resource group

rgName=dagx-$suffix
echo "create resource group $rgName"
az group create --name $rgName --location westeurope

# provision keyvault
keyvaultName=DagxKeyVault-$suffix
echo "provision key vault $keyvaultName and assign roles"
az keyvault create --enable-rbac-authorization -g $rgName -n $keyvaultName -l westeurope 
keyVaultId=$(az keyvault list -g $rgName | jq -r '.[0].id')
az role assignment create --role "Key Vault Secrets Officer" --scope $keyVaultId --assignee $appId

# assign current user as admin to vault
currentUserOid=$(az ad signed-in-user show | jq -r '.objectId')
az role assignment create --role "Key Vault Administrator" --scope $keyVaultId --assignee $currentUserOid

# provision storage account 
saName=dagxblobstore$suffix
saJson=$(az storage account create --name $saName -g $rgName --https-only true --kind BlobStorage -l westeurope --min-tls-version TLS1_2 --sku Standard_LRS --access-tier hot)

## let keyvault handle the storage account's key regeneration and store an SAS token definition
## if we wanna use that feature, we can uncomment the next 5 lines

# saId=$(echo $saJson | jq -r '.id')
# az role assignment create --role "Storage Account Key Operator Service Role" --scope $saId --assignee cfa8b339-82a2-471a-a3c9-0fc0be7a4093
# az keyvault storage add --vault-name $keyvaultName -n $saName --active-key-name key1 --auto-regenerate-key --regeneration-period P90D --resource-id $saId
# sas=$(az storage account generate-sas --expiry 2021-01-05 --permission rwdl --resource-type co --services b --https-only --account-name $saName --account-key 00000000)
# az keyvault storage sas-definition create --vault-name $keyVaultName --account-name $saName -n DataTransferDefinition --validity-period P2D --sas-type service --templateUri $sas

# Store storage account key1 and key2 in vault
keyJson=$(az storage account keys list -n $saName)
key1=$(echo $keyJson | jq -r '.[0].value')
key2=$(echo $keyJson | jq -r '.[1].value')
az keyvault secret set --name "$saName-key1" --vault-name $keyvaultName --value $key1
az keyvault secret set --name "$saName-key2" --vault-name $keyvaultName --value $key2
echo "storage account keys successfully stored in vault"

echo "remove role assignment for current user"
az role assignment delete --role "Key Vault Administrator" --scope $keyVaultId --assignee $currentUserOid

# some output:
echo "certificate: ./cert.pfx and ./cert.pem"
echo "private key: ./key.pem"
echo "client-id: $clientId"
echo "tenant-id: $tenantId"
echo "resource-group: $rgName"

# cleanup
echo 'Press q to cleanup resources, any other key to quit...'
read -n 1 k <&1 

if [[ $k = q ]] ; then

    echo "cleaning up..."
    echo "deleting app registration"
    az ad app delete --id $objectId
    echo "deleting resource group"
    az group delete -n $rgName -y
    echo "purging vault $keyvaultName"
    az keyvault purge --name $keyvaultName
    echo "cleanup done"

fi