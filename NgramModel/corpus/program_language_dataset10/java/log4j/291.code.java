package org.apache.log4j;
import junit.framework.TestCase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableRenderer;
import org.apache.log4j.spi.ThrowableRendererSupport;
import org.apache.log4j.varia.LevelRangeFilter;
public class PropertyConfiguratorTest extends TestCase {
    public PropertyConfiguratorTest(final String testName) {
        super(testName);
    }
    public void testBadUnicodeEscape() throws IOException {
        String fileName = "output/badescape.properties";
        FileWriter writer = new FileWriter(fileName);
        writer.write("log4j.rootLogger=\\uXX41");
        writer.close();
        PropertyConfigurator.configure(fileName);
        File file = new File(fileName);
        assertTrue(file.delete()) ;
        assertFalse(file.exists());
    }
        public void testURL() throws IOException {
        File file = new File("output/unclosed.properties");
        FileWriter writer = new FileWriter(file);
        writer.write("log4j.rootLogger=debug");
        writer.close();
        URL url = file.toURL();
        PropertyConfigurator.configure(url);
        assertTrue(file.delete());
        assertFalse(file.exists());
    }
        public void testURLBadEscape() throws IOException {
        File file = new File("output/urlbadescape.properties");
        FileWriter writer = new FileWriter(file);
        writer.write("log4j.rootLogger=\\uXX41");
        writer.close();
        URL url = file.toURL();
        PropertyConfigurator.configure(url);
        assertTrue(file.delete());
        assertFalse(file.exists());
    }
    public void testJarURL() throws IOException {
        File dir = new File("output");
        dir.mkdirs();
        File file = new File("output/properties.jar");
        ZipOutputStream zos =
            new ZipOutputStream(new FileOutputStream(file));
        zos.putNextEntry(new ZipEntry(LogManager.DEFAULT_CONFIGURATION_FILE));
        zos.write("log4j.rootLogger=debug".getBytes());
        zos.closeEntry();
        zos.close();
        URL url = new URL("jar:" + file.toURL() + "!/" +
                LogManager.DEFAULT_CONFIGURATION_FILE);
        PropertyConfigurator.configure(url);
        assertTrue(file.delete());
        assertFalse(file.exists());
    }
    public void testReset() {
        VectorAppender appender = new VectorAppender();
        appender.setName("A1");
        Logger.getRootLogger().addAppender(appender);
        Properties props = new Properties();
        props.put("log4j.reset", "true");
        PropertyConfigurator.configure(props);
        assertNull(Logger.getRootLogger().getAppender("A1"));
        LogManager.resetConfiguration();
    }
    public static class RollingPolicy implements OptionHandler {
        private boolean activated = false;
        public RollingPolicy() {
        }
        public void activateOptions() {
            activated = true;
        }
        public final boolean isActivated() {
            return activated;
        }
    }
    public static final class FixedWindowRollingPolicy extends RollingPolicy {
        private String activeFileName;
        private String fileNamePattern;
        private int minIndex;
        public FixedWindowRollingPolicy() {
            minIndex = -1;
        }
        public String getActiveFileName() {
            return activeFileName;
        }
        public void setActiveFileName(final String val) {
            activeFileName = val;
        }
        public String getFileNamePattern() {
            return fileNamePattern;
        }
        public void setFileNamePattern(final String val) {
            fileNamePattern = val;
        }
        public int getMinIndex() {
            return minIndex;
        }
        public void setMinIndex(final int val) {
            minIndex = val;
        }
    }
    public static class TriggeringPolicy implements OptionHandler {
        private boolean activated = false;
        public TriggeringPolicy() {
        }
        public void activateOptions() {
            activated = true;
        }
        public final boolean isActivated() {
            return activated;
        }
    }
    public static final class FilterBasedTriggeringPolicy extends TriggeringPolicy {
        private Filter filter;
        public FilterBasedTriggeringPolicy() {
        }
        public void setFilter(final Filter val) {
             filter = val;
        }
        public Filter getFilter() {
            return filter;
        }
    }
    public static final class RollingFileAppender extends AppenderSkeleton {
        private RollingPolicy rollingPolicy;
        private TriggeringPolicy triggeringPolicy;
        private boolean append;
        public RollingFileAppender() {
        }
        public RollingPolicy getRollingPolicy() {
            return rollingPolicy;
        }
        public void setRollingPolicy(final RollingPolicy policy) {
            rollingPolicy = policy;
        }
        public TriggeringPolicy getTriggeringPolicy() {
            return triggeringPolicy;
        }
        public void setTriggeringPolicy(final TriggeringPolicy policy) {
            triggeringPolicy = policy;
        }
        public boolean getAppend() {
            return append;
        }
        public void setAppend(boolean val) {
            append = val;
        }
        public void close() {
        }
        public boolean requiresLayout() {
            return true;
        }
        public void append(final LoggingEvent event) {
        }
    }
    public void testNested() {
        PropertyConfigurator.configure("input/filter1.properties");
        RollingFileAppender rfa = (RollingFileAppender)
                Logger.getLogger("org.apache.log4j.PropertyConfiguratorTest")
                   .getAppender("ROLLING");
        FixedWindowRollingPolicy rollingPolicy = (FixedWindowRollingPolicy) rfa.getRollingPolicy();
        assertEquals("filterBase-test1.log", rollingPolicy.getActiveFileName());
        assertEquals("filterBased-test1.%i", rollingPolicy.getFileNamePattern());
        assertEquals(0, rollingPolicy.getMinIndex());
        assertTrue(rollingPolicy.isActivated());
        FilterBasedTriggeringPolicy triggeringPolicy =
                (FilterBasedTriggeringPolicy) rfa.getTriggeringPolicy();
        LevelRangeFilter filter = (LevelRangeFilter) triggeringPolicy.getFilter();
        assertTrue(Level.INFO.equals(filter.getLevelMin()));
        LogManager.resetConfiguration();
    }
    public static class MockThrowableRenderer implements ThrowableRenderer, OptionHandler {
        private boolean activated = false;
        private boolean showVersion = true;
        public MockThrowableRenderer() {
        }
        public void activateOptions() {
            activated = true;
        }
        public boolean isActivated() {
            return activated;
        }
        public String[] doRender(final Throwable t) {
            return new String[0];
        }
        public void setShowVersion(boolean v) {
            showVersion = v;
        }
        public boolean getShowVersion() {
            return showVersion;
        }
    }
    public void testThrowableRenderer() {
        Properties props = new Properties();
        props.put("log4j.throwableRenderer", "org.apache.log4j.PropertyConfiguratorTest$MockThrowableRenderer");
        props.put("log4j.throwableRenderer.showVersion", "false");
        PropertyConfigurator.configure(props);
        ThrowableRendererSupport repo = (ThrowableRendererSupport) LogManager.getLoggerRepository();
        MockThrowableRenderer renderer = (MockThrowableRenderer) repo.getThrowableRenderer();
        LogManager.resetConfiguration();
        assertNotNull(renderer);
        assertEquals(true, renderer.isActivated());
        assertEquals(false, renderer.getShowVersion());
    }
}
