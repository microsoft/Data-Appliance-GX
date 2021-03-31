package com.microsoft.dagx.system;

import com.microsoft.dagx.monitor.ConsoleMonitor;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.monitor.MultiplexingMonitor;
import com.microsoft.dagx.spi.system.MonitorExtension;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionLoaderTest {

    @Test
    void loadMonitor_whenSingleMonitorExtension() {
        var mockedMonitor= (Monitor)EasyMock.mock(Monitor.class);

        var exts= new ArrayList<MonitorExtension>();
        exts.add(() -> mockedMonitor);

        var monitor = ExtensionLoader.loadMonitor(exts);

        assertEquals(mockedMonitor, monitor);
    }

    @Test
    void loadMonitor_whenMultipleMonitorExtensions() {
        var mockedMonitor= (Monitor)EasyMock.mock(Monitor.class);

        var exts= new ArrayList<MonitorExtension>();
        exts.add(() -> mockedMonitor);
        exts.add(ConsoleMonitor::new);

        var monitor= ExtensionLoader.loadMonitor(exts);

        assertTrue(monitor instanceof MultiplexingMonitor);
    }

    @Test
    void loadMonitor_whenNoMonitorExtension() {

       var monitor= ExtensionLoader.loadMonitor(new ArrayList<>());

       assertTrue(monitor instanceof ConsoleMonitor);

    }
}
