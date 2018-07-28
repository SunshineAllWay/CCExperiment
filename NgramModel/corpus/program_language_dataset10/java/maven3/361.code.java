package org.apache.maven.lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.lifecycle.internal.ExecutionPlanItem;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
public class MavenExecutionPlan
    implements Iterable<ExecutionPlanItem>
{
    private final List<ExecutionPlanItem> planItem;
    private final Map<String, ExecutionPlanItem> lastMojoExecutionForAllPhases;
    final List<String> phasesInExecutionPlan;
    public MavenExecutionPlan( List<ExecutionPlanItem> planItem, DefaultLifecycles defaultLifecycles )
    {
        this.planItem = planItem;
        lastMojoExecutionForAllPhases = new LinkedHashMap<String, ExecutionPlanItem>();
        LinkedHashSet<String> totalPhaseSet = new LinkedHashSet<String>();
        if ( defaultLifecycles != null )
        {
            for ( String phase : getDistinctPhasesInOrderOfExecutionPlanAppearance( planItem ) )
            {
                final Lifecycle lifecycle = defaultLifecycles.get( phase );
                if ( lifecycle != null )
                {
                    totalPhaseSet.addAll( lifecycle.getPhases() );
                }
            }
        }
        this.phasesInExecutionPlan = new ArrayList<String>( totalPhaseSet );
        Map<String, ExecutionPlanItem> lastInExistingPhases = new HashMap<String, ExecutionPlanItem>();
        for ( ExecutionPlanItem executionPlanItem : getExecutionPlanItems() )
        {
            lastInExistingPhases.put( executionPlanItem.getLifecyclePhase(), executionPlanItem );
        }
        ExecutionPlanItem lastSeenExecutionPlanItem = null;
        ExecutionPlanItem forThisPhase;
        for ( String phase : totalPhaseSet )
        {
            forThisPhase = lastInExistingPhases.get( phase );
            if ( forThisPhase != null )
            {
                lastSeenExecutionPlanItem = forThisPhase;
            }
            lastMojoExecutionForAllPhases.put( phase, lastSeenExecutionPlanItem );
        }
    }
    public Iterator<ExecutionPlanItem> iterator()
    {
        return getExecutionPlanItems().iterator();
    }
    public ExecutionPlanItem findLastInPhase( String requestedPhase )
    {
        return lastMojoExecutionForAllPhases.get( requestedPhase );
    }
    private List<ExecutionPlanItem> getExecutionPlanItems()
    {
        return planItem;
    }
    private static Iterable<String> getDistinctPhasesInOrderOfExecutionPlanAppearance(
        List<ExecutionPlanItem> planItems )
    {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for ( ExecutionPlanItem executionPlanItem : planItems )
        {
            final String phase = executionPlanItem.getLifecyclePhase();
            if ( !result.contains( phase ) )
            {
                result.add( phase );
            }
        }
        return result;
    }
    public void forceAllComplete()
    {
        for ( ExecutionPlanItem executionPlanItem : getExecutionPlanItems() )
        {
            executionPlanItem.forceComplete();
        }
    }
    public void waitUntilAllDone()
        throws InterruptedException
    {
        for ( ExecutionPlanItem executionPlanItem : getExecutionPlanItems() )
        {
            executionPlanItem.waitUntilDone();
        }
    }
    public boolean containsPhase( String phase )
    {
        return phasesInExecutionPlan.contains( phase );
    }
    public List<MojoExecution> getMojoExecutions()
    {
        List<MojoExecution> result = new ArrayList<MojoExecution>();
        for ( ExecutionPlanItem executionPlanItem : planItem )
        {
            result.add( executionPlanItem.getMojoExecution() );
        }
        return result;
    }
    public Set<Plugin> getNonThreadSafePlugins()
    {
        Set<Plugin> plugins = new HashSet<Plugin>();
        for ( ExecutionPlanItem executionPlanItem : planItem )
        {
            final MojoExecution mojoExecution = executionPlanItem.getMojoExecution();
            if ( !mojoExecution.getMojoDescriptor().isThreadSafe() )
            {
                plugins.add( mojoExecution.getPlugin() );
            }
        }
        return plugins;
    }
    @SuppressWarnings( { "UnusedDeclaration" } )
    @Deprecated
    public List<MojoExecution> getExecutions()
    {
        return getMojoExecutions();
    }
    public int size()
    {
        return planItem.size();
    }
}
