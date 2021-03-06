package org.apache.maven.model;
import junit.framework.TestCase;
public class SiteTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Site().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Site().equals( null ) );
        new Site().equals( new Site() );
    }
    public void testEqualsIdentity()
    {
        Site thing = new Site();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Site().toString() );
    }
}
