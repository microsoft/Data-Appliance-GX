package com.microsoft.dagx.security.azure;

import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.security.VaultEntry;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/vault")
public class AzureVaultAccessController {

    private final Vault vault;

    public AzureVaultAccessController(Vault vault) {
        this.vault = vault;
    }

    @GET
    public Response getSecret(@QueryParam("key") String key) {
        var value = vault.resolveSecret(key);
        return Response.status(Response.Status.OK).entity(value).build();
    }

    @POST
    public Response setSecret(VaultEntry entry) {
        var response= vault.storeSecret(entry.getKey(), entry.getValue());
        return response.success() ? Response.status(Response.Status.OK).build() :
                Response.status(Response.Status.BAD_REQUEST).entity(response.error()).build();
    }

    @DELETE
    public Response delSecret(@QueryParam("key") String key) {
        var response = vault.deleteSecret(key);
        return response.success() ? Response.status(Response.Status.OK).build() :
                Response.status(Response.Status.BAD_REQUEST).entity(response.error()).build();
    }

}
