package org.apache.maven.model;
import junit.framework.TestCase;
public class ProfileTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Profile().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Profile().equals( null ) );
        new Profile().equals( new Profile() );
    }
    public void testEqualsIdentity()
    {
        Profile thing = new Profile();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Profile().toString() );
    }
}
