package org.apache.maven.plugin;
import java.util.List;
import org.apache.maven.AbstractCoreMavenComponentTestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.DefaultRepositoryRequest;
import org.apache.maven.artifact.repository.RepositoryRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;
public class PluginManagerTest
    extends AbstractCoreMavenComponentTestCase
{
    @Requirement
    private DefaultBuildPluginManager pluginManager;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        pluginManager = (DefaultBuildPluginManager) lookup( BuildPluginManager.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        pluginManager = null;
        super.tearDown();
    }
    protected String getProjectsDirectory()
    {
        return "src/test/projects/plugin-manager";
    }
    public void testPluginLoading()
        throws Exception
    {
        MavenSession session = createMavenSession( null );       
        Plugin plugin = new Plugin();
        plugin.setGroupId( "org.apache.maven.its.plugins" );
        plugin.setArtifactId( "maven-it-plugin" );
        plugin.setVersion( "0.1" );
        PluginDescriptor pluginDescriptor =
            pluginManager.loadPlugin( plugin, session.getCurrentProject().getRemotePluginRepositories(),
                                      session.getRepositorySession() );
        assertNotNull( pluginDescriptor );
    }
    public void testMojoDescriptorRetrieval()
        throws Exception
    {
        MavenSession session = createMavenSession( null );       
        String goal = "it";
        Plugin plugin = new Plugin();
        plugin.setGroupId( "org.apache.maven.its.plugins" );
        plugin.setArtifactId( "maven-it-plugin" );
        plugin.setVersion( "0.1" );
        MojoDescriptor mojoDescriptor =
            pluginManager.getMojoDescriptor( plugin, goal, session.getCurrentProject().getRemotePluginRepositories(),
                                             session.getRepositorySession() );
        assertNotNull( mojoDescriptor );
        assertEquals( goal, mojoDescriptor.getGoal() );
        PluginDescriptor pluginDescriptor = mojoDescriptor.getPluginDescriptor();
        assertNotNull( pluginDescriptor );
        assertEquals( "org.apache.maven.its.plugins", pluginDescriptor.getGroupId() );
        assertEquals( "maven-it-plugin", pluginDescriptor.getArtifactId() );
        assertEquals( "0.1", pluginDescriptor.getVersion() );
    }
    public void testRemoteResourcesPlugin()
        throws Exception
    {
    }
    public void testSurefirePlugin()
        throws Exception
    {
    }
    public void testMojoConfigurationIsMergedCorrectly()
        throws Exception
    {
    }
    public void testMojoWhereInternallyStatedDependencyIsOverriddenByProject()
        throws Exception
    {
    }
    public void testMojoThatIsPresentInTheCurrentBuild()
        throws Exception
    {
    }
    public void testAggregatorMojo()
        throws Exception
    {
    }
    public void testMojoThatRequiresExecutionToAGivenPhaseBeforeExecutingItself()
        throws Exception
    {
    }
    public void testThatPluginDependencyThatHasSystemScopeIsResolved()
        throws Exception
    {
        MavenSession session = createMavenSession( getProject( "project-contributing-system-scope-plugin-dep" ) );
        MavenProject project = session.getCurrentProject();
        Plugin plugin = project.getPlugin( "org.apache.maven.its.plugins:maven-it-plugin" );                
        RepositoryRequest repositoryRequest = new DefaultRepositoryRequest();
        repositoryRequest.setLocalRepository( getLocalRepository() );
        repositoryRequest.setRemoteRepositories( getPluginArtifactRepositories() );
        PluginDescriptor pluginDescriptor =
            pluginManager.loadPlugin( plugin, session.getCurrentProject().getRemotePluginRepositories(),
                                      session.getRepositorySession() );
        pluginManager.getPluginRealm( session, pluginDescriptor );
        List<Artifact> artifacts = pluginDescriptor.getArtifacts();
        for ( Artifact a : artifacts )
        {
            if ( a.getGroupId().equals( "org.apache.maven.its.mng3586" ) && a.getArtifactId().equals( "tools" ) )
            {
                return;
            }
        }
        fail( "Can't find the system scoped dependency in the plugin artifacts." );
    }
    protected void assertPluginDescriptor( MojoDescriptor mojoDescriptor, String groupId, String artifactId, String version )
    {
        assertNotNull( mojoDescriptor );        
        PluginDescriptor pd = mojoDescriptor.getPluginDescriptor();
        assertNotNull( pd );
        assertEquals( groupId, pd.getGroupId() );
        assertEquals( artifactId, pd.getArtifactId() );
        assertEquals( version, pd.getVersion() );        
    }       
}
