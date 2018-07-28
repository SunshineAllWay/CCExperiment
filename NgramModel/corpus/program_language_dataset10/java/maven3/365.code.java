package org.apache.maven.lifecycle;
import org.apache.maven.plugin.MojoExecution;
import java.util.List;
public class Scheduling
{
    private String lifecycle;
    private List<Schedule> schedules;
    public Scheduling()
    {
    }
    public Scheduling( String lifecycle, List<Schedule> schedules )
    {
        this.lifecycle = lifecycle;
        this.schedules = schedules;
    }
    public String getLifecycle()
    {
        return lifecycle;
    }
    public void setLifecycle( String lifecycle )
    {
        this.lifecycle = lifecycle;
    }
    public List<Schedule> getSchedules()
    {
        return schedules;
    }
    public Schedule getSchedule( String phaseName )
    {
        if ( phaseName == null )
        {
            return null;
        }
        for ( Schedule schedule : schedules )
        {
            if ( phaseName.equals( schedule.getPhase() ) )
            {
                return schedule;
            }
        }
        return null;
    }
    public Schedule getSchedule( MojoExecution mojoExecution )
    {
        if ( mojoExecution == null )
        {
            return null;
        }
        for ( Schedule schedule : schedules )
        {
            if ( schedule.appliesTo( mojoExecution ) )
            {
                return schedule;
            }
        }
        return null;
    }
    public void setSchedules( List<Schedule> schedules )
    {
        this.schedules = schedules;
    }
}