package org.apache.maven.model;
import junit.framework.TestCase;
public class ActivationFileTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new ActivationFile().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new ActivationFile().equals( null ) );
        new ActivationFile().equals( new ActivationFile() );
    }
    public void testEqualsIdentity()
    {
        ActivationFile thing = new ActivationFile();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new ActivationFile().toString() );
    }
}
