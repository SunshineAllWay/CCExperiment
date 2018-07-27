package org.apache.maven.model;
import junit.framework.TestCase;
public class ModelTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Model().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Model().equals( null ) );
        new Model().equals( new Model() );
    }
    public void testEqualsIdentity()
    {
        Model thing = new Model();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Model().toString() );
    }
}
