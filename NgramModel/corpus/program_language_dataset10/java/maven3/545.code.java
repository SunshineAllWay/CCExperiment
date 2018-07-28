package org.apache.maven;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;
public abstract class AbstractCoreMavenComponentTestCase
    extends PlexusTestCase
{
    @Requirement
    protected RepositorySystem repositorySystem;
    @Requirement
    protected org.apache.maven.project.ProjectBuilder projectBuilder;
    protected void setUp()
        throws Exception
    {
        repositorySystem = lookup( RepositorySystem.class );
        projectBuilder = lookup( org.apache.maven.project.ProjectBuilder.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        repositorySystem = null;
        projectBuilder = null;
        super.tearDown();
    }
    abstract protected String getProjectsDirectory();
    protected File getProject( String name )
        throws Exception
    {
        File source = new File( new File( getBasedir(), getProjectsDirectory() ), name );
        File target = new File( new File( getBasedir(), "target" ), name );
        FileUtils.copyDirectoryStructureIfModified( source, target );
        return new File( target, "pom.xml" );
    }
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
    }
    protected MavenExecutionRequest createMavenExecutionRequest( File pom )
        throws Exception
    {        
        MavenExecutionRequest request = new DefaultMavenExecutionRequest()
            .setPom( pom )
            .setProjectPresent( true )
            .setShowErrors( true )
            .setPluginGroups( Arrays.asList( new String[] { "org.apache.maven.plugins" } ) )
            .setLocalRepository( getLocalRepository() )
            .setRemoteRepositories( getRemoteRepositories() )
            .setPluginArtifactRepositories( getPluginArtifactRepositories() )
            .setGoals( Arrays.asList( new String[] { "package" } ) );
        return request;
    }
    protected MavenSession createMavenSession( File pom )
        throws Exception        
    {
        return createMavenSession( pom, new Properties() );
    }
    protected MavenSession createMavenSession( File pom, Properties executionProperties )
        throws Exception
    {
        MavenExecutionRequest request = createMavenExecutionRequest( pom );
        ProjectBuildingRequest configuration = new DefaultProjectBuildingRequest()
            .setLocalRepository( request.getLocalRepository() )
            .setRemoteRepositories( request.getRemoteRepositories() )
            .setPluginArtifactRepositories( request.getPluginArtifactRepositories() )
            .setSystemProperties( executionProperties );
        MavenProject project = null;
        if ( pom != null )
        {
            project = projectBuilder.build( pom, configuration ).getProject();
        }
        else
        {
            project = createStubMavenProject();
            project.setRemoteArtifactRepositories( request.getRemoteRepositories() );
            project.setPluginArtifactRepositories( request.getPluginArtifactRepositories() );
        }
        initRepoSession( configuration );
        MavenSession session =
            new MavenSession( getContainer(), configuration.getRepositorySession(), request,
                              new DefaultMavenExecutionResult() );
        session.setProjects( Arrays.asList( project ) );
        return session;
    }
    protected void initRepoSession( ProjectBuildingRequest request )
    {
        File localRepo = new File( request.getLocalRepository().getBasedir() );
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        session.setLocalRepositoryManager( new SimpleLocalRepositoryManager( localRepo ) );
        request.setRepositorySession( session );
    }
    protected MavenProject createStubMavenProject()
    {
        Model model = new Model();
        model.setGroupId( "org.apache.maven.test" );
        model.setArtifactId( "maven-test" );
        model.setVersion( "1.0" );
        return new MavenProject( model );
    }
    protected List<ArtifactRepository> getRemoteRepositories()
        throws InvalidRepositoryException
    {
        File repoDir = new File( getBasedir(), "src/test/remote-repo" ).getAbsoluteFile();
        RepositoryPolicy policy = new RepositoryPolicy();
        policy.setEnabled( true );
        policy.setChecksumPolicy( "ignore" );
        policy.setUpdatePolicy( "always" );
        Repository repository = new Repository();
        repository.setId( RepositorySystem.DEFAULT_REMOTE_REPO_ID );
        repository.setUrl( "file://" + repoDir.toURI().getPath() );
        repository.setReleases( policy );
        repository.setSnapshots( policy );
        return Arrays.asList( repositorySystem.buildArtifactRepository( repository ) );
    }
    protected List<ArtifactRepository> getPluginArtifactRepositories()
        throws InvalidRepositoryException
    {
        return getRemoteRepositories();
    }
    protected ArtifactRepository getLocalRepository()
        throws InvalidRepositoryException
    {
        File repoDir = new File( getBasedir(), "target/local-repo" ).getAbsoluteFile();
        return repositorySystem.createLocalRepository( repoDir );
    }
    protected class ProjectBuilder
    {
        private MavenProject project;
        public ProjectBuilder( MavenProject project )
        {
            this.project = project;
        }
        public ProjectBuilder( String groupId, String artifactId, String version )
        {
            Model model = new Model();
            model.setModelVersion( "4.0.0" );
            model.setGroupId( groupId );
            model.setArtifactId( artifactId );
            model.setVersion( version );  
            model.setBuild(  new Build() );
            project = new MavenProject( model );            
        }
        public ProjectBuilder setGroupId( String groupId )
        {
            project.setGroupId( groupId );
            return this;
        }
        public ProjectBuilder setArtifactId( String artifactId )
        {
            project.setArtifactId( artifactId );
            return this;
        }
        public ProjectBuilder setVersion( String version )
        {
            project.setVersion( version );
            return this;
        }
        public ProjectBuilder addDependency( String groupId, String artifactId, String version, String scope )
        {
            return addDependency( groupId, artifactId, version, scope, (Exclusion)null );
        }
        public ProjectBuilder addDependency( String groupId, String artifactId, String version, String scope, Exclusion exclusion )
        {
            return addDependency( groupId, artifactId, version, scope, null, exclusion );            
        }
        public ProjectBuilder addDependency( String groupId, String artifactId, String version, String scope, String systemPath )
        {
            return addDependency( groupId, artifactId, version, scope, systemPath, null );         
        }
        public ProjectBuilder addDependency( String groupId, String artifactId, String version, String scope, String systemPath, Exclusion exclusion )
        {
            Dependency d = new Dependency();
            d.setGroupId( groupId );
            d.setArtifactId( artifactId );
            d.setVersion( version );
            d.setScope( scope );
            if ( systemPath != null && scope.equals(  Artifact.SCOPE_SYSTEM ) )
            {
                d.setSystemPath( systemPath );
            }
            if ( exclusion != null )
            {
                d.addExclusion( exclusion );
            }
            project.getDependencies().add( d );
            return this;
        }
        public ProjectBuilder addPlugin( Plugin plugin )
        {
            project.getBuildPlugins().add( plugin );            
            return this;
        }
        public MavenProject get()
        {
            return project;
        }        
    }    
    protected class PluginBuilder
    {
        private Plugin plugin;
        public PluginBuilder( String groupId, String artifactId, String version )
        {
            plugin = new Plugin();
            plugin.setGroupId( groupId );
            plugin.setArtifactId( artifactId );
            plugin.setVersion( version );                         
        }
        public PluginBuilder addDependency( String groupId, String artifactId, String version, String scope, Exclusion exclusion )
        {
            return addDependency( groupId, artifactId, version, scope, exclusion );            
        }
        public PluginBuilder addDependency( String groupId, String artifactId, String version, String scope, String systemPath )
        {
            return addDependency( groupId, artifactId, version, scope, systemPath, null );         
        }
        public PluginBuilder addDependency( String groupId, String artifactId, String version, String scope, String systemPath, Exclusion exclusion )
        {
            Dependency d = new Dependency();
            d.setGroupId( groupId );
            d.setArtifactId( artifactId );
            d.setVersion( version );
            d.setScope( scope );
            if ( systemPath != null && scope.equals(  Artifact.SCOPE_SYSTEM ) )
            {
                d.setSystemPath( systemPath );
            }
            if ( exclusion != null )
            {
                d.addExclusion( exclusion );
            }
            plugin.getDependencies().add( d );
            return this;
        }
        public Plugin get()
        {
            return plugin;
        }        
    }        
}
