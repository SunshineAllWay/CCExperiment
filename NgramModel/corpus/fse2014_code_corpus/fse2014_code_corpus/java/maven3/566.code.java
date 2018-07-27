package org.apache.maven.lifecycle.internal;
import junit.framework.TestCase;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.lifecycle.internal.stub.LifecycleExecutionPlanCalculatorStub;
import org.apache.maven.lifecycle.internal.stub.ProjectDependencyGraphStub;
import org.apache.maven.plugin.MojoExecution;
import java.util.List;
public class PhaseRecorderTest extends TestCase
{
    public void testObserveExecution() throws Exception {
        PhaseRecorder phaseRecorder = new PhaseRecorder( ProjectDependencyGraphStub.A);
        MavenExecutionPlan plan = LifecycleExecutionPlanCalculatorStub.getProjectAExceutionPlan();
        final List<MojoExecution> executions = plan.getMojoExecutions();
        final MojoExecution mojoExecution1 = executions.get( 0 );
        final MojoExecution mojoExecution2 = executions.get( 1 );
        phaseRecorder.observeExecution( mojoExecution1 );
        assertTrue( ProjectDependencyGraphStub.A.hasLifecyclePhase( mojoExecution1.getLifecyclePhase() ));
        assertFalse( ProjectDependencyGraphStub.A.hasLifecyclePhase( mojoExecution2.getLifecyclePhase() ));
        assertFalse( phaseRecorder.isDifferentPhase( mojoExecution1));
        assertTrue( phaseRecorder.isDifferentPhase( mojoExecution2));
    }
}
