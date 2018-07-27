package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ExecutionEventCatapult.class )
public class DefaultExecutionEventCatapult
    implements ExecutionEventCatapult
{
    public void fire( ExecutionEvent.Type eventType, MavenSession session, MojoExecution mojoExecution )
    {
        fire( eventType, session, mojoExecution, null );
    }
    public void fire( ExecutionEvent.Type eventType, MavenSession session, MojoExecution mojoExecution,
                      Exception exception )
    {
        ExecutionListener listener = session.getRequest().getExecutionListener();
        if ( listener != null )
        {
            ExecutionEvent event = new DefaultExecutionEvent( eventType, session, mojoExecution, exception );
            switch ( eventType )
            {
                case ProjectDiscoveryStarted:
                    listener.projectDiscoveryStarted( event );
                    break;
                case SessionStarted:
                    listener.sessionStarted( event );
                    break;
                case SessionEnded:
                    listener.sessionEnded( event );
                    break;
                case ProjectSkipped:
                    listener.projectSkipped( event );
                    break;
                case ProjectStarted:
                    listener.projectStarted( event );
                    break;
                case ProjectSucceeded:
                    listener.projectSucceeded( event );
                    break;
                case ProjectFailed:
                    listener.projectFailed( event );
                    break;
                case MojoSkipped:
                    listener.mojoSkipped( event );
                    break;
                case MojoStarted:
                    listener.mojoStarted( event );
                    break;
                case MojoSucceeded:
                    listener.mojoSucceeded( event );
                    break;
                case MojoFailed:
                    listener.mojoFailed( event );
                    break;
                case ForkStarted:
                    listener.forkStarted( event );
                    break;
                case ForkSucceeded:
                    listener.forkSucceeded( event );
                    break;
                case ForkFailed:
                    listener.forkFailed( event );
                    break;
                case ForkedProjectStarted:
                    listener.forkedProjectStarted( event );
                    break;
                case ForkedProjectSucceeded:
                    listener.forkedProjectSucceeded( event );
                    break;
                case ForkedProjectFailed:
                    listener.forkedProjectFailed( event );
                    break;
                default:
                    throw new IllegalStateException( "Unknown execution event type " + eventType );
            }
        }
    }
}
