package org.apache.maven.artifact.resolver;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.wagon.events.TransferListener;
public interface ArtifactResolver
{
    ArtifactResolutionResult resolve( ArtifactResolutionRequest request );
    @Deprecated
    String ROLE = ArtifactResolver.class.getName();
    @Deprecated
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts, Artifact originatingArtifact,
                                                  ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source, ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts, Artifact originatingArtifact,
                                                  Map managedVersions, ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts, Artifact originatingArtifact,
                                                  Map managedVersions, ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source, ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts, Artifact originatingArtifact,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactRepository localRepository, ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts, Artifact originatingArtifact,
                                                  Map managedVersions, ArtifactRepository localRepository,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactMetadataSource source, ArtifactFilter filter,
                                                  List<ResolutionListener> listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    ArtifactResolutionResult resolveTransitively( Set<Artifact> artifacts, Artifact originatingArtifact,
                                                  List<ArtifactRepository> remoteRepositories,
                                                  ArtifactRepository localRepository, ArtifactMetadataSource source,
                                                  List<ResolutionListener> listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    void resolve( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    void resolve( Artifact artifact, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository,
                  TransferListener downloadMonitor )
        throws ArtifactResolutionException, ArtifactNotFoundException;
    @Deprecated
    void resolveAlways( Artifact artifact, List<ArtifactRepository> remoteRepositories,
                        ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException;
}
