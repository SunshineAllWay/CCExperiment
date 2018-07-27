package org.apache.maven.model;
import junit.framework.TestCase;
public class PluginExecutionTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new PluginExecution().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new PluginExecution().equals( null ) );
        new PluginExecution().equals( new PluginExecution() );
    }
    public void testEqualsIdentity()
    {
        PluginExecution thing = new PluginExecution();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new PluginExecution().toString() );
    }
}
