package org.apache.maven.lifecycle.internal;
import junit.framework.TestCase;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.lifecycle.internal.stub.BuildPluginManagerStub;
import org.apache.maven.lifecycle.internal.stub.DefaultLifecyclesStub;
import org.apache.maven.lifecycle.internal.stub.DefaultSchedulesStub;
import org.apache.maven.lifecycle.internal.stub.PluginPrefixResolverStub;
import org.apache.maven.lifecycle.internal.stub.PluginVersionResolverStub;
import org.apache.maven.lifecycle.internal.stub.ProjectDependencyGraphStub;
public class LifecycleExecutionPlanCalculatorTest
    extends TestCase
{
    public void testCalculateExecutionPlanWithGoalTasks()
        throws Exception
    {
        MojoDescriptorCreator mojoDescriptorCreator = createMojoDescriptorCreator();
        LifecycleExecutionPlanCalculator lifecycleExecutionPlanCalculator =
            createExecutionPlaceCalculator( mojoDescriptorCreator );
        final GoalTask goalTask1 = new GoalTask( "compiler:compile" );
        final GoalTask goalTask2 = new GoalTask( "surefire:test" );
        final TaskSegment taskSegment1 = new TaskSegment( false, goalTask1, goalTask2 );
        final MavenSession session1 = ProjectDependencyGraphStub.getMavenSession( ProjectDependencyGraphStub.A );
        MavenExecutionPlan executionPlan =
            lifecycleExecutionPlanCalculator.calculateExecutionPlan( session1, ProjectDependencyGraphStub.A,
                                                                     taskSegment1.getTasks() );
        assertEquals( 2, executionPlan.size() );
        final GoalTask goalTask3 = new GoalTask( "surefire:test" );
        final TaskSegment taskSegment2 = new TaskSegment( false, goalTask1, goalTask2, goalTask3 );
        MavenExecutionPlan executionPlan2 =
            lifecycleExecutionPlanCalculator.calculateExecutionPlan( session1, ProjectDependencyGraphStub.A,
                                                                     taskSegment2.getTasks() );
        assertEquals( 3, executionPlan2.size() );
    }
    public static LifecycleExecutionPlanCalculator createExecutionPlaceCalculator(
        MojoDescriptorCreator mojoDescriptorCreator )
    {
        LifecyclePluginResolver lifecyclePluginResolver =
            new LifecyclePluginResolver( new PluginVersionResolverStub() );
        return new DefaultLifecycleExecutionPlanCalculator( new BuildPluginManagerStub(),
                                                            DefaultLifecyclesStub.createDefaultLifecycles(),
                                                            mojoDescriptorCreator, lifecyclePluginResolver,
                                                            DefaultSchedulesStub.createDefaultSchedules() );
    }
    public static MojoDescriptorCreator createMojoDescriptorCreator()
    {
        return new MojoDescriptorCreator( new PluginVersionResolverStub(), new BuildPluginManagerStub(),
                                          new PluginPrefixResolverStub(),
                                          new LifecyclePluginResolver( new PluginVersionResolverStub() ) );
    }
}
