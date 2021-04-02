package com.microsoft.dagx.security.azure;

import com.microsoft.dagx.spi.protocol.web.WebService;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.system.ServiceExtension;
import com.microsoft.dagx.spi.system.ServiceExtensionContext;

public class AzureApiExtension implements ServiceExtension {

    @Override
    public void initialize(ServiceExtensionContext context) {
        var webService = context.getService(WebService.class);
        var vault= context.getService(Vault.class);
        var monitor= context.getMonitor();

        var ctrl= new AzureVaultAccessController(vault);
        webService.registerController(ctrl);
    }
}
