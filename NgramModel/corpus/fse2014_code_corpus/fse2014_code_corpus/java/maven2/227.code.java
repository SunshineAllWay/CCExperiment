package org.apache.maven.model;
import junit.framework.TestCase;
public class ReportingTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Reporting().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Reporting().equals( null ) );
        new Reporting().equals( new Reporting() );
    }
    public void testEqualsIdentity()
    {
        Reporting thing = new Reporting();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Reporting().toString() );
    }
}
