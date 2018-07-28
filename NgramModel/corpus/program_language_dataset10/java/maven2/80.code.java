package org.apache.maven.artifact.manager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.io.File;
import java.util.Collection;
import java.util.List;
public interface WagonManager
{
    String ROLE = WagonManager.class.getName();
    Wagon getWagon( String protocol )
        throws UnsupportedProtocolException;
    Wagon getWagon( Repository repository )
        throws UnsupportedProtocolException, WagonConfigurationException;
    void getArtifact( Artifact artifact, List remoteRepositories )
        throws TransferFailedException, ResourceDoesNotExistException;
    void getArtifact( Artifact artifact, ArtifactRepository repository )
        throws TransferFailedException, ResourceDoesNotExistException;
    void putArtifact( File source, Artifact artifact, ArtifactRepository deploymentRepository )
        throws TransferFailedException;
    void putArtifactMetadata( File source, ArtifactMetadata artifactMetadata, ArtifactRepository repository )
        throws TransferFailedException;
    void getArtifactMetadata( ArtifactMetadata metadata, ArtifactRepository remoteRepository, File destination,
                              String checksumPolicy )
        throws TransferFailedException, ResourceDoesNotExistException;
    void getArtifactMetadataFromDeploymentRepository( ArtifactMetadata metadata, ArtifactRepository remoteRepository,
                                                      File file, String checksumPolicyWarn )
        throws TransferFailedException, ResourceDoesNotExistException;
    void setOnline( boolean online );
    boolean isOnline();
    void addProxy( String protocol, String host, int port, String username, String password, String nonProxyHosts );
    void addAuthenticationInfo( String repositoryId, String username, String password, String privateKey,
                                String passphrase );
    void addMirror( String id, String mirrorOf, String url );
    void setDownloadMonitor( TransferListener downloadMonitor );
    void addPermissionInfo( String repositoryId, String filePermissions, String directoryPermissions );
    ProxyInfo getProxy( String protocol );
    AuthenticationInfo getAuthenticationInfo( String id );
    void addConfiguration( String repositoryId, Xpp3Dom configuration );
    void setInteractive( boolean interactive );
    void registerWagons( Collection wagons, PlexusContainer extensionContainer );
    void setDefaultRepositoryPermissions( RepositoryPermissions permissions );
    ArtifactRepository getMirrorRepository( ArtifactRepository repository );
}
