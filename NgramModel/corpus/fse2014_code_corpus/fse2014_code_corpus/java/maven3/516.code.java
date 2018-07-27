package org.apache.maven.repository;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.sonatype.aether.RepositorySystemSession;
public interface RepositorySystem
{
    final String DEFAULT_LOCAL_REPO_ID = "local";
    final String userHome = System.getProperty( "user.home" );
    final File userMavenConfigurationHome = new File( userHome, ".m2" );
    final File defaultUserLocalRepository = new File( userMavenConfigurationHome, "repository" );
    final String DEFAULT_REMOTE_REPO_ID = "central";
    final String DEFAULT_REMOTE_REPO_URL = "http://repo1.maven.org/maven2";
    Artifact createArtifact( String groupId, String artifactId, String version, String packaging );
    Artifact createArtifact( String groupId, String artifactId, String version, String scope, String type );
    Artifact createProjectArtifact( String groupId, String artifactId, String version );
    Artifact createArtifactWithClassifier( String groupId, String artifactId, String version, String type,
                                           String classifier );
    Artifact createPluginArtifact( Plugin plugin );
    Artifact createDependencyArtifact( Dependency dependency );
    ArtifactRepository buildArtifactRepository( Repository repository )
        throws InvalidRepositoryException;
    ArtifactRepository createDefaultRemoteRepository()
        throws InvalidRepositoryException;
    ArtifactRepository createDefaultLocalRepository()
        throws InvalidRepositoryException;
    ArtifactRepository createLocalRepository( File localRepository )
        throws InvalidRepositoryException;
    ArtifactRepository createArtifactRepository( String id, String url, ArtifactRepositoryLayout repositoryLayout,
                                                 ArtifactRepositoryPolicy snapshots, ArtifactRepositoryPolicy releases );
    List<ArtifactRepository> getEffectiveRepositories( List<ArtifactRepository> repositories );
    Mirror getMirror( ArtifactRepository repository, List<Mirror> mirrors );
    void injectMirror( List<ArtifactRepository> repositories, List<Mirror> mirrors );
    void injectProxy( List<ArtifactRepository> repositories, List<org.apache.maven.settings.Proxy> proxies );
    void injectAuthentication( List<ArtifactRepository> repositories, List<Server> servers );
    void injectMirror( RepositorySystemSession session, List<ArtifactRepository> repositories );
    void injectProxy( RepositorySystemSession session, List<ArtifactRepository> repositories );
    void injectAuthentication( RepositorySystemSession session, List<ArtifactRepository> repositories );
    ArtifactResolutionResult resolve( ArtifactResolutionRequest request );
    void publish( ArtifactRepository repository, File source, String remotePath,
                  ArtifactTransferListener transferListener )
        throws ArtifactTransferFailedException;
    void retrieve( ArtifactRepository repository, File destination, String remotePath,
                   ArtifactTransferListener transferListener )
        throws ArtifactTransferFailedException, ArtifactDoesNotExistException;
}
