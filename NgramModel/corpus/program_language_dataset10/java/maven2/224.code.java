package org.apache.maven.model;
import junit.framework.TestCase;
public class PrerequisitesTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Prerequisites().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Prerequisites().equals( null ) );
        new Prerequisites().equals( new Prerequisites() );
    }
    public void testEqualsIdentity()
    {
        Prerequisites thing = new Prerequisites();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Prerequisites().toString() );
    }
}
