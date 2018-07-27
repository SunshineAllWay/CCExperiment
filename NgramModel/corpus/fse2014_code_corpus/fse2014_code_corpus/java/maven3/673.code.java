package org.apache.maven.model;
import junit.framework.TestCase;
public class ExclusionTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Exclusion().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Exclusion().equals( null ) );
        new Exclusion().equals( new Exclusion() );
    }
    public void testEqualsIdentity()
    {
        Exclusion thing = new Exclusion();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Exclusion().toString() );
    }
}
