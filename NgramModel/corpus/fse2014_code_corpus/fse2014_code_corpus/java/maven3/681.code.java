package org.apache.maven.model;
import junit.framework.TestCase;
public class ParentTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Parent().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Parent().equals( null ) );
        new Parent().equals( new Parent() );
    }
    public void testEqualsIdentity()
    {
        Parent thing = new Parent();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Parent().toString() );
    }
}
