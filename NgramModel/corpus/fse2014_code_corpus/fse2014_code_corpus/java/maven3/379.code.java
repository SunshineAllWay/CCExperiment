package org.apache.maven.lifecycle.internal;
import org.apache.maven.lifecycle.Schedule;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import java.util.concurrent.CountDownLatch;
public class ExecutionPlanItem
{
    private final MojoExecution mojoExecution;
    private final Schedule schedule;
    private final CountDownLatch done = new CountDownLatch( 1 );
    public ExecutionPlanItem( MojoExecution mojoExecution, Schedule schedule )
    {
        this.mojoExecution = mojoExecution;
        this.schedule = schedule;
    }
    public MojoExecution getMojoExecution()
    {
        return mojoExecution;
    }
    public String getLifecyclePhase()
    {
        return mojoExecution.getLifecyclePhase();
    }
    public void setComplete()
    {
        done.countDown();
    }
    public boolean isDone()
    {
        return done.getCount() < 1;
    }
    public void forceComplete()
    {
        setComplete();
    }
    public void waitUntilDone()
        throws InterruptedException
    {
        done.await();
    }
    public Schedule getSchedule()
    {
        return schedule;
    }
    public Plugin getPlugin()
    {
        final MojoDescriptor mojoDescriptor = getMojoExecution().getMojoDescriptor();
        return mojoDescriptor.getPluginDescriptor().getPlugin();
    }
    @Override
    public String toString()
    {
        return "ExecutionPlanItem{" + ", mojoExecution=" + mojoExecution + ", schedule=" + schedule + '}' +
            super.toString();
    }
}
