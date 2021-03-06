package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.lifecycle.DefaultSchedules;
import org.apache.maven.lifecycle.Schedule;
import org.apache.maven.lifecycle.Scheduling;
import java.util.Arrays;
import java.util.List;
public class DefaultSchedulesStub
{
    public static DefaultSchedules createDefaultSchedules()
    {
        return new DefaultSchedules( getSchedulingList() );
    }
    public static List<Scheduling> getSchedulingList()
    {
        return Arrays.asList( new Scheduling( "default", Arrays.asList( new Schedule( "compile", false, false ),
                                                                        new Schedule( "test", false, true ) ) ) );
    }
}