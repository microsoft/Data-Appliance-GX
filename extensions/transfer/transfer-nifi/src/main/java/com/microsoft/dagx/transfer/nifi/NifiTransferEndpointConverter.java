package com.microsoft.dagx.transfer.nifi;

import com.microsoft.dagx.schema.Schema;
import com.microsoft.dagx.schema.SchemaRegistry;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.types.domain.transfer.DataAddress;

public class NifiTransferEndpointConverter {
    private SchemaRegistry schemaRegistry;
    private final Vault vault;

    public NifiTransferEndpointConverter(SchemaRegistry registry, Vault vault){

        this.schemaRegistry = registry;
        this.vault = vault;
    }
    NifiTransferEndpoint convert(DataAddress dataAddress){
        var type = dataAddress.getType();
        var schema = schemaRegistry.getSchema(type);

        if(schema == null )
            throw new NifiTransferException("No schema is registered for type "+type);

        validate(dataAddress, schema);

        return NifiTransferEndpoint.NifiTransferEndpointBuilder.newInstance()
                .type(type)
                .key(vault.resolveSecret(dataAddress.getKeyName()))
                .properties(dataAddress.getProperties())
                .build();
    }

    private void validate(DataAddress dataAddress, Schema schema) {
        //todo: call validators, throw exception
    }
}
