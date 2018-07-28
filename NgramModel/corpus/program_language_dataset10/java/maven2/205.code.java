package org.apache.maven.model;
import junit.framework.TestCase;
public class DependencyManagementTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new DependencyManagement().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new DependencyManagement().equals( null ) );
        new DependencyManagement().equals( new DependencyManagement() );
    }
    public void testEqualsIdentity()
    {
        DependencyManagement thing = new DependencyManagement();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new DependencyManagement().toString() );
    }
}
