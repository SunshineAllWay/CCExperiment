package org.apache.maven.project;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.wagon.events.TransferListener;
@Deprecated
public interface MavenProjectBuilder
{
    MavenProject build( File pom, ProjectBuilderConfiguration configuration )
        throws ProjectBuildingException;
    MavenProject build( File pom, ArtifactRepository localRepository, ProfileManager profileManager )
        throws ProjectBuildingException;
    MavenProject buildFromRepository( Artifact artifact, List<ArtifactRepository> remoteRepositories,
                                      ArtifactRepository localRepository )
        throws ProjectBuildingException;
    MavenProject buildFromRepository( Artifact artifact, List<ArtifactRepository> remoteRepositories,
                                      ArtifactRepository localRepository, boolean allowStubModel )
        throws ProjectBuildingException;
    MavenProject buildStandaloneSuperProject( ProjectBuilderConfiguration configuration )
        throws ProjectBuildingException;
    MavenProject buildStandaloneSuperProject( ArtifactRepository localRepository )
        throws ProjectBuildingException;
    MavenProject buildStandaloneSuperProject( ArtifactRepository localRepository, ProfileManager profileManager )
        throws ProjectBuildingException;
    MavenProject buildWithDependencies( File pom, ArtifactRepository localRepository,
                                        ProfileManager globalProfileManager, TransferListener transferListener )
        throws ProjectBuildingException, ArtifactResolutionException, ArtifactNotFoundException;
    MavenProject buildWithDependencies( File pom, ArtifactRepository localRepository,
                                        ProfileManager globalProfileManager )
        throws ProjectBuildingException, ArtifactResolutionException, ArtifactNotFoundException;
}
