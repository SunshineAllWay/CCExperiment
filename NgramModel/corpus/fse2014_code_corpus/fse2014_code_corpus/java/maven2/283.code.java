package org.apache.maven.project;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.wagon.events.TransferListener;
import java.io.File;
import java.util.List;
public interface MavenProjectBuilder
{
    String ROLE = MavenProjectBuilder.class.getName();
    String STANDALONE_SUPERPOM_GROUPID = "org.apache.maven";
    String STANDALONE_SUPERPOM_ARTIFACTID = "super-pom";
    String STANDALONE_SUPERPOM_VERSION = "2.0";
    MavenProject build( File project, ArtifactRepository localRepository, ProfileManager globalProfileManager )
        throws ProjectBuildingException;
    MavenProject build( File project, ArtifactRepository localRepository, ProfileManager globalProfileManager,
                        boolean checkDistributionManagementStatus )
        throws ProjectBuildingException;
    MavenProject buildWithDependencies( File project, ArtifactRepository localRepository,
                                        ProfileManager globalProfileManager, TransferListener transferListener )
        throws ProjectBuildingException, ArtifactResolutionException, ArtifactNotFoundException;
    MavenProject buildWithDependencies( File project, ArtifactRepository localRepository,
                                        ProfileManager globalProfileManager )
        throws ProjectBuildingException, ArtifactResolutionException, ArtifactNotFoundException;
    MavenProject buildFromRepository( Artifact artifact, List remoteArtifactRepositories,
                                      ArtifactRepository localRepository )
        throws ProjectBuildingException;
    MavenProject buildFromRepository( Artifact artifact, List remoteArtifactRepositories,
                                      ArtifactRepository localRepository, boolean allowStubModel )
        throws ProjectBuildingException;
    MavenProject buildFromRepository( Artifact artifact, List remoteArtifactRepositories,
                                      ProjectBuilderConfiguration configuration, boolean allowStubModel )
        throws ProjectBuildingException;
    MavenProject buildStandaloneSuperProject( ArtifactRepository localRepository )
        throws ProjectBuildingException;
    MavenProject buildStandaloneSuperProject( ArtifactRepository localRepository, ProfileManager profileManager )
        throws ProjectBuildingException;
    MavenProject buildStandaloneSuperProject( ProjectBuilderConfiguration config )
        throws ProjectBuildingException;
    MavenProject build( File pom,
                        ProjectBuilderConfiguration config )
        throws ProjectBuildingException;
    MavenProject build( File pom,
                        ProjectBuilderConfiguration config,
                        boolean checkDistributionManagementStatus )
        throws ProjectBuildingException;
    void calculateConcreteState( MavenProject project, ProjectBuilderConfiguration config )
        throws ModelInterpolationException;
    void calculateConcreteState( MavenProject project, ProjectBuilderConfiguration config, boolean processReferences )
        throws ModelInterpolationException;
}
