package org.apache.maven.repository.legacy;
import java.io.File;
import org.apache.maven.artifact.AbstractArtifactComponentTestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.repository.legacy.DefaultUpdateCheckManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
public class DefaultUpdateCheckManagerTest
    extends AbstractArtifactComponentTestCase
{
    DefaultUpdateCheckManager updateCheckManager;
    @Override
    protected String component()
    {
        return "updateCheckManager";
    }
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        updateCheckManager = new DefaultUpdateCheckManager( new ConsoleLogger( Logger.LEVEL_DEBUG, "test" ) );
    }
    public void testArtifact() throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();
        ArtifactRepository localRepository = localRepository();
        Artifact a = createArtifact( "a", "0.0.1-SNAPSHOT" );
        File file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        file.delete();
        a.setFile( file );
        File touchFile = updateCheckManager.getTouchfile( a );
        touchFile.delete();
        assertTrue( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        file.getParentFile().mkdirs();
        file.createNewFile();
        updateCheckManager.touch( a, remoteRepository, null );
        assertFalse( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        assertNull( updateCheckManager.readLastUpdated( touchFile,
                                                        updateCheckManager.getRepositoryKey( remoteRepository ) ) );
        assertFalse( updateCheckManager.getTouchfile( a ).exists() );
    }
    public void testMissingArtifact()
        throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();
        ArtifactRepository localRepository = localRepository();
        Artifact a = createArtifact( "a", "0.0.1-SNAPSHOT" );
        File file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        file.delete();
        a.setFile( file );
        File touchFile = updateCheckManager.getTouchfile( a );
        touchFile.delete();
        assertTrue( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        updateCheckManager.touch( a, remoteRepository, null );
        assertFalse( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        assertFalse( file.exists() );
        assertNotNull( updateCheckManager.readLastUpdated( touchFile,
                                                           updateCheckManager.getRepositoryKey( remoteRepository ) ) );
    }
    public void testPom() throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();
        ArtifactRepository localRepository = localRepository();
        Artifact a = createArtifact( "a", "0.0.1", "pom" );
        File file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        file.delete();
        a.setFile( file );
        File touchFile = updateCheckManager.getTouchfile( a );
        touchFile.delete();
        assertTrue( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        file.getParentFile().mkdirs();
        file.createNewFile();
        updateCheckManager.touch( a, remoteRepository, null );
        assertFalse( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        assertNull( updateCheckManager.readLastUpdated( touchFile,
                                                        updateCheckManager.getRepositoryKey( remoteRepository ) ) );
        assertFalse( updateCheckManager.getTouchfile( a ).exists() );
    }
    public void testMissingPom()
        throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();
        ArtifactRepository localRepository = localRepository();
        Artifact a = createArtifact( "a", "0.0.1", "pom" );
        File file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        file.delete();
        a.setFile( file );
        File touchFile = updateCheckManager.getTouchfile( a );
        touchFile.delete();
        assertTrue( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        updateCheckManager.touch( a, remoteRepository, null );
        assertFalse( updateCheckManager.isUpdateRequired( a, remoteRepository ) );
        assertFalse( file.exists() );
        assertNotNull( updateCheckManager.readLastUpdated( touchFile,
                                                           updateCheckManager.getRepositoryKey( remoteRepository ) ) );
    }
    public void testMetadata() throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();
        ArtifactRepository localRepository = localRepository();
        Artifact a = createRemoteArtifact( "a", "0.0.1-SNAPSHOT" );
        RepositoryMetadata metadata = new ArtifactRepositoryMetadata( a );
        File file = new File( localRepository.getBasedir(),
                              localRepository.pathOfLocalRepositoryMetadata( metadata, localRepository ) );
        file.delete();
        File touchFile = updateCheckManager.getTouchfile( metadata, file );
        touchFile.delete();
        assertTrue( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );
        file.getParentFile().mkdirs();
        file.createNewFile();
        updateCheckManager.touch( metadata, remoteRepository, file );
        assertFalse( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );
        assertNotNull( updateCheckManager.readLastUpdated( touchFile, updateCheckManager.getMetadataKey( remoteRepository, file ) ) );
    }
    public void testMissingMetadata() throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();
        ArtifactRepository localRepository = localRepository();
        Artifact a = createRemoteArtifact( "a", "0.0.1-SNAPSHOT" );
        RepositoryMetadata metadata = new ArtifactRepositoryMetadata( a );
        File file = new File( localRepository.getBasedir(),
                              localRepository.pathOfLocalRepositoryMetadata( metadata, localRepository ) );
        file.delete();
        File touchFile = updateCheckManager.getTouchfile( metadata, file );
        touchFile.delete();
        assertTrue( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );
        updateCheckManager.touch( metadata, remoteRepository, file );
        assertFalse( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );
        assertNotNull( updateCheckManager.readLastUpdated( touchFile, updateCheckManager.getMetadataKey( remoteRepository, file ) ) );
    }
    public void testArtifactTouchFileName() throws Exception
    {
        ArtifactFactory artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.ROLE );
        ArtifactRepository localRepository = localRepository();
        Artifact a = artifactFactory.createArtifactWithClassifier( "groupdId", "a", "0.0.1-SNAPSHOT", "jar", null );
        File file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        a.setFile( file );
        assertEquals( "a-0.0.1-SNAPSHOT.jar.lastUpdated", updateCheckManager.getTouchfile( a ).getName() );
        a = artifactFactory.createArtifactWithClassifier( "groupdId", "a", "0.0.1-SNAPSHOT", "jar", "classifier" );
        file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        a.setFile( file );
        assertEquals( "a-0.0.1-SNAPSHOT-classifier.jar.lastUpdated", updateCheckManager.getTouchfile( a ).getName() );
    }
}
