package org.apache.maven.lifecycle.internal;
import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.lifecycle.internal.stub.LifecycleExecutionPlanCalculatorStub;
import org.apache.maven.lifecycle.internal.stub.LoggerStub;
import org.apache.maven.lifecycle.internal.stub.ProjectDependencyGraphStub;
import java.util.HashSet;
public class BuilderCommonTest
    extends TestCase
{
    public void testResolveBuildPlan()
        throws Exception
    {
        MavenSession original = ProjectDependencyGraphStub.getMavenSession();
        final TaskSegment taskSegment1 = new TaskSegment( false );
        final MavenSession session1 = original.clone();
        session1.setCurrentProject( ProjectDependencyGraphStub.A );
        final BuilderCommon builderCommon = getBuilderCommon();
        final MavenExecutionPlan plan =
            builderCommon.resolveBuildPlan( session1, ProjectDependencyGraphStub.A, taskSegment1,
                                            new HashSet<Artifact>() );
        assertEquals( LifecycleExecutionPlanCalculatorStub.getProjectAExceutionPlan().size(), plan.size() );
    }
    public void testHandleBuildError()
        throws Exception
    {
    }
    public void testAttachToThread()
        throws Exception
    {
    }
    public void testGetKey()
        throws Exception
    {
    }
    public static BuilderCommon getBuilderCommon()
    {
        final LifecycleDebugLogger logger = new LifecycleDebugLogger( new LoggerStub() );
        return new BuilderCommon( logger, new LifecycleExecutionPlanCalculatorStub(),
                                  new LoggerStub() );
    }
}
