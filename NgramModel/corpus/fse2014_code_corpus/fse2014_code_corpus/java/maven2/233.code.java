package org.apache.maven.model;
import junit.framework.TestCase;
public class ScmTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Scm().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Scm().equals( null ) );
        new Scm().equals( new Scm() );
    }
    public void testEqualsIdentity()
    {
        Scm thing = new Scm();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Scm().toString() );
    }
}
