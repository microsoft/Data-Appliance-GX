package com.microsoft.dagx.security.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.security.VaultResponse;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Implements a vault backed by Azure Vault.
 */
public class AzureVault implements Vault {

    private final Monitor monitor;
    private final SecretClient secretClient;

    public AzureVault(Monitor monitor, String clientId, String tenantId, String certificatePath, String keyVaultName) {
        this.monitor = monitor;

        String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";

        TokenCredential credential = buildCertificateCredentials(clientId, tenantId, certificatePath);

        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(credential)
                .buildClient();
    }

    private ClientCertificateCredential buildCertificateCredentials(String clientId, String tenantId, String certificatePath) {
        return new ClientCertificateCredentialBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .pfxCertificate(certificatePath, "")
                .build();
    }

    @Override
    public @Nullable String resolveSecret(String key) {
        try {

            var secret = secretClient.getSecret(key);
            monitor.debug("Secret obtained successfully");
            return secret.getValue();
        } catch (ResourceNotFoundException ex) {
            monitor.severe("Secret not found!", ex);
            return null;
        }

    }

    @Override
    public VaultResponse storeSecret(String key, String value) {
        try {
            var secret = secretClient.setSecret(key, value);
            monitor.debug("storing secret successful");
            return VaultResponse.OK;
        } catch (Exception ex) {
            monitor.severe("Error storing secret", ex);
            return new VaultResponse(ex.getMessage());
        }
    }

    @Override
    public VaultResponse deleteSecret(String key) {
        SyncPoller<DeletedSecret, Void> poller = null;
        try {
            poller = secretClient.beginDeleteSecret(key);
            monitor.debug("Begin deleting secret");
            poller.waitForCompletion(Duration.ofMinutes(1));
            monitor.debug("deletion complete");
            return VaultResponse.OK;
        } catch (ResourceNotFoundException ex) {
            monitor.severe("Error storing secret - does not exist!");
            return new VaultResponse(ex.getMessage());
        } catch (RuntimeException re) {
            monitor.severe("Error deleting secret", re);

            if (re.getCause() != null && re.getCause() instanceof TimeoutException) {
                try {
                    if (poller != null) {
                        poller.cancelOperation();
                    }
                } catch (Exception e) {
                    monitor.severe("Failed to abort the deletion. ", e);
                    return new VaultResponse(e.getMessage());
                }
            }
            return new VaultResponse(re.getMessage());
        }
    }
}
