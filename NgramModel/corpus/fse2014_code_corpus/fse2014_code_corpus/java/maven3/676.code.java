package org.apache.maven.model;
import junit.framework.TestCase;
public class LicenseTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new License().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new License().equals( null ) );
        new License().equals( new License() );
    }
    public void testEqualsIdentity()
    {
        License thing = new License();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new License().toString() );
    }
}
