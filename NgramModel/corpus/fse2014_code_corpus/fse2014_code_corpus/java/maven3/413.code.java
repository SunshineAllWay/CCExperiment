package org.apache.maven.plugin;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
@Component(role = BuildPluginManager.class)
public class DefaultBuildPluginManager
    implements BuildPluginManager
{
    @Requirement
    private PlexusContainer container;
    @Requirement
    private MavenPluginManager mavenPluginManager;
    @Requirement
    private LegacySupport legacySupport;
    public PluginDescriptor loadPlugin( Plugin plugin, List<RemoteRepository> repositories, RepositorySystemSession session )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException, InvalidPluginDescriptorException
    {
        return mavenPluginManager.getPluginDescriptor( plugin, repositories, session );
    }
    public void executeMojo( MavenSession session, MojoExecution mojoExecution )
        throws MojoFailureException, MojoExecutionException, PluginConfigurationException, PluginManagerException
    {
        MavenProject project = session.getCurrentProject();
        MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();
        Mojo mojo = null;
        ClassRealm pluginRealm;
        try
        {
            pluginRealm = getPluginRealm( session, mojoDescriptor.getPluginDescriptor() );
        }
        catch ( PluginResolutionException e )
        {
            throw new PluginExecutionException( mojoExecution, project, e );
        }
        ClassRealm oldLookupRealm = container.setLookupRealm( pluginRealm );
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( pluginRealm );
        MavenSession oldSession = legacySupport.getSession();
        try
        {
            mojo = mavenPluginManager.getConfiguredMojo( Mojo.class, session, mojoExecution );
            legacySupport.setSession( session );
            try
            {
                mojo.execute();
            }
            catch ( ClassCastException e )
            {
                throw e;
            }
            catch ( RuntimeException e )
            {
                throw new PluginExecutionException( mojoExecution, project, e );
            }
        }
        catch ( PluginContainerException e )
        {
            throw new PluginExecutionException( mojoExecution, project, e );
        }
        catch ( NoClassDefFoundError e )
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream( 1024 );
            PrintStream ps = new PrintStream( os );
            ps.println( "A required class was missing while executing " + mojoDescriptor.getId() + ": "
                + e.getMessage() );
            pluginRealm.display( ps );
            Exception wrapper = new PluginContainerException( mojoDescriptor, pluginRealm, os.toString(), e );
            throw new PluginExecutionException( mojoExecution, project, wrapper );
        }
        catch ( LinkageError e )
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream( 1024 );
            PrintStream ps = new PrintStream( os );
            ps.println( "An API incompatibility was encountered while executing " + mojoDescriptor.getId() + ": "
                + e.getClass().getName() + ": " + e.getMessage() );
            pluginRealm.display( ps );
            Exception wrapper = new PluginContainerException( mojoDescriptor, pluginRealm, os.toString(), e );
            throw new PluginExecutionException( mojoExecution, project, wrapper );
        }
        catch ( ClassCastException e )
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream( 1024 );
            PrintStream ps = new PrintStream( os );
            ps.println( "A type incompatibility occured while executing " + mojoDescriptor.getId() + ": "
                + e.getMessage() );
            pluginRealm.display( ps );
            throw new PluginExecutionException( mojoExecution, project, os.toString(), e );
        }
        finally
        {
            mavenPluginManager.releaseMojo( mojo, mojoExecution );
            Thread.currentThread().setContextClassLoader( oldClassLoader );
            container.setLookupRealm( oldLookupRealm );
            legacySupport.setSession( oldSession );
        }
    }
    public ClassRealm getPluginRealm( MavenSession session, PluginDescriptor pluginDescriptor ) 
        throws PluginResolutionException, PluginManagerException
    {
        ClassRealm pluginRealm = pluginDescriptor.getClassRealm();
        if ( pluginRealm != null )
        {
            return pluginRealm;
        }
        mavenPluginManager.setupPluginRealm( pluginDescriptor, session, null, null, null );
        return pluginDescriptor.getClassRealm();
    }
    public MojoDescriptor getMojoDescriptor( Plugin plugin, String goal, List<RemoteRepository> repositories,
                                             RepositorySystemSession session )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, InvalidPluginDescriptorException
    {
        return mavenPluginManager.getMojoDescriptor( plugin, goal, repositories, session );
    }
}
