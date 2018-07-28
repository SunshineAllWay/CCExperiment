package org.apache.maven.model;
import junit.framework.TestCase;
public class RelocationTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Relocation().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Relocation().equals( null ) );
        new Relocation().equals( new Relocation() );
    }
    public void testEqualsIdentity()
    {
        Relocation thing = new Relocation();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Relocation().toString() );
    }
}
