package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
public interface ArtifactResolver
{
    String ROLE = ArtifactResolver.class.getName();
    void resolve( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts,
                                                  Artifact originatingArtifact,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactRepository localRepository,
                                                  ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts,
                                                  Artifact originatingArtifact,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactRepository localRepository,
                                                  ArtifactMetadataSource source,
                                                  List listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts,
                                                  Artifact originatingArtifact,
                                                  ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source,
                                                  ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts,
                                                  Artifact originatingArtifact,
                                                  Map managedVersions,
                                                  ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts,
                                                  Artifact originatingArtifact,
                                                  Map managedVersions,
                                                  ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source, ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts,
                                                  Artifact originatingArtifact,
                                                  Map managedVersions,
                                                  ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source,
                                                  ArtifactFilter filter,
                                                  List listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    void resolveAlways( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException;
}