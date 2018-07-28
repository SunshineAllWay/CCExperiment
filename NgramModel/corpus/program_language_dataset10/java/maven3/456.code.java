package org.apache.maven.plugin.version;
import java.util.Collections;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
public class DefaultPluginVersionRequest
    implements PluginVersionRequest
{
    private String groupId;
    private String artifactId;
    private Model pom;
    private List<RemoteRepository> repositories = Collections.emptyList();
    private RepositorySystemSession session;
    public DefaultPluginVersionRequest()
    {
    }
    public DefaultPluginVersionRequest( Plugin plugin, MavenSession session )
    {
        setGroupId( plugin.getGroupId() );
        setArtifactId( plugin.getArtifactId() );
        setRepositorySession( session.getRepositorySession() );
        MavenProject project = session.getCurrentProject();
        if ( project != null )
        {
            setRepositories( project.getRemotePluginRepositories() );
        }
    }
    public DefaultPluginVersionRequest( Plugin plugin, RepositorySystemSession session, List<RemoteRepository> repositories )
    {
        setGroupId( plugin.getGroupId() );
        setArtifactId( plugin.getArtifactId() );
        setRepositorySession( session );
        setRepositories( repositories );
    }
    public String getGroupId()
    {
        return groupId;
    }
    public DefaultPluginVersionRequest setGroupId( String groupId )
    {
        this.groupId = groupId;
        return this;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
    public DefaultPluginVersionRequest setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
        return this;
    }
    public Model getPom()
    {
        return pom;
    }
    public DefaultPluginVersionRequest setPom( Model pom )
    {
        this.pom = pom;
        return this;
    }
    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }
    public DefaultPluginVersionRequest setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories != null )
        {
            this.repositories = repositories;
        }
        else
        {
            this.repositories = Collections.emptyList();
        }
        return this;
    }
    public RepositorySystemSession getRepositorySession()
    {
        return session;
    }
    public DefaultPluginVersionRequest setRepositorySession( RepositorySystemSession session )
    {
        this.session = session;
        return this;
    }
}
