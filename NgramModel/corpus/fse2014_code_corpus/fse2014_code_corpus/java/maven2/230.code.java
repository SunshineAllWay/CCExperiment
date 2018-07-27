package org.apache.maven.model;
import junit.framework.TestCase;
public class RepositoryPolicyTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new RepositoryPolicy().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new RepositoryPolicy().equals( null ) );
        new RepositoryPolicy().equals( new RepositoryPolicy() );
    }
    public void testEqualsIdentity()
    {
        RepositoryPolicy thing = new RepositoryPolicy();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new RepositoryPolicy().toString() );
    }
}
