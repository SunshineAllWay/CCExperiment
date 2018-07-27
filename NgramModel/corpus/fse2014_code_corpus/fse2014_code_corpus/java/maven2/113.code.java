package org.apache.maven.artifact.test;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.ReaderFactory;
import java.io.File;
public abstract class ArtifactTestCase
    extends PlexusTestCase
{
    private ArtifactRepository localRepository;
    protected File getLocalArtifactPath( Artifact artifact )
    {
        return new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
    }
    protected void setUp()
        throws Exception
    {
        super.setUp();
        String localRepo = System.getProperty( "maven.repo.local" );
        if ( localRepo == null )
        {
            File settingsFile = new File( System.getProperty( "user.home" ), ".m2/settings.xml" );
            if ( settingsFile.exists() )
            {
                Settings settings = new SettingsXpp3Reader().read( ReaderFactory.newXmlReader( settingsFile ) );
                localRepo = settings.getLocalRepository();
            }
        }
        if ( localRepo == null )
        {
            localRepo = System.getProperty( "user.home" ) + "/.m2/repository";
        }
        ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) container.lookup(
            ArtifactRepositoryLayout.ROLE, "default" );
        localRepository = new DefaultArtifactRepository( "local", "file://" + localRepo, repositoryLayout );
    }
}
