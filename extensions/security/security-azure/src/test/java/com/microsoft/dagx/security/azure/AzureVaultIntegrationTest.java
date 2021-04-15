package com.microsoft.dagx.security.azure;

import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.VaultResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class AzureVaultIntegrationTest {

    private static AzureVault azureVault;
    private String secretKey;

    @BeforeAll
    public static void setupAzure() throws IOException, URISyntaxException {
        var resStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("azurecredentials.properties");
        var props = new Properties();
        props.load(resStream);

        var clientId = props.getProperty("dagx.vault.clientid");
        var tenantId = props.getProperty("dagx.vault.tenantid");
        var vaultName = props.getProperty("dagx.vault.name");
        var certfile = props.getProperty("dagx.vault.certificate");
        var certPath= Thread.currentThread().getContextClassLoader().getResource(certfile).toURI().getPath();

        azureVault = AzureVault.authenticateWithCertificate(new LoggerMonitor(), clientId, tenantId, certPath, vaultName);
    }

    @BeforeEach
    void verifyAzureResourceGroup() {
        secretKey = "testkey";
    }

    @Test
    void storeSecret() {

        VaultResponse vaultResponse = azureVault.storeSecret(secretKey, "testvalue");

        assertTrue(vaultResponse.success());
        assertEquals("testvalue", azureVault.resolveSecret(secretKey));
    }

    @Test
    void storeSecret_overwrites() {
        azureVault.storeSecret(secretKey, "value1");
        azureVault.storeSecret(secretKey, "value2");

        assertEquals("value2", azureVault.resolveSecret(secretKey));
    }

    @Test
    void resolveSecret_notExist() {
        assertNull(azureVault.resolveSecret("notexist"));
    }

    @Test
    void resolveSecret() {
        assertTrue(azureVault.storeSecret(secretKey, "someVal").success());

        assertEquals("someVal", azureVault.resolveSecret(secretKey));
    }

    @Test
    void delete_notExist() {
        VaultResponse vr = azureVault.deleteSecret("notexist");
        assertFalse(vr.success());
        assertNotNull(vr.error());
    }

    @Test
    void delete() {
        azureVault.storeSecret(secretKey, "someval");

        VaultResponse vr = azureVault.deleteSecret(secretKey);
        assertTrue(vr.success());
        assertNull(vr.error());
    }

    @AfterEach
    void cleanup() {
        azureVault.deleteSecret(secretKey);
    }

    private static class LoggerMonitor implements Monitor {
        private final Logger logger = LoggerFactory.getLogger(LoggerMonitor.class);

        @Override
        public void info(String message, Throwable... errors) {
            if (errors == null || errors.length == 0) {
                logger.info(message);
            } else {
                logger.error(message);
            }
        }
    }
}
