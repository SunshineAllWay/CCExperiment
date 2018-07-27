package org.apache.maven.model;
import junit.framework.TestCase;
public class ReportPluginTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new ReportPlugin().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new ReportPlugin().equals( null ) );
        new ReportPlugin().equals( new ReportPlugin() );
    }
    public void testEqualsIsKey()
    {
        ReportPlugin thing = new ReportPlugin();
        thing.setGroupId( "groupId" );
        thing.setArtifactId( "artifactId" );
        thing.setVersion( "1.0" );
        ReportPlugin thing2 = new ReportPlugin();
        thing2.setGroupId( "groupId" );
        thing2.setArtifactId( "artifactId" );
        thing2.setVersion( "2.0" );
        assertEquals( thing2, thing );
        ReportPlugin thing3 = new ReportPlugin();
        thing3.setGroupId( "otherGroupId" );
        thing3.setArtifactId( "artifactId" );
        assertFalse( thing3.equals( thing ) );
    }
    public void testHashcodeIsId()
    {
        ReportPlugin thing = new ReportPlugin();
        thing.setGroupId( "groupId" );
        thing.setArtifactId( "artifactId" );
        thing.setVersion( "1.0" );
        ReportPlugin thing2 = new ReportPlugin();
        thing2.setGroupId( "groupId" );
        thing2.setArtifactId( "artifactId" );
        thing2.setVersion( "2.0" );
        assertEquals( thing2.hashCode(), thing.hashCode() );
        ReportPlugin thing3 = new ReportPlugin();
        thing3.setGroupId( "otherGroupId" );
        thing3.setArtifactId( "artifactId" );
        assertFalse( thing3.hashCode() == thing.hashCode() );
    }
    public void testEqualsIdentity()
    {
        ReportPlugin thing = new ReportPlugin();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new ReportPlugin().toString() );
    }
}
