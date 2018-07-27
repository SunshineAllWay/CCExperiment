package org.apache.maven.model;
import junit.framework.TestCase;
public class ResourceTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Resource().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Resource().equals( null ) );
        new Resource().equals( new Resource() );
    }
    public void testEqualsIdentity()
    {
        Resource thing = new Resource();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Resource().toString() );
    }
}
