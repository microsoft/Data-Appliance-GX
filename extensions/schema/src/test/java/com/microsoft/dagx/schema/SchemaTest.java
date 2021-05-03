package com.microsoft.dagx.schema;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaTest {

    @Test
    void getAttributes(){
        Schema schema = createSchema();
        assertThat(schema.getAttributes()).hasSize(1);
    }

    @Test
    void getAttributes_addWithSameName(){
        var schema= createSchema();
        var hasAdded= schema.getAttributes().add(new SchemaAttribute("test", false));
        assertThat(hasAdded).isTrue();
    }

    @Test
    void getRequiredAttributes(){
        var schema= createSchema();
        schema.getAttributes().add(new SchemaAttribute("foo", true));
        schema.getAttributes().add(new SchemaAttribute("bar", false));

        assertThat(schema.getRequiredAttributes()).hasSize(2)
                .allMatch(sa -> sa.getName().equals("foo") || sa.getName().equals("test"));
    }
    @NotNull
    private Schema createSchema() {
        Schema schema= new Schema() {
            @Override
            protected void addAttributes() {
                attributes.add(new SchemaAttribute("test", true));
            }

            @Override
            public String getName() {
                return "testSchema";
            }
        };
        return schema;
    }

}
