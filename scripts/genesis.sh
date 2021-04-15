#!/bin/bash

if [ -z "$1" ]
    then
        echo "please supply your subscription ID as first argument!"
fi

if ! command -v openssl &> /dev/null
then 
    echo "openssl is not installed - aborting!"
fi

if ! command -v jq &> /dev/null
then
    echo "jq (Json Processor) is not installed - aborting!"
fi

DISPLAYNAME=PrimaryIdentity
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
dt=$(date +%s)
rgName=dagx-$dt
echo "create resource group $rgName"
az group create --name $rgName --location westeurope

# provision keyvault
keyvaultName=DagxKeyVault-$dt
echo "provision key vault $keyvaultName and assign roles"
az keyvault create --enable-rbac-authorization -g $rgName -n $keyvaultName -l westeurope 
keyVaultId=$(az keyvault list -g $rgName | jq -r '.[0].id')
az role assignment create --role "Key Vault Secrets Officer" --scope $keyVaultId --assignee $appId

# provision storage account 
saJson=$(az storage account create --name dagxblobstore -g $rgName --https-only true --kind BlobStorage -l westeurope --min-tls-version TLS1_2 --sku Standard_LRS --access-tier hot)

# some output:
echo "certificate: ./cert.pfx and ./cert.pem"
echo "private key: ./key.pem"
echo "client-id: $clientId"
echo "tenant-id: $tenantId"
echo "resource-group: $rgName"

# cleanup
echo 'Press a to cleanup resources, any other key to quit...'
read -n 1 -r -s -p k <&1 

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