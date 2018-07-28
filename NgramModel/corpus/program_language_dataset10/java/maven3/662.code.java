package org.apache.maven.model;
import junit.framework.TestCase;
public class ActivationOSTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new ActivationOS().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new ActivationOS().equals( null ) );
        new ActivationOS().equals( new ActivationOS() );
    }
    public void testEqualsIdentity()
    {
        ActivationOS thing = new ActivationOS();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new ActivationOS().toString() );
    }
}
