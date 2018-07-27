package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.Schedule;
import org.apache.maven.lifecycle.Scheduling;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static org.apache.maven.lifecycle.internal.stub.LifecycleExecutionPlanCalculatorStub.*;
public class DefaultLifecyclesStub
{
    public static DefaultLifecycles createDefaultLifecycles()
    {
        List<String> stubDefaultCycle =
            Arrays.asList( VALIDATE.getPhase(), INITIALIZE.getPhase(), PROCESS_RESOURCES.getPhase(), COMPILE.getPhase(),
                           TEST.getPhase(), PROCESS_TEST_RESOURCES.getPhase(), PACKAGE.getPhase(), "BEER",
                           INSTALL.getPhase() );
        List<String> stubCleanCycle = Arrays.asList( PRE_CLEAN.getPhase(), CLEAN.getPhase(), POST_CLEAN.getPhase() );
        List<String> stubSiteCycle =
            Arrays.asList( PRE_SITE.getPhase(), SITE.getPhase(), POST_SITE.getPhase(), SITE_DEPLOY.getPhase() );
        Iterator<List<String>> lcs = Arrays.asList( stubDefaultCycle, stubCleanCycle, stubSiteCycle ).iterator();
        Map<String, Lifecycle> lifeCycles = new HashMap<String, Lifecycle>();
        for ( String s : DefaultLifecycles.STANDARD_LIFECYCLES )
        {
            final Lifecycle lifecycle = new Lifecycle( s, lcs.next(), null );
            lifeCycles.put( s, lifecycle );
        }
        return new DefaultLifecycles( lifeCycles, new LoggerStub() );
    }
    public static List<Scheduling> getSchedulingList()
    {
        return Arrays.asList( new Scheduling( "default", Arrays.asList( new Schedule( "compile", false, false ),
                                                                        new Schedule( "test", false, true ) ) ) );
    }
}