package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.lifecycle.internal.ProjectSegment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
public class CompletionServiceStub
    implements CompletionService<ProjectSegment>
{
    List<FutureTask<ProjectSegment>> projectBuildFutureTasks =
        Collections.synchronizedList( new ArrayList<FutureTask<ProjectSegment>>() );
    final boolean finishImmediately;
    public int size()
    {
        return projectBuildFutureTasks.size();
    }
    public CompletionServiceStub( boolean finishImmediately )
    {
        this.finishImmediately = finishImmediately;
    }
    public Future<ProjectSegment> submit( Callable<ProjectSegment> task )
    {
        FutureTask<ProjectSegment> projectBuildFutureTask = new FutureTask<ProjectSegment>( task );
        projectBuildFutureTasks.add( projectBuildFutureTask );
        if ( finishImmediately )
        {
            projectBuildFutureTask.run();
        }
        return projectBuildFutureTask;
    }
    public Future<ProjectSegment> submit( Runnable task, ProjectSegment result )
    {
        FutureTask<ProjectSegment> projectBuildFutureTask = new FutureTask<ProjectSegment>( task, result );
        projectBuildFutureTasks.add( projectBuildFutureTask );
        if ( finishImmediately )
        {
            projectBuildFutureTask.run();
        }
        return projectBuildFutureTask;
    }
    public Future<ProjectSegment> take()
        throws InterruptedException
    {
        return null;
    }
    public Future<ProjectSegment> poll()
    {
        return null;
    }
    public Future<ProjectSegment> poll( long timeout, TimeUnit unit )
        throws InterruptedException
    {
        return null;
    }
}
