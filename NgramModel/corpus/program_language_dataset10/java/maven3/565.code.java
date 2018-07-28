package org.apache.maven.lifecycle.internal;
import junit.framework.TestCase;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleNotFoundException;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.apache.maven.lifecycle.internal.stub.ExecutionEventCatapultStub;
import org.apache.maven.lifecycle.internal.stub.LifecycleExecutionPlanCalculatorStub;
import org.apache.maven.lifecycle.internal.stub.LifecycleTaskSegmentCalculatorStub;
import org.apache.maven.lifecycle.internal.stub.LoggerStub;
import org.apache.maven.lifecycle.internal.stub.MojoExecutorStub;
import org.apache.maven.lifecycle.internal.stub.ProjectDependencyGraphStub;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class LifecycleWeaveBuilderTest
    extends TestCase
{
    public void testBuildProjectThreaded()
        throws Exception
    {
        ExecutorService executor = Executors.newFixedThreadPool( 10 );
        ExecutorCompletionService<ProjectSegment> service = new ExecutorCompletionService<ProjectSegment>( executor );
        runWithCompletionService( executor );
        executor.shutdown();
    }
    public void testBuildProjectThreadedAggressive()
        throws Exception
    {
        ExecutorService executor = Executors.newFixedThreadPool( 10 );
        ExecutorCompletionService<ProjectSegment> service = new ExecutorCompletionService<ProjectSegment>( executor );
        runWithCompletionService( executor );
        executor.shutdown();
    }
    private ProjectBuildList runWithCompletionService( ExecutorService service )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        PluginVersionResolutionException, LifecyclePhaseNotFoundException, LifecycleNotFoundException,
        ExecutionException, InterruptedException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
            BuildListCalculator buildListCalculator = new BuildListCalculator();
            final MavenSession session = ProjectDependencyGraphStub.getMavenSession();
            List<TaskSegment> taskSegments = getTaskSegmentCalculator().calculateTaskSegments( session );
            ProjectBuildList projectBuildList = buildListCalculator.calculateProjectBuilds( session, taskSegments );
            final MojoExecutorStub mojoExecutorStub = new MojoExecutorStub();
            final LifecycleWeaveBuilder builder = getWeaveBuilder( mojoExecutorStub );
            final ReactorContext buildContext = createBuildContext( session );
            ReactorBuildStatus reactorBuildStatus = new ReactorBuildStatus( session.getProjectDependencyGraph() );
            builder.build( projectBuildList, buildContext, taskSegments, session, service, reactorBuildStatus );
            LifecycleExecutionPlanCalculatorStub lifecycleExecutionPlanCalculatorStub =
                new LifecycleExecutionPlanCalculatorStub();
            final int expected = lifecycleExecutionPlanCalculatorStub.getNumberOfExceutions( projectBuildList );
            assertEquals( "All executions should be scheduled", expected, mojoExecutorStub.executions.size() );
            return projectBuildList;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( loader );
        }
    }
    private static LifecycleTaskSegmentCalculator getTaskSegmentCalculator()
    {
        return new LifecycleTaskSegmentCalculatorStub();
    }
    private ReactorContext createBuildContext( MavenSession session )
    {
        MavenExecutionResult mavenExecutionResult = new DefaultMavenExecutionResult();
        ReactorBuildStatus reactorBuildStatus = new ReactorBuildStatus( session.getProjectDependencyGraph() );
        return new ReactorContext( mavenExecutionResult, null, null, reactorBuildStatus );
    }
    private LifecycleWeaveBuilder getWeaveBuilder( MojoExecutor mojoExecutor )
    {
        final BuilderCommon builderCommon = getBuilderCommon();
        final LoggerStub loggerStub = new LoggerStub();
        return new LifecycleWeaveBuilder( mojoExecutor, builderCommon, loggerStub, new ExecutionEventCatapultStub() );
    }
    private BuilderCommon getBuilderCommon()
    {
        final LifecycleDebugLogger logger = new LifecycleDebugLogger( new LoggerStub() );
        return new BuilderCommon( logger, new LifecycleExecutionPlanCalculatorStub(),
                                  new LoggerStub() );
    }
}
