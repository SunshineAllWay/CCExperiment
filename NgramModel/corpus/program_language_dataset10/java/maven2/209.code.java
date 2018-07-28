package org.apache.maven.model;
import junit.framework.TestCase;
public class DistributionManagementTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new DistributionManagement().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new DistributionManagement().equals( null ) );
        new DistributionManagement().equals( new DistributionManagement() );
    }
    public void testEqualsIdentity()
    {
        DistributionManagement thing = new DistributionManagement();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new DistributionManagement().toString() );
    }
}
