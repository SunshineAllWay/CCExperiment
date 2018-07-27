package org.apache.maven.project;
import java.util.List;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
public interface ProjectBuildingHelper
{
    List<ArtifactRepository> createArtifactRepositories( List<Repository> pomRepositories,
                                                         List<ArtifactRepository> externalRepositories,
                                                         ProjectBuildingRequest request )
        throws InvalidRepositoryException;
    ProjectRealmCache.CacheRecord createProjectRealm( MavenProject project, Model model,
                                                      ProjectBuildingRequest request )
        throws PluginResolutionException, PluginVersionResolutionException;
    void selectProjectRealm( MavenProject project );
}
