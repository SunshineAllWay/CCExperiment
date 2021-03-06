package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
public interface ArtifactCollector
{
    ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact,
                                      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                                      ArtifactMetadataSource source, ArtifactFilter filter,
                                      List<ResolutionListener> listeners )
        throws ArtifactResolutionException;
    ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact, Map managedVersions,
                                      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                                      ArtifactMetadataSource source, ArtifactFilter filter,
                                      List<ResolutionListener> listeners )
        throws ArtifactResolutionException;
}
