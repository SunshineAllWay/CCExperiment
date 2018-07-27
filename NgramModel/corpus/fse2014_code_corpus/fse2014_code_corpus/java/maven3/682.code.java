package org.apache.maven.model;
import junit.framework.TestCase;
public class PluginConfigurationTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new PluginConfiguration().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new PluginConfiguration().equals( null ) );
        new PluginConfiguration().equals( new PluginConfiguration() );
    }
    public void testEqualsIdentity()
    {
        PluginConfiguration thing = new PluginConfiguration();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new PluginConfiguration().toString() );
    }
}
