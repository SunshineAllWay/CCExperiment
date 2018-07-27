package org.apache.maven.rtinfo.internal;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.PlexusTestCase;
public class DefaultRuntimeInformationTest
    extends PlexusTestCase
{
    public void testGetMavenVersion()
        throws Exception
    {
        RuntimeInformation rtInfo = lookup( RuntimeInformation.class );
        String mavenVersion = rtInfo.getMavenVersion();
        assertNotNull( mavenVersion );
        assertTrue( mavenVersion.length() > 0 );
    }
    public void testIsMavenVersion()
        throws Exception
    {
        RuntimeInformation rtInfo = lookup( RuntimeInformation.class );
        assertTrue( rtInfo.isMavenVersion( "2.0" ) );
        assertFalse( rtInfo.isMavenVersion( "9.9" ) );
        assertTrue( rtInfo.isMavenVersion( "[2.0.11,2.1.0),[3.0,)" ) );
        assertFalse( rtInfo.isMavenVersion( "[9.0,)" ) );
        try
        {
            rtInfo.isMavenVersion( "[3.0," );
            fail( "Bad version range wasn't rejected" );
        }
        catch ( IllegalArgumentException e )
        {
            assertTrue( true );
        }
        try
        {
            rtInfo.isMavenVersion( "" );
            fail( "Bad version range wasn't rejected" );
        }
        catch ( IllegalArgumentException e )
        {
            assertTrue( true );
        }
        try
        {
            rtInfo.isMavenVersion( null );
            fail( "Bad version range wasn't rejected" );
        }
        catch ( IllegalArgumentException e )
        {
            assertTrue( true );
        }
    }
}
