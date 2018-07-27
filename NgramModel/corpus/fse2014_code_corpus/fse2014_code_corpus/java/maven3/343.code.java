package org.apache.maven.execution;
public interface ExecutionListener
{
    void projectDiscoveryStarted( ExecutionEvent event );
    void sessionStarted( ExecutionEvent event );
    void sessionEnded( ExecutionEvent event );
    void projectSkipped( ExecutionEvent event );
    void projectStarted( ExecutionEvent event );
    void projectSucceeded( ExecutionEvent event );
    void projectFailed( ExecutionEvent event );
    void mojoSkipped( ExecutionEvent event );
    void mojoStarted( ExecutionEvent event );
    void mojoSucceeded( ExecutionEvent event );
    void mojoFailed( ExecutionEvent event );
    void forkStarted( ExecutionEvent event );
    void forkSucceeded( ExecutionEvent event );
    void forkFailed( ExecutionEvent event );
    void forkedProjectStarted( ExecutionEvent event );
    void forkedProjectSucceeded( ExecutionEvent event );
    void forkedProjectFailed( ExecutionEvent event );
}
