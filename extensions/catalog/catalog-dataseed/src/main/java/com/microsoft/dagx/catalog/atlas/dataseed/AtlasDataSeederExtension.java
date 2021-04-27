package com.microsoft.dagx.catalog.atlas.dataseed;

import com.microsoft.dagx.catalog.atlas.metadata.AtlasApi;
import com.microsoft.dagx.catalog.atlas.metadata.AtlasExtension;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.system.ServiceExtension;
import com.microsoft.dagx.spi.system.ServiceExtensionContext;
import org.apache.atlas.model.typedef.AtlasTypesDef;

import java.util.*;


public class AtlasDataSeederExtension implements ServiceExtension {

    private final boolean shouldCleanup;
    private Monitor monitor;
    private AtlasTypesDef classificationTypes;
    List<AtlasTypesDef> entityTypes = new ArrayList<>();
    private List<String> entityGuids;
    private AtlasDataSeeder dataSeeder;

    public AtlasDataSeederExtension() {
        shouldCleanup = true;

    }

    public AtlasDataSeederExtension(boolean shouldCleanup) {
        this.shouldCleanup = shouldCleanup;
    }

    @Override
    public Set<String> requires() {
        return Set.of(AtlasExtension.ATLAS_FEATURE);
    }


    @Override
    public LoadPhase phase() {
        return LoadPhase.PRIMORDIAL;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();
        dataSeeder = new AtlasDataSeeder(context.getService(AtlasApi.class));


        monitor.info("Initialized Atlas Data Seeder");
    }


    @Override
    public void start() {
        monitor.info("Starting to seed data to Atlas");
        try {
            monitor.debug("Create Classifications");
            classificationTypes = dataSeeder.createClassifications();
        } catch (DagxException e) {
            monitor.severe("Error creating classifications", e);
        }

        try {
            monitor.debug("Create TypeDefs");
            entityTypes = dataSeeder.createTypedefs();
        } catch (DagxException e) {
            monitor.severe("Error creating TypeDefs", e);
        }

        try {
            monitor.debug("Create Entities");
            entityGuids = dataSeeder.createEntities();
        } catch (DagxException e) {
            monitor.severe("Error creating Entities", e);
        }

        monitor.info("Done seeding data to Atlas");
    }


    @Override
    public void shutdown() {
        if (shouldCleanup) {
            monitor.info("Cleaning up Entities");
            dataSeeder.deleteEntities(entityGuids);
            monitor.info("Cleaning up Classifications");
            dataSeeder.deleteClassificationTypes(classificationTypes);
            monitor.info("Cleaning up Entity Types");
            dataSeeder.deleteEntityTypes(entityTypes);
        }
    }

}
