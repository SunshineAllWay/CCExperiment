package org.apache.maven.artifact.repository;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
public class DefaultRepositoryRequest
    implements RepositoryRequest
{
    private boolean offline;
    private boolean forceUpdate;
    private ArtifactRepository localRepository;
    private List<ArtifactRepository> remoteRepositories;
    public DefaultRepositoryRequest()
    {
    }
    public DefaultRepositoryRequest( RepositoryRequest repositoryRequest )
    {
        setLocalRepository( repositoryRequest.getLocalRepository() );
        setRemoteRepositories( repositoryRequest.getRemoteRepositories() );
        setOffline( repositoryRequest.isOffline() );
        setForceUpdate( repositoryRequest.isForceUpdate() );
    }
    public static RepositoryRequest getRepositoryRequest( MavenSession session, MavenProject project )
    {
        RepositoryRequest request = new DefaultRepositoryRequest();
        request.setLocalRepository( session.getLocalRepository() );
        if ( project != null )
        {
            request.setRemoteRepositories( project.getPluginArtifactRepositories() );
        }
        request.setOffline( session.isOffline() );
        request.setForceUpdate( session.getRequest().isUpdateSnapshots() );
        return request;
    }
    public boolean isOffline()
    {
        return offline;
    }
    public DefaultRepositoryRequest setOffline( boolean offline )
    {
        this.offline = offline;
        return this;
    }
    public boolean isForceUpdate()
    {
        return forceUpdate;
    }
    public DefaultRepositoryRequest setForceUpdate( boolean forceUpdate )
    {
        this.forceUpdate = forceUpdate;
        return this;
    }
    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }
    public DefaultRepositoryRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
        return this;
    }
    public List<ArtifactRepository> getRemoteRepositories()
    {
        if ( remoteRepositories == null )
        {
            remoteRepositories = new ArrayList<ArtifactRepository>();
        }
        return remoteRepositories;
    }
    public DefaultRepositoryRequest setRemoteRepositories( List<ArtifactRepository> remoteRepositories )
    {
        this.remoteRepositories = remoteRepositories;
        return this;
    }
}
