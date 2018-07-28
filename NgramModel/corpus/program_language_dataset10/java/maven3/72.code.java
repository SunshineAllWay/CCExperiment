package org.apache.maven.artifact.resolver;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
@Deprecated
public interface ArtifactCollector
    extends org.apache.maven.repository.legacy.resolver.LegacyArtifactCollector
{
    @Deprecated
    ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact,
                                      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                                      ArtifactMetadataSource source, ArtifactFilter filter,
                                      List<ResolutionListener> listeners )
        throws ArtifactResolutionException;
}
