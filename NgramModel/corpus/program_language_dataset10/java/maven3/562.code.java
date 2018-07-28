package org.apache.maven.lifecycle.internal;
import junit.framework.TestCase;
import org.apache.maven.lifecycle.Schedule;
import org.apache.maven.lifecycle.internal.stub.MojoExecutorStub;
import org.apache.maven.plugin.MojoExecution;
public class ExecutionPlanItemTest
    extends TestCase
{
    public void testSetComplete()
        throws Exception
    {
        ExecutionPlanItem item = createExecutionPlanItem( "testMojo", null );
        item.setComplete();  
        assertTrue( item.isDone() );
    }
    public void testWaitUntilDone()
        throws Exception
    {
        final ExecutionPlanItem item =
            createExecutionPlanItem( "testMojo", createExecutionPlanItem( "testMojo2", null ) );
        new Thread( new Runnable()
        {
            public void run()
            {
                item.setComplete();
            }
        } ).start();
        item.waitUntilDone();
    }
    public static ExecutionPlanItem createExecutionPlanItem( String mojoDescription, ExecutionPlanItem downStream )
    {
        return createExecutionPlanItem( mojoDescription, downStream, null );
    }
    public static ExecutionPlanItem createExecutionPlanItem( String mojoDescription, ExecutionPlanItem downStream,
                                                             Schedule schedule )
    {
        return new ExecutionPlanItem( new MojoExecution( MojoExecutorStub.createMojoDescriptor( mojoDescription ) ),
                                      schedule );
    }
}
