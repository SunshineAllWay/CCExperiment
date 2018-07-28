package org.apache.maven.lifecycle;
import org.apache.maven.AbstractCoreMavenComponentTestCase;
import org.apache.maven.exception.ExceptionHandler;
import org.apache.maven.lifecycle.internal.LifecycleDependencyResolver;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.lifecycle.internal.LifecycleModuleBuilder;
import org.apache.maven.lifecycle.internal.LifecycleTaskSegmentCalculator;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.codehaus.plexus.component.annotations.Requirement;
public class LifecycleExecutorSubModulesTest
    extends AbstractCoreMavenComponentTestCase
{
    @Requirement
    private DefaultLifecycles defaultLifeCycles;
    @Requirement
    private MojoExecutor mojoExecutor;
    @Requirement
    private LifecycleModuleBuilder lifeCycleBuilder;
    @Requirement
    private LifecycleDependencyResolver lifeCycleDependencyResolver;
    @Requirement
    private LifecycleExecutionPlanCalculator lifeCycleExecutionPlanCalculator;
    @Requirement
    private LifeCyclePluginAnalyzer lifeCyclePluginAnalyzer;
    @Requirement
    private LifecycleTaskSegmentCalculator lifeCycleTaskSegmentCalculator;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        defaultLifeCycles = lookup( DefaultLifecycles.class );
        mojoExecutor = lookup( MojoExecutor.class );
        lifeCycleBuilder = lookup( LifecycleModuleBuilder.class );
        lifeCycleDependencyResolver = lookup( LifecycleDependencyResolver.class );
        lifeCycleExecutionPlanCalculator = lookup( LifecycleExecutionPlanCalculator.class );
        lifeCyclePluginAnalyzer = lookup( LifeCyclePluginAnalyzer.class );
        lifeCycleTaskSegmentCalculator = lookup( LifecycleTaskSegmentCalculator.class );
        lookup( ExceptionHandler.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        defaultLifeCycles = null;
        super.tearDown();
    }
    protected String getProjectsDirectory()
    {
        return "src/test/projects/lifecycle-executor";
    }
    public void testCrweation()
        throws Exception
    {
        assertNotNull( defaultLifeCycles );
        assertNotNull( mojoExecutor );
        assertNotNull( lifeCycleBuilder );
        assertNotNull( lifeCycleDependencyResolver );
        assertNotNull( lifeCycleExecutionPlanCalculator );
        assertNotNull( lifeCyclePluginAnalyzer );
        assertNotNull( lifeCycleTaskSegmentCalculator );
    }
}