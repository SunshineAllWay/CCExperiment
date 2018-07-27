package org.apache.maven.model;
import junit.framework.TestCase;
public class DependencyTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Dependency().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Dependency().equals( null ) );
        new Dependency().equals( new Dependency() );
    }
    public void testEqualsIdentity()
    {
        Dependency thing = new Dependency();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Dependency().toString() );
    }
}
