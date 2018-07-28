package org.apache.maven.project;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.graph.DependencyFilter;
public class DefaultDependencyResolutionRequest
    implements DependencyResolutionRequest
{
    private MavenProject project;
    private DependencyFilter filter;
    private RepositorySystemSession session;
    public DefaultDependencyResolutionRequest()
    {
    }
    public DefaultDependencyResolutionRequest( MavenProject project, RepositorySystemSession session )
    {
        setMavenProject( project );
        setRepositorySession( session );
    }
    public DependencyFilter getResolutionFilter()
    {
        return filter;
    }
    public MavenProject getMavenProject()
    {
        return project;
    }
    public RepositorySystemSession getRepositorySession()
    {
        return session;
    }
    public DependencyResolutionRequest setResolutionFilter( DependencyFilter filter )
    {
        this.filter = filter;
        return this;
    }
    public DependencyResolutionRequest setMavenProject( MavenProject project )
    {
        this.project = project;
        return this;
    }
    public DependencyResolutionRequest setRepositorySession( RepositorySystemSession repositorySession )
    {
        this.session = repositorySession;
        return this;
    }
}
