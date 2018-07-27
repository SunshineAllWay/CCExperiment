package org.apache.maven.artifact.manager;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
@Deprecated
public interface WagonManager
    extends org.apache.maven.repository.legacy.WagonManager
{
    AuthenticationInfo getAuthenticationInfo( String id );
    ProxyInfo getProxy( String protocol );
    void getArtifact( Artifact artifact, ArtifactRepository repository )
        throws TransferFailedException, ResourceDoesNotExistException;
    void getArtifact( Artifact artifact, List<ArtifactRepository> remoteRepositories )
        throws TransferFailedException, ResourceDoesNotExistException;
    ArtifactRepository getMirrorRepository( ArtifactRepository repository );
}
