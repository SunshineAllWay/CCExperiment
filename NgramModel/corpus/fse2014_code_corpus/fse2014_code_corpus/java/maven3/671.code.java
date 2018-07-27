package org.apache.maven.model;
import junit.framework.TestCase;
public class DeveloperTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Developer().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Developer().equals( null ) );
        new Developer().equals( new Developer() );
    }
    public void testEqualsIdentity()
    {
        Developer thing = new Developer();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Developer().toString() );
    }
}
