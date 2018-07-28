package org.apache.maven.lifecycle;
import org.apache.maven.AbstractCoreMavenComponentTestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.exception.ExceptionHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.ExecutionPlanItem;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.lifecycle.internal.LifecycleTaskSegmentCalculator;
import org.apache.maven.lifecycle.internal.DefaultLifecycleTaskSegmentCalculator;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.lifecycle.internal.TaskSegment;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class LifecycleExecutorTest
    extends AbstractCoreMavenComponentTestCase
{
    @Requirement
    private DefaultLifecycleExecutor lifecycleExecutor;
    @Requirement
    private DefaultLifecycleTaskSegmentCalculator lifeCycleTaskSegmentCalculator;
    @Requirement
    private LifecycleExecutionPlanCalculator lifeCycleExecutionPlanCalculator;
    @Requirement
    private MojoDescriptorCreator mojoDescriptorCreator;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        lifecycleExecutor = (DefaultLifecycleExecutor) lookup( LifecycleExecutor.class );
        lifeCycleTaskSegmentCalculator =
            (DefaultLifecycleTaskSegmentCalculator) lookup( LifecycleTaskSegmentCalculator.class );
        lifeCycleExecutionPlanCalculator = lookup( LifecycleExecutionPlanCalculator.class );
        mojoDescriptorCreator = lookup( MojoDescriptorCreator.class );
        lookup( ExceptionHandler.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        lifecycleExecutor = null;
        super.tearDown();
    }
    protected String getProjectsDirectory()
    {
        return "src/test/projects/lifecycle-executor";
    }
    public void testCalculationOfBuildPlanWithIndividualTaskWherePluginIsSpecifiedInThePom()
        throws Exception
    {
        File pom = getProject( "project-basic" );
        MavenSession session = createMavenSession( pom );
        assertEquals( "project-basic", session.getCurrentProject().getArtifactId() );
        assertEquals( "1.0", session.getCurrentProject().getVersion() );
        List<MojoExecution> executionPlan = getExecutions( calculateExecutionPlan( session, "resources:resources" ) );
        assertEquals( 1, executionPlan.size() );
        MojoExecution mojoExecution = executionPlan.get( 0 );
        assertNotNull( mojoExecution );
        assertEquals( "org.apache.maven.plugins",
                      mojoExecution.getMojoDescriptor().getPluginDescriptor().getGroupId() );
        assertEquals( "maven-resources-plugin",
                      mojoExecution.getMojoDescriptor().getPluginDescriptor().getArtifactId() );
        assertEquals( "0.1", mojoExecution.getMojoDescriptor().getPluginDescriptor().getVersion() );
    }
    public void testCalculationOfBuildPlanWithIndividualTaskOfTheCleanLifecycle()
        throws Exception
    {
        File pom = getProject( "project-basic" );
        MavenSession session = createMavenSession( pom );
        assertEquals( "project-basic", session.getCurrentProject().getArtifactId() );
        assertEquals( "1.0", session.getCurrentProject().getVersion() );
        List<MojoExecution> executionPlan = getExecutions( calculateExecutionPlan( session, "clean" ) );
        assertEquals( 1, executionPlan.size() );
        MojoExecution mojoExecution = executionPlan.get( 0 );
        assertNotNull( mojoExecution );
        assertEquals( "org.apache.maven.plugins",
                      mojoExecution.getMojoDescriptor().getPluginDescriptor().getGroupId() );
        assertEquals( "maven-clean-plugin", mojoExecution.getMojoDescriptor().getPluginDescriptor().getArtifactId() );
        assertEquals( "0.1", mojoExecution.getMojoDescriptor().getPluginDescriptor().getVersion() );
    }
    public void testCalculationOfBuildPlanWithIndividualTaskOfTheCleanCleanGoal()
        throws Exception
    {
        File pom = getProject( "project-basic" );
        MavenSession session = createMavenSession( pom );
        assertEquals( "project-basic", session.getCurrentProject().getArtifactId() );
        assertEquals( "1.0", session.getCurrentProject().getVersion() );
        List<MojoExecution> executionPlan = getExecutions( calculateExecutionPlan( session, "clean:clean" ) );
        assertEquals( 1, executionPlan.size() );
        MojoExecution mojoExecution = executionPlan.get( 0 );
        assertNotNull( mojoExecution );
        assertEquals( "org.apache.maven.plugins",
                      mojoExecution.getMojoDescriptor().getPluginDescriptor().getGroupId() );
        assertEquals( "maven-clean-plugin", mojoExecution.getMojoDescriptor().getPluginDescriptor().getArtifactId() );
        assertEquals( "0.1", mojoExecution.getMojoDescriptor().getPluginDescriptor().getVersion() );
    }
    List<MojoExecution> getExecutions( MavenExecutionPlan mavenExecutionPlan )
    {
        List<MojoExecution> result = new ArrayList<MojoExecution>();
        for ( ExecutionPlanItem executionPlanItem : mavenExecutionPlan )
        {
            result.add( executionPlanItem.getMojoExecution() );
        }
        return result;
    }
    public void testCalculationOfBuildPlanTasksOfTheCleanLifecycleAndTheInstallLifecycle()
        throws Exception
    {
        File pom = getProject( "project-with-additional-lifecycle-elements" );
        MavenSession session = createMavenSession( pom );
        assertEquals( "project-with-additional-lifecycle-elements", session.getCurrentProject().getArtifactId() );
        assertEquals( "1.0", session.getCurrentProject().getVersion() );
        List<MojoExecution> executionPlan = getExecutions( calculateExecutionPlan( session, "clean", "install" ) );
        assertEquals( 10, executionPlan.size() );
        assertEquals( "clean:clean", executionPlan.get( 0 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "resources:resources", executionPlan.get( 1 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "compiler:compile", executionPlan.get( 2 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:generate-metadata", executionPlan.get( 3 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "resources:testResources", executionPlan.get( 4 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "compiler:testCompile", executionPlan.get( 5 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:generate-test-metadata", executionPlan.get( 6 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "surefire:test", executionPlan.get( 7 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "jar:jar", executionPlan.get( 8 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "install:install", executionPlan.get( 9 ).getMojoDescriptor().getFullGoalName() );
    }
    public void testCalculationOfBuildPlanWithMultipleExecutionsOfModello()
        throws Exception
    {
        File pom = getProject( "project-with-multiple-executions" );
        MavenSession session = createMavenSession( pom );
        assertEquals( "project-with-multiple-executions", session.getCurrentProject().getArtifactId() );
        assertEquals( "1.0.1", session.getCurrentProject().getVersion() );
        MavenExecutionPlan plan = calculateExecutionPlan( session, "clean", "install" );
        List<MojoExecution> executions = getExecutions( plan );
        assertEquals( 16, executions.size() );
        assertEquals( "clean:clean", executions.get( 0 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:xpp3-writer", executions.get( 1 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:java", executions.get( 2 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:xpp3-reader", executions.get( 3 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:xpp3-writer", executions.get( 4 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:java", executions.get( 5 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:xpp3-reader", executions.get( 6 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "plugin:descriptor", executions.get( 7 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "resources:resources", executions.get( 8 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "compiler:compile", executions.get( 9 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "resources:testResources", executions.get( 10 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "compiler:testCompile", executions.get( 11 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "surefire:test", executions.get( 12 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "jar:jar", executions.get( 13 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "plugin:addPluginArtifactMetadata", executions.get( 14 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "install:install", executions.get( 15 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "src/main/mdo/remote-resources.mdo",
                      new MojoExecutionXPathContainer( executions.get( 1 ) ).getValue(
                          "configuration/models[1]/model" ) );
        assertEquals( "src/main/mdo/supplemental-model.mdo",
                      new MojoExecutionXPathContainer( executions.get( 4 ) ).getValue(
                          "configuration/models[1]/model" ) );
    }
    public void testLifecycleQueryingUsingADefaultLifecyclePhase()
        throws Exception
    {
        File pom = getProject( "project-with-additional-lifecycle-elements" );
        MavenSession session = createMavenSession( pom );
        assertEquals( "project-with-additional-lifecycle-elements", session.getCurrentProject().getArtifactId() );
        assertEquals( "1.0", session.getCurrentProject().getVersion() );
        List<MojoExecution> executionPlan = getExecutions( calculateExecutionPlan( session, "package" ) );
        assertEquals( 8, executionPlan.size() );
        assertEquals( "resources:resources", executionPlan.get( 0 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "compiler:compile", executionPlan.get( 1 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:generate-metadata", executionPlan.get( 2 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "resources:testResources", executionPlan.get( 3 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "compiler:testCompile", executionPlan.get( 4 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "it:generate-test-metadata", executionPlan.get( 5 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "surefire:test", executionPlan.get( 6 ).getMojoDescriptor().getFullGoalName() );
        assertEquals( "jar:jar", executionPlan.get( 7 ).getMojoDescriptor().getFullGoalName() );
    }
    public void testLifecyclePluginsRetrievalForDefaultLifecycle()
        throws Exception
    {
        List<Plugin> plugins =
            new ArrayList<Plugin>( lifecycleExecutor.getPluginsBoundByDefaultToAllLifecycles( "jar" ) );
        assertEquals( 8, plugins.size() );
    }
    public void testPluginConfigurationCreation()
        throws Exception
    {
        File pom = getProject( "project-with-additional-lifecycle-elements" );
        MavenSession session = createMavenSession( pom );
        MojoDescriptor mojoDescriptor =
            mojoDescriptorCreator.getMojoDescriptor( "org.apache.maven.its.plugins:maven-it-plugin:0.1:java", session,
                                                     session.getCurrentProject() );
        Xpp3Dom dom = MojoDescriptorCreator.convert( mojoDescriptor );
        System.out.println( dom );
    }
    MavenExecutionPlan calculateExecutionPlan( MavenSession session, String... tasks )
        throws Exception
    {
        List<TaskSegment> taskSegments =
            lifeCycleTaskSegmentCalculator.calculateTaskSegments( session, Arrays.asList( tasks ) );
        TaskSegment mergedSegment = new TaskSegment( false );
        for ( TaskSegment taskSegment : taskSegments )
        {
            mergedSegment.getTasks().addAll( taskSegment.getTasks() );
        }
        return lifeCycleExecutionPlanCalculator.calculateExecutionPlan( session, session.getCurrentProject(),
                                                                        mergedSegment.getTasks() );
    }
    public void testInvalidGoalName()
        throws Exception
    {
        File pom = getProject( "project-basic" );
        MavenSession session = createMavenSession( pom );
        try
        {
            getExecutions( calculateExecutionPlan( session, "resources:" ) );
            fail( "expected a MojoNotFoundException" );
        }
        catch ( MojoNotFoundException e )
        {
            assertEquals( "", e.getGoal() );
        }
        try
        {
            getExecutions( calculateExecutionPlan( session, "org.apache.maven.plugins:maven-resources-plugin:0.1:resources:toomany" ) );
            fail( "expected a MojoNotFoundException" );
        }
        catch ( MojoNotFoundException e )
        {
            assertEquals( "resources:toomany", e.getGoal() );
        }
    }
    public void testPluginPrefixRetrieval()
        throws Exception
    {
        File pom = getProject( "project-basic" );
        MavenSession session = createMavenSession( pom );
        Plugin plugin = mojoDescriptorCreator.findPluginForPrefix( "resources", session );
        assertEquals( "org.apache.maven.plugins", plugin.getGroupId() );
        assertEquals( "maven-resources-plugin", plugin.getArtifactId() );
    }
    public void testFindingPluginPrefixforCleanClean()
        throws Exception
    {
        File pom = getProject( "project-basic" );
        MavenSession session = createMavenSession( pom );
        Plugin plugin = mojoDescriptorCreator.findPluginForPrefix( "clean", session );
        assertNotNull( plugin );
    }
}
