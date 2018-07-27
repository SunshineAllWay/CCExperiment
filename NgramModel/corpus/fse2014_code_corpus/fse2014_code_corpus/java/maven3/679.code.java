package org.apache.maven.model;
import junit.framework.TestCase;
public class NotifierTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Notifier().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Notifier().equals( null ) );
        new Notifier().equals( new Notifier() );
    }
    public void testEqualsIdentity()
    {
        Notifier thing = new Notifier();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Notifier().toString() );
    }
}
