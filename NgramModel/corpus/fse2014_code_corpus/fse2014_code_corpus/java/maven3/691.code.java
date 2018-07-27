package org.apache.maven.model;
import junit.framework.TestCase;
public class ReportPluginTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new ReportPlugin().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new ReportPlugin().equals( null ) );
        new ReportPlugin().equals( new ReportPlugin() );
    }
    public void testEqualsIdentity()
    {
        ReportPlugin thing = new ReportPlugin();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new ReportPlugin().toString() );
    }
}
