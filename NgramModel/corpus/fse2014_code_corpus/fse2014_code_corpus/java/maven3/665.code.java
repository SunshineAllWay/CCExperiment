package org.apache.maven.model;
import junit.framework.TestCase;
public class BuildTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Build().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Build().equals( null ) );
        new Build().equals( new Build() );
    }
    public void testEqualsIdentity()
    {
        Build thing = new Build();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Build().toString() );
    }
}
