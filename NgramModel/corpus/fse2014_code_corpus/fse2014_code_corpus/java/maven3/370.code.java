package org.apache.maven.lifecycle.internal;
import org.apache.maven.project.MavenProject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ConcurrentBuildLogger
{
    private final long startTime;
    private final Map<MavenProject, Thread> threadMap = new ConcurrentHashMap<MavenProject, Thread>();
    public ConcurrentBuildLogger()
    {
        startTime = System.currentTimeMillis();
    }
    List<BuildLogItem> items = Collections.synchronizedList( new ArrayList<BuildLogItem>() );
    public BuildLogItem createBuildLogItem( MavenProject project, ExecutionPlanItem current )
    {
        threadMap.put( project, Thread.currentThread() );
        BuildLogItem result = new BuildLogItem( project, current );
        items.add( result );
        return result;
    }
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        for ( Map.Entry<MavenProject, Thread> mavenProjectThreadEntry : threadMap.entrySet() )
        {
            result.append( mavenProjectThreadEntry.getKey().getName() );
            result.append( " ran on " );
            result.append( mavenProjectThreadEntry.getValue().getName() );
            result.append( "\n" );
        }
        for ( BuildLogItem builtLogItem : items )
        {
            result.append( builtLogItem.toString( startTime ) );
            result.append( "\n" );
        }
        return result.toString();
    }
    public String toGraph()
    {
        StringBuilder result = new StringBuilder();
        Map<MavenProject, Collection<BuildLogItem>> multiMap = new HashMap<MavenProject, Collection<BuildLogItem>>();
        for ( BuildLogItem builtLogItem : items )
        {
            MavenProject project = builtLogItem.getProject();
            Collection<BuildLogItem> bag = multiMap.get( project );
            if ( bag == null )
            {
                bag = new ArrayList<BuildLogItem>();
                multiMap.put( project, bag );
            }
            bag.add( builtLogItem );
        }
        result.append( "digraph build" );
        result.append( " {\n " );
        for ( MavenProject mavenProject : multiMap.keySet() )
        {
            final Collection<BuildLogItem> builtLogItems = multiMap.get( mavenProject );
            result.append( "   subgraph " );
            result.append( mavenProject.getArtifactId() );
            result.append( "   {\n" );
            for ( BuildLogItem builtLogItem : builtLogItems )
            {
                result.append( builtLogItem.toGraph( startTime ) );
            }
            result.append( "\n   }\n" );
        }
        result.append( "\n}\n " );
        return result.toString();
    }
}
