package org.apache.maven.model;
import junit.framework.TestCase;
public class ActivationTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Activation().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Activation().equals( null ) );
        new Activation().equals( new Activation() );
    }
    public void testEqualsIdentity()
    {
        Activation thing = new Activation();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Activation().toString() );
    }
}
