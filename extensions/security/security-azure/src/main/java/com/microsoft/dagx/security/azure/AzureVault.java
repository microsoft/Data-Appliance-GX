package com.microsoft.dagx.security.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.security.VaultResponse;
import org.jetbrains.annotations.Nullable;

/**
 * Implements a vault backed by Azure Vault.
 */
public class AzureVault implements Vault {

    private final Monitor monitor;
    private final SecretClient secretClient;

    public AzureVault(Monitor monitor, String clientId, String tenantId, String certificatePath) {
        this.monitor = monitor;

        var keyVaultName = "dagx-vault";
        String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";

        TokenCredential credential = buildCertificateCredentials(clientId, tenantId, certificatePath);

        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(credential)
                .buildClient();
    }

    private ClientCertificateCredential buildCertificateCredentials(String clientId, String tenantId, String certificatePath){
        return new ClientCertificateCredentialBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .pfxCertificate(certificatePath, "")
                .build();
    }

    @Override
    public @Nullable String resolveSecret(String key) {
        try {
            var secret = secretClient.getSecret("test");
            monitor.info("Secret \"test\" obtained successfully");
            return secret.getValue();
        } catch (ResourceNotFoundException ex) {
            monitor.severe("Secret " + key + " not found!", ex);
            return null;
        }

    }

    @Override
    public VaultResponse storeSecret(String key, String value) {
        try {
            var secret = secretClient.setSecret(key, value);
            monitor.info("storing secret "+key+" successful");
            return VaultResponse.OK;
        } catch (Exception ex) {
            monitor.severe("Error storing secret " + key, ex);
            return new VaultResponse(ex.getMessage());
        }
    }
}
