package org.apache.log4j;
import junit.framework.TestCase;
public class LogManagerTest extends TestCase {
  public LogManagerTest(final String testName) {
    super(testName);
  }
  public void testDefaultConfigurationFile() {
     assertEquals("log4j.properties", LogManager.DEFAULT_CONFIGURATION_FILE);
  }
  public void testDefaultXmlConfigurationFile() {
     assertEquals("log4j.xml", LogManager.DEFAULT_XML_CONFIGURATION_FILE);
  }
  public void testDefaultConfigurationKey() {
     assertEquals("log4j.configuration", LogManager.DEFAULT_CONFIGURATION_KEY);
  }
  public void testConfiguratorClassKey() {
     assertEquals("log4j.configuratorClass", LogManager.CONFIGURATOR_CLASS_KEY);
  }
  public void testDefaultInitOverrideKey() {
     assertEquals("log4j.defaultInitOverride", LogManager.DEFAULT_INIT_OVERRIDE_KEY);
  }
}
