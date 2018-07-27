package org.apache.maven.profiles;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationOS;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.profiles.activation.ProfileActivationException;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusTestCase;
public class DefaultProfileManagerTest
    extends PlexusTestCase
{
    public void testShouldActivateNonExistantSettingsProfile()
        throws ProfileActivationException
    {
        Settings settings = new Settings();
        settings.addActiveProfile( "testProfile" );
        ProfileManager profileManager = new DefaultProfileManager( getContainer(), settings );
        List activeIds = profileManager.getExplicitlyActivatedIds();
        assertNotNull( activeIds );
        assertEquals( 1, activeIds.size() );
        assertEquals( "testProfile", activeIds.get( 0 ) );
        List activeProfiles = profileManager.getActiveProfiles();
        assertTrue( activeProfiles.isEmpty() );
        Profile p = new Profile();
        p.setId( "testProfile" );
        profileManager.addProfile( p );
        activeProfiles = profileManager.getActiveProfiles();
        assertNotNull( activeProfiles );
        assertEquals( 1, activeProfiles.size() );
        assertEquals( "testProfile", ( (Profile) activeProfiles.get( 0 ) ).getId() );
    }
    public void testShouldActivateDefaultProfile()
        throws ProfileActivationException
    {
        Profile notActivated = new Profile();
        notActivated.setId( "notActivated" );
        Activation nonActivation = new Activation();
        nonActivation.setJdk( "19.2" );
        notActivated.setActivation( nonActivation );
        Profile defaultActivated = new Profile();
        defaultActivated.setId( "defaultActivated" );
        Activation defaultActivation = new Activation();
        defaultActivation.setActiveByDefault( true );
        defaultActivated.setActivation( defaultActivation );
        ProfileManager profileManager = new DefaultProfileManager( getContainer() );
        profileManager.addProfile( notActivated );
        profileManager.addProfile( defaultActivated );
        List active = profileManager.getActiveProfiles();
        assertNotNull( active );
        assertEquals( 1, active.size() );
        assertEquals( "defaultActivated", ( (Profile) active.get( 0 ) ).getId() );
    }
    public void testShouldNotActivateDefaultProfile()
        throws ProfileActivationException
    {
        Profile syspropActivated = new Profile();
        syspropActivated.setId( "syspropActivated" );
        Activation syspropActivation = new Activation();
        ActivationProperty syspropProperty = new ActivationProperty();
        syspropProperty.setName( "java.version" );
        syspropActivation.setProperty( syspropProperty );
        syspropActivated.setActivation( syspropActivation );
        Profile defaultActivated = new Profile();
        defaultActivated.setId( "defaultActivated" );
        Activation defaultActivation = new Activation();
        defaultActivation.setActiveByDefault( true );
        defaultActivated.setActivation( defaultActivation );
        ProfileManager profileManager = new DefaultProfileManager( getContainer() );
        profileManager.addProfile( syspropActivated );
        profileManager.addProfile( defaultActivated );
        List active = profileManager.getActiveProfiles();
        assertNotNull( active );
        assertEquals( 1, active.size() );
        assertEquals( "syspropActivated", ( (Profile) active.get( 0 ) ).getId() );
    }
    public void testShouldNotActivateReversalOfPresentSystemProperty()
        throws ProfileActivationException
    {
        Profile syspropActivated = new Profile();
        syspropActivated.setId( "syspropActivated" );
        Activation syspropActivation = new Activation();
        ActivationProperty syspropProperty = new ActivationProperty();
        syspropProperty.setName( "!java.version" );
        syspropActivation.setProperty( syspropProperty );
        syspropActivated.setActivation( syspropActivation );
        ProfileManager profileManager = new DefaultProfileManager( getContainer() );
        profileManager.addProfile( syspropActivated );
        List active = profileManager.getActiveProfiles();
        assertNotNull( active );
        assertEquals( 0, active.size() );
    }
    public void testShouldOverrideAndActivateInactiveProfile()
        throws ProfileActivationException
    {
        Profile syspropActivated = new Profile();
        syspropActivated.setId( "syspropActivated" );
        Activation syspropActivation = new Activation();
        ActivationProperty syspropProperty = new ActivationProperty();
        syspropProperty.setName( "!java.version" );
        syspropActivation.setProperty( syspropProperty );
        syspropActivated.setActivation( syspropActivation );
        ProfileManager profileManager = new DefaultProfileManager( getContainer() );
        profileManager.addProfile( syspropActivated );
        profileManager.explicitlyActivate( "syspropActivated" );
        List active = profileManager.getActiveProfiles();
        assertNotNull( active );
        assertEquals( 1, active.size() );
        assertEquals( "syspropActivated", ( (Profile) active.get( 0 ) ).getId() );
    }
    public void testShouldOverrideAndDeactivateActiveProfile()
        throws ProfileActivationException
    {
        Profile syspropActivated = new Profile();
        syspropActivated.setId( "syspropActivated" );
        Activation syspropActivation = new Activation();
        ActivationProperty syspropProperty = new ActivationProperty();
        syspropProperty.setName( "java.version" );
        syspropActivation.setProperty( syspropProperty );
        syspropActivated.setActivation( syspropActivation );
        ProfileManager profileManager = new DefaultProfileManager( getContainer() );
        profileManager.addProfile( syspropActivated );
        profileManager.explicitlyDeactivate( "syspropActivated" );
        List active = profileManager.getActiveProfiles();
        assertNotNull( active );
        assertEquals( 0, active.size() );
    }
    public void testOsActivationProfile()
        throws ProfileActivationException
    {
        Profile osActivated = new Profile();
        osActivated.setId( "os-profile" );
        Activation osActivation = new Activation();
        ActivationOS activationOS = new ActivationOS();
        activationOS.setName( "!dddd" );
        osActivation.setOs( activationOS );
        osActivated.setActivation( osActivation );
        ProfileManager profileManager = new DefaultProfileManager( getContainer() );
        profileManager.addProfile( osActivated );
        List active = profileManager.getActiveProfiles();
        assertNotNull( active );
        assertEquals( 1, active.size() );
    }
    public void testFileActivationProfile()
        throws ProfileActivationException, IOException
    {
        Profile osActivated = new Profile();
        osActivated.setId( "os-profile" );
        Activation fileActivation = new Activation();
        org.apache.maven.model.ActivationFile activationFile = new org.apache.maven.model.ActivationFile();
        File f = File.createTempFile( "activationTest", null );
        f.createNewFile();
        activationFile.setExists( f.getAbsolutePath() );
        fileActivation.setFile( activationFile );
        osActivated.setActivation( fileActivation );
        ProfileManager profileManager = new DefaultProfileManager( getContainer() );
        profileManager.addProfile( osActivated );
        List active = profileManager.getActiveProfiles();
        assertNotNull( active );
        assertEquals( 1, active.size() );
        try
        {
            f.delete();
        }
        catch (Exception e)
        {
        }
    }
}
