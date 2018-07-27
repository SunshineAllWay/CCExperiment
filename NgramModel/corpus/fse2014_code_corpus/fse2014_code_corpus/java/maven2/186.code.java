package org.apache.maven.cli;
import java.util.Properties;
import junit.framework.TestCase;
public class MavenCliTest
    extends TestCase
{
    public void testGetExecutionProperties()
        throws Exception
    {
        System.setProperty( "test.property.1", "1.0" );
        System.setProperty( "test.property.2", "2.0" );
        Properties execProperties = new Properties();
        Properties userProperties = new Properties();
        MavenCli.populateProperties( ( new CLIManager() ).parse( new String[] {
            "-Dtest.property.2=2.1",
            "-Dtest.property.3=3.0" } ), execProperties, userProperties );
        System.out.println( "Execution properties:\n\n" );
        execProperties.list( System.out );
        System.out.println( "\n\nUser properties:\n\n" );
        userProperties.list( System.out );
        String envPath = execProperties.getProperty( "env.PATH" );
        String envPath2 = userProperties.getProperty( "env.PATH" );
        if ( envPath == null )
        {
            envPath = execProperties.getProperty( "env.Path" );
            envPath2 = userProperties.getProperty( "env.Path" );
        }
        assertNotNull( envPath );
        assertNull( envPath2 );
        assertEquals( "1.0", execProperties.getProperty( "test.property.1" ) );
        assertNull( userProperties.getProperty( "test.property.1" ) );
        assertEquals( "3.0", execProperties.getProperty( "test.property.3" ) );
        assertEquals( "3.0", userProperties.getProperty( "test.property.3" ) );
    }
    public void testGetBuildProperties()
        throws Exception
    {
        Properties properties = MavenCli.getBuildProperties();
        assertNotNull( properties.getProperty( "version" ) );
        assertNotNull( properties.getProperty( "buildNumber" ) );
        assertNotNull( properties.getProperty( "timestamp" ) );
        assertFalse( properties.getProperty( "version" ).equals( "${project.version}" ) );
        assertFalse( properties.getProperty( "buildNumber" ).equals( "${buildNumber}" ) );
        assertFalse( properties.getProperty( "timestamp" ).equals( "${timestamp}" ) );
    }
}
