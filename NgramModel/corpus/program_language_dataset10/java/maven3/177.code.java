package org.apache.maven.repository.metadata;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
public interface MetadataSource
{
    String ROLE = MetadataSource.class.getName();
    MetadataResolution retrieve( ArtifactMetadata artifact, ArtifactRepository localRepository,
                                 List<ArtifactRepository> remoteRepositories )
        throws MetadataRetrievalException;
}