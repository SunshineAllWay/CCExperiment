package org.apache.maven.lifecycle;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.lifecycle.internal.LifecycleStarter;
import org.apache.maven.lifecycle.internal.LifecycleTaskSegmentCalculator;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.lifecycle.internal.ProjectIndex;
import org.apache.maven.lifecycle.internal.TaskSegment;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Component( role = LifecycleExecutor.class )
public class DefaultLifecycleExecutor
    implements LifecycleExecutor
{
    @Requirement
    private LifeCyclePluginAnalyzer lifeCyclePluginAnalyzer;
    @Requirement
    private DefaultLifecycles defaultLifeCycles;
    @Requirement
    private LifecycleTaskSegmentCalculator lifecycleTaskSegmentCalculator;
    @Requirement
    private LifecycleExecutionPlanCalculator lifecycleExecutionPlanCalculator;
    @Requirement
    private MojoExecutor mojoExecutor;
    @Requirement
    private LifecycleStarter lifecycleStarter;
    public void execute( MavenSession session )
    {
        lifecycleStarter.execute( session );
    }
    @Requirement
    private MojoDescriptorCreator mojoDescriptorCreator;
    public Set<Plugin> getPluginsBoundByDefaultToAllLifecycles( String packaging )
    {
        return lifeCyclePluginAnalyzer.getPluginsBoundByDefaultToAllLifecycles( packaging );
    }
    @SuppressWarnings( { "UnusedDeclaration" } )
    @Deprecated
    public Map<String, Lifecycle> getPhaseToLifecycleMap()
    {
        return defaultLifeCycles.getPhaseToLifecycleMap();
    }
    @SuppressWarnings( { "UnusedDeclaration" } )
    MojoDescriptor getMojoDescriptor( String task, MavenSession session, MavenProject project, String invokedVia,
                                      boolean canUsePrefix, boolean isOptionalMojo )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        PluginVersionResolutionException
    {
        return mojoDescriptorCreator.getMojoDescriptor( task, session, project );
    }
    @SuppressWarnings( { "UnusedDeclaration" } )
    public MavenExecutionPlan calculateExecutionPlan( MavenSession session, boolean setup, String... tasks )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        PluginManagerException, LifecyclePhaseNotFoundException, LifecycleNotFoundException,
        PluginVersionResolutionException
    {
        List<TaskSegment> taskSegments =
            lifecycleTaskSegmentCalculator.calculateTaskSegments( session, Arrays.asList( tasks ) );
        TaskSegment mergedSegment = new TaskSegment( false );
        for ( TaskSegment taskSegment : taskSegments )
        {
            mergedSegment.getTasks().addAll( taskSegment.getTasks() );
        }
        return lifecycleExecutionPlanCalculator.calculateExecutionPlan( session, session.getCurrentProject(),
                                                                        mergedSegment.getTasks(), setup );
    }
    public MavenExecutionPlan calculateExecutionPlan( MavenSession session, String... tasks )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        PluginManagerException, LifecyclePhaseNotFoundException, LifecycleNotFoundException,
        PluginVersionResolutionException
    {
        return calculateExecutionPlan( session, true, tasks );
    }
    public void calculateForkedExecutions( MojoExecution mojoExecution, MavenSession session )
        throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
        PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException
    {
        lifecycleExecutionPlanCalculator.calculateForkedExecutions( mojoExecution, session );
    }
    public List<MavenProject> executeForkedExecutions( MojoExecution mojoExecution, MavenSession session )
        throws LifecycleExecutionException
    {
        return mojoExecutor.executeForkedExecutions( mojoExecution, session, new ProjectIndex( session.getProjects() ) );
    }
}
