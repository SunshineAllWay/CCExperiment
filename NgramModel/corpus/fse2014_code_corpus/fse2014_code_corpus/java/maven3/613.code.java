package org.apache.maven.settings;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Profile;
import org.apache.maven.project.DefaultProjectBuilder;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.harness.PomTestWrapper;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;
public class PomConstructionWithSettingsTest
    extends PlexusTestCase
{
    private static final String BASE_DIR = "src/test";
    private static final String BASE_POM_DIR = BASE_DIR + "/resources-settings";
    private DefaultProjectBuilder projectBuilder;
    private RepositorySystem repositorySystem;
    private File testDirectory;
    protected void setUp()
        throws Exception
    {
        testDirectory = new File( getBasedir(), BASE_POM_DIR );
        projectBuilder = (DefaultProjectBuilder) lookup( ProjectBuilder.class );
        repositorySystem = lookup( RepositorySystem.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        projectBuilder = null;
        super.tearDown();
    }
    public void testSettingsNoPom() throws Exception
    {
        PomTestWrapper pom = buildPom( "settings-no-pom" );
        assertEquals( "local-profile-prop-value", pom.getValue( "properties/local-profile-prop" ) );
    }
    public void testPomAndSettingsInterpolation() throws Exception
    {
        PomTestWrapper pom = buildPom( "test-pom-and-settings-interpolation" );
        assertEquals( "applied", pom.getValue( "properties/settingsProfile" ) );
        assertEquals( "applied", pom.getValue( "properties/pomProfile" ) );
        assertEquals( "settings", pom.getValue( "properties/pomVsSettings" ) );
        assertEquals( "settings", pom.getValue( "properties/pomVsSettingsInterpolated" ) );
    }
    public void testRepositories() throws Exception
    {
        PomTestWrapper pom = buildPom( "repositories" );
        assertEquals( "maven-core-it-0", pom.getValue( "repositories[1]/id" ) );
    }
    private PomTestWrapper buildPom( String pomPath )
        throws Exception
    {
        File pomFile = new File( testDirectory + File.separator + pomPath, "pom.xml" );
        File settingsFile = new File( testDirectory + File.separator + pomPath, "settings.xml" );
        Settings settings = readSettingsFile( settingsFile );
        ProjectBuildingRequest config = new DefaultProjectBuildingRequest();
        for ( org.apache.maven.settings.Profile rawProfile : settings.getProfiles() )
        {
            Profile profile = SettingsUtils.convertFromSettingsProfile( rawProfile );
            config.addProfile( profile );
        }
        String localRepoUrl =
            System.getProperty( "maven.repo.local", System.getProperty( "user.home" ) + "/.m2/repository" );
        localRepoUrl = "file://" + localRepoUrl;
        config.setLocalRepository( repositorySystem.createArtifactRepository( "local", localRepoUrl,
                                                                              new DefaultRepositoryLayout(), null, null ) );
        config.setActiveProfileIds( settings.getActiveProfiles() );
        MavenRepositorySystemSession repoSession = new MavenRepositorySystemSession();
        repoSession.setLocalRepositoryManager( new SimpleLocalRepositoryManager(
                                                                                 new File(
                                                                                           config.getLocalRepository().getBasedir() ) ) );
        config.setRepositorySession( repoSession );
        return new PomTestWrapper( pomFile, projectBuilder.build( pomFile, config ).getProject() );
    }
    private static Settings readSettingsFile( File settingsFile )
        throws IOException, XmlPullParserException
    {
        Settings settings = null;
        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( settingsFile );
            SettingsXpp3Reader modelReader = new SettingsXpp3Reader();
            settings = modelReader.read( reader );
        }
        finally
        {
            IOUtil.close( reader );
        }
        return settings;
    }
}
