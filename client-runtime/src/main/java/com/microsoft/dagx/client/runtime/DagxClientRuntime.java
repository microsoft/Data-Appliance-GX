/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.client.runtime;

import com.microsoft.dagx.monitor.MonitorProvider;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.system.ServiceExtension;
import com.microsoft.dagx.spi.types.TypeManager;
import com.microsoft.dagx.system.DefaultServiceExtensionContext;

import java.util.List;
import java.util.ListIterator;

import static com.microsoft.dagx.system.ExtensionLoader.*;

public class DagxClientRuntime {
    private Monitor monitor;
    private TypeManager typeManager;
    private List<ServiceExtension> serviceExtensions;
    private DefaultServiceExtensionContext context;

    private DagxClientRuntime() {
    }

    public void start() {
        MonitorProvider.setInstance(monitor);

        context = new DefaultServiceExtensionContext(typeManager, monitor);
        context.initialize();

        try {
            loadVault(context);

            serviceExtensions = context.loadServiceExtensions();

            bootServiceExtensions(serviceExtensions, context);
            monitor.debug("Client runtime started");
        } catch (Exception e) {
            monitor.severe("Error booting runtime", e);   // do not stop the process as the client may be embedded
        }
    }

    public void shutdown() {
        ListIterator<ServiceExtension> iter = serviceExtensions.listIterator(serviceExtensions.size());
        while (iter.hasPrevious()) {
            iter.previous().shutdown();
        }
        monitor.debug("Client runtime shutdown complete");
    }

    public TypeManager getTypeManager() {
        return typeManager;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public <T> T getService(Class<T> type) {
        return context.getService(type);
    }

    public static class Builder {
        private final DagxClientRuntime runtime;
        private boolean immutable;

        private Builder() {
            runtime = new DagxClientRuntime();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder monitor(Monitor monitor) {
            checkImmutable();
            runtime.monitor = monitor;
            return this;
        }

        public Builder typeManager(TypeManager typeManager) {
            checkImmutable();
            runtime.typeManager = typeManager;
            return this;
        }

        public DagxClientRuntime build() {
            checkImmutable();
            immutable = true;

            if (runtime.monitor == null) {
                runtime.monitor = loadMonitor();
            }
            if (runtime.typeManager == null) {
                runtime.typeManager = new TypeManager();
            }

            return runtime;
        }

        private void checkImmutable() {
            if (immutable) {
                throw new IllegalStateException("Runtime has already been built");
            }
        }
    }
}
