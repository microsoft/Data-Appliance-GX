/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.security.azure;

import com.microsoft.dagx.spi.security.Vault;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(EasyMockExtension.class)
class AzureCertificateResolverTest {

    @Mock
    Vault vault;

    private AzureCertificateResolver certificateResolver;

    @Test
    void verifyResolution() {
        EasyMock.expect(vault.resolveSecret("test")).andReturn(TEST);
        EasyMock.replay(vault);

        assertNotNull(certificateResolver.resolveCertificate("test"));
        EasyMock.verify(vault);
    }

    @BeforeEach
    void setUp() {
        certificateResolver = new AzureCertificateResolver(vault);
    }


    static String TEST = "-----BEGIN CERTIFICATE-----" +
            "MIICVjCCAb8CAg37MA0GCSqGSIb3DQEBBQUAMIGbMQswCQYDVQQGEwJKUDEOMAwG" +
            "A1UECBMFVG9reW8xEDAOBgNVBAcTB0NodW8ta3UxETAPBgNVBAoTCEZyYW5rNERE" +
            "MRgwFgYDVQQLEw9XZWJDZXJ0IFN1cHBvcnQxGDAWBgNVBAMTD0ZyYW5rNEREIFdl" +
            "YiBDQTEjMCEGCSqGSIb3DQEJARYUc3VwcG9ydEBmcmFuazRkZC5jb20wHhcNMTIw" +
            "ODIyMDUyNzIzWhcNMTcwODIxMDUyNzIzWjBKMQswCQYDVQQGEwJKUDEOMAwGA1UE" +
            "CAwFVG9reW8xETAPBgNVBAoMCEZyYW5rNEREMRgwFgYDVQQDDA93d3cuZXhhbXBs" +
            "ZS5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMYBBrx5PlP0WNI/ZdzD" +
            "+6Pktmurn+F2kQYbtc7XQh8/LTBvCo+P6iZoLEmUA9e7EXLRxgU1CVqeAi7QcAn9" +
            "MwBlc8ksFJHB0rtf9pmf8Oza9E0Bynlq/4/Kb1x+d+AyhL7oK9tQwB24uHOueHi1" +
            "C/iVv8CSWKiYe6hzN1txYe8rAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAASPdjigJ" +
            "kXCqKWpnZ/Oc75EUcMi6HztaW8abUMlYXPIgkV2F7YanHOB7K4f7OOLjiz8DTPFf" +
            "jC9UeuErhaA/zzWi8ewMTFZW/WshOrm3fNvcMrMLKtH534JKvcdMg6qIdjTFINIr" +
            "evnAhf0cwULaebn+lMs8Pdl7y37+sfluVok=" +
            "-----END CERTIFICATE-----";

}
