package org.apache.maven.model;
import junit.framework.TestCase;
public class ContributorTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Contributor().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Contributor().equals( null ) );
        new Contributor().equals( new Contributor() );
    }
    public void testEqualsIdentity()
    {
        Contributor thing = new Contributor();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Contributor().toString() );
    }
}
