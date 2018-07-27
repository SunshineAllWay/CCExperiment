package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.GoalTask;
import org.apache.maven.lifecycle.internal.LifecycleTask;
import org.apache.maven.lifecycle.internal.DefaultLifecycleTaskSegmentCalculator;
import org.apache.maven.lifecycle.internal.TaskSegment;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import java.util.ArrayList;
import java.util.List;
public class LifecycleTaskSegmentCalculatorStub
    extends DefaultLifecycleTaskSegmentCalculator
{
    public static final String clean = "clean";
    public static final String aggr = "aggr";
    public static final String install = "install";
    public List<TaskSegment> calculateTaskSegments( MavenSession session, List<String> tasks )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        PluginVersionResolutionException
    {
        List<TaskSegment> taskSegments = new ArrayList<TaskSegment>( tasks.size() );
        TaskSegment currentSegment = null;
        for ( String task : tasks )
        {
            if ( aggr.equals( task ) )
            {
                boolean aggregating = true;
                if ( currentSegment == null || currentSegment.isAggregating() != aggregating )
                {
                    currentSegment = new TaskSegment( aggregating );
                    taskSegments.add( currentSegment );
                }
                currentSegment.getTasks().add( new GoalTask( task ) );
            }
            else
            {
                if ( currentSegment == null || currentSegment.isAggregating() )
                {
                    currentSegment = new TaskSegment( false );
                    taskSegments.add( currentSegment );
                }
                currentSegment.getTasks().add( new LifecycleTask( task ) );
            }
        }
        return taskSegments;
    }
    public boolean requiresProject( MavenSession session )
    {
        return true;
    }
}
