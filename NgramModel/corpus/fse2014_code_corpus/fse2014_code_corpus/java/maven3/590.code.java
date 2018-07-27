package org.apache.maven.project;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
public class EmptyProjectBuildingHelper
    implements ProjectBuildingHelper
{
    public List<ArtifactRepository> createArtifactRepositories( List<Repository> pomRepositories,
                                                                List<ArtifactRepository> externalRepositories,
                                                                ProjectBuildingRequest request )
    {
        if ( externalRepositories != null )
        {
            return externalRepositories;
        }
        else
        {
            return new ArrayList<ArtifactRepository>();
        }
    }
    public ProjectRealmCache.CacheRecord createProjectRealm( MavenProject proejct,
                                                             Model model, ProjectBuildingRequest request )
    {
        return new ProjectRealmCache.CacheRecord( null, null );
    }
    public void selectProjectRealm( MavenProject project )
    {
    }
}
