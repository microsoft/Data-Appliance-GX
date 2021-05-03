package com.microsoft.dagx.schema.azure;

import com.microsoft.dagx.schema.Schema;
import com.microsoft.dagx.schema.SchemaAttribute;

public class AzureSchema extends Schema {


    @Override
    protected void addAttributes() {
        attributes.add(new SchemaAttribute("account", true));
        attributes.add(new SchemaAttribute("blobname", true));
        attributes.add(new SchemaAttribute("container", true));
        attributes.add(new SchemaAttribute("type", true));
        attributes.add(new SchemaAttribute("keyName", true));
    }

    @Override
    public String getName() {
        return "AzureStorage";
    }

}
