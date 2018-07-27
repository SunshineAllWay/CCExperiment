package org.apache.maven.model;
import junit.framework.TestCase;
public class CiManagementTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new CiManagement().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new CiManagement().equals( null ) );
        new CiManagement().equals( new CiManagement() );
    }
    public void testEqualsIdentity()
    {
        CiManagement thing = new CiManagement();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new CiManagement().toString() );
    }
}
