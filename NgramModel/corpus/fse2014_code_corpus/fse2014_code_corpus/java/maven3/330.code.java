package org.apache.maven.eventspy.internal;
import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
class EventSpyExecutionListener
    extends AbstractExecutionListener
{
    private final EventSpyDispatcher dispatcher;
    private final ExecutionListener delegate;
    public EventSpyExecutionListener( EventSpyDispatcher dispatcher, ExecutionListener delegate )
    {
        this.dispatcher = dispatcher;
        this.delegate = delegate;
    }
    @Override
    public void projectDiscoveryStarted( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.projectDiscoveryStarted( event );
    }
    @Override
    public void sessionStarted( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.sessionStarted( event );
    }
    @Override
    public void sessionEnded( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.sessionEnded( event );
    }
    @Override
    public void projectSkipped( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.projectSkipped( event );
    }
    @Override
    public void projectStarted( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.projectStarted( event );
    }
    @Override
    public void projectSucceeded( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.projectSucceeded( event );
    }
    @Override
    public void projectFailed( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.projectFailed( event );
    }
    @Override
    public void forkStarted( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.forkStarted( event );
    }
    @Override
    public void forkSucceeded( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.forkSucceeded( event );
    }
    @Override
    public void forkFailed( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.forkFailed( event );
    }
    @Override
    public void mojoSkipped( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.mojoSkipped( event );
    }
    @Override
    public void mojoStarted( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.mojoStarted( event );
    }
    @Override
    public void mojoSucceeded( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.mojoSucceeded( event );
    }
    @Override
    public void mojoFailed( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.mojoFailed( event );
    }
    @Override
    public void forkedProjectStarted( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.forkedProjectStarted( event );
    }
    @Override
    public void forkedProjectSucceeded( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.forkedProjectSucceeded( event );
    }
    @Override
    public void forkedProjectFailed( ExecutionEvent event )
    {
        dispatcher.onEvent( event );
        delegate.forkedProjectFailed( event );
    }
}
