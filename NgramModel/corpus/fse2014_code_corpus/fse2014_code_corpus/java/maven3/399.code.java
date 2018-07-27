package org.apache.maven.lifecycle.internal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public final class TaskSegment
{
    private final List<Object> tasks;
    private final boolean aggregating;
    public TaskSegment( boolean aggregating )
    {
        this.aggregating = aggregating;
        tasks = new ArrayList<Object>();
    }
    public TaskSegment( boolean aggregating, Object... tasks )
    {
        this.aggregating = aggregating;
        this.tasks = new ArrayList<Object>( Arrays.asList( tasks ) );
    }
    @Override
    public String toString()
    {
        return getTasks().toString();
    }
    public List<Object> getTasks()
    {
        return tasks;
    }
    public boolean isAggregating()
    {
        return aggregating;
    }
}
