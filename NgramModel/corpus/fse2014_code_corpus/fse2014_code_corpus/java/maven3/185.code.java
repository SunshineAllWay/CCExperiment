package org.apache.maven.artifact.deployer;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;
import org.apache.maven.repository.legacy.metadata.MetadataResolutionRequest;
import org.apache.maven.repository.legacy.metadata.ResolutionGroup;
public class SimpleArtifactMetadataSource
    implements ArtifactMetadataSource
{
    public ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository,
                                     List<ArtifactRepository> remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        throw new UnsupportedOperationException( "Cannot retrieve metadata in this test case" );
    }
    public List<ArtifactVersion> retrieveAvailableVersions( Artifact artifact, ArtifactRepository localRepository,
                                                            List<ArtifactRepository> remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        return Collections.<ArtifactVersion>singletonList( new DefaultArtifactVersion( "10.1.3" ) );
    }
    public List<ArtifactVersion> retrieveAvailableVersionsFromDeploymentRepository( Artifact artifact,
                                                                                    ArtifactRepository localRepository,
                                                                                    ArtifactRepository remoteRepository )
        throws ArtifactMetadataRetrievalException
    {
        return Collections.<ArtifactVersion>singletonList( new DefaultArtifactVersion( "10.1.3" ) );
    }
    public ResolutionGroup retrieve( MetadataResolutionRequest request )
        throws ArtifactMetadataRetrievalException
    {
        return retrieve( request.getArtifact(), request.getLocalRepository(), request.getRemoteRepositories() );
    }
}
