package org.apache.maven.model;
import junit.framework.TestCase;
public class ActivationPropertyTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new ActivationProperty().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new ActivationProperty().equals( null ) );
        new ActivationProperty().equals( new ActivationProperty() );
    }
    public void testEqualsIdentity()
    {
        ActivationProperty thing = new ActivationProperty();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new ActivationProperty().toString() );
    }
}
