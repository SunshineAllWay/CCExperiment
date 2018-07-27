package org.apache.maven.repository.legacy;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.repository.Repository;
public interface WagonManager
{
    @Deprecated
    Wagon getWagon( String protocol )
        throws UnsupportedProtocolException;
    @Deprecated
    Wagon getWagon( Repository repository )
        throws UnsupportedProtocolException, WagonConfigurationException;
    void getArtifact( Artifact artifact, ArtifactRepository repository, TransferListener transferListener, boolean force )
        throws TransferFailedException, ResourceDoesNotExistException;
    void getArtifact( Artifact artifact, List<ArtifactRepository> remoteRepositories,
                      TransferListener transferListener, boolean force )
        throws TransferFailedException, ResourceDoesNotExistException;
    void getRemoteFile( ArtifactRepository repository, File destination, String remotePath,
                        TransferListener downloadMonitor, String checksumPolicy, boolean force )
        throws TransferFailedException, ResourceDoesNotExistException;
    void getArtifactMetadata( ArtifactMetadata metadata, ArtifactRepository remoteRepository, File destination,
                              String checksumPolicy )
        throws TransferFailedException, ResourceDoesNotExistException;
    void getArtifactMetadataFromDeploymentRepository( ArtifactMetadata metadata, ArtifactRepository remoteRepository,
                                                      File file, String checksumPolicyWarn )
        throws TransferFailedException, ResourceDoesNotExistException;
    void putArtifact( File source, Artifact artifact, ArtifactRepository deploymentRepository,
                      TransferListener downloadMonitor )
        throws TransferFailedException;
    void putRemoteFile( ArtifactRepository repository, File source, String remotePath, TransferListener downloadMonitor )
        throws TransferFailedException;
    void putArtifactMetadata( File source, ArtifactMetadata artifactMetadata, ArtifactRepository repository )
        throws TransferFailedException;
}
