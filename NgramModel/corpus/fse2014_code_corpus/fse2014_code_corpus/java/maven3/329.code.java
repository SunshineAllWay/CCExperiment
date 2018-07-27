package org.apache.maven.eventspy.internal;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionListener;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.aether.RepositoryListener;
@Component( role = EventSpyDispatcher.class )
public class EventSpyDispatcher
{
    @Requirement
    private Logger logger;
    @Requirement( role = EventSpy.class )
    private List<EventSpy> eventSpies;
    public void setEventSpies( List<EventSpy> eventSpies )
    {
        this.eventSpies = new ArrayList<EventSpy>( eventSpies );
    }
    public List<EventSpy> getEventSpies()
    {
        return eventSpies;
    }
    public ExecutionListener chainListener( ExecutionListener listener )
    {
        if ( eventSpies.isEmpty() )
        {
            return listener;
        }
        return new EventSpyExecutionListener( this, listener );
    }
    public RepositoryListener chainListener( RepositoryListener listener )
    {
        if ( eventSpies.isEmpty() )
        {
            return listener;
        }
        return new EventSpyRepositoryListener( this, listener );
    }
    public void init( EventSpy.Context context )
    {
        if ( eventSpies.isEmpty() )
        {
            return;
        }
        for ( EventSpy eventSpy : eventSpies )
        {
            try
            {
                eventSpy.init( context );
            }
            catch ( Exception e )
            {
                String msg = "Failed to initialize spy " + eventSpy.getClass().getName() + ": " + e.getMessage();
                if ( logger.isDebugEnabled() )
                {
                    logger.warn( msg, e );
                }
                else
                {
                    logger.warn( msg );
                }
            }
        }
    }
    public void onEvent( Object event )
    {
        if ( eventSpies.isEmpty() )
        {
            return;
        }
        for ( EventSpy eventSpy : eventSpies )
        {
            try
            {
                eventSpy.onEvent( event );
            }
            catch ( Exception e )
            {
                String msg = "Failed to forward event to spy " + eventSpy.getClass().getName() + ": " + e.getMessage();
                if ( logger.isDebugEnabled() )
                {
                    logger.warn( msg, e );
                }
                else
                {
                    logger.warn( msg );
                }
            }
        }
    }
    public void close()
    {
        if ( eventSpies.isEmpty() )
        {
            return;
        }
        for ( EventSpy eventSpy : eventSpies )
        {
            try
            {
                eventSpy.close();
            }
            catch ( Exception e )
            {
                String msg = "Failed to close spy " + eventSpy.getClass().getName() + ": " + e.getMessage();
                if ( logger.isDebugEnabled() )
                {
                    logger.warn( msg, e );
                }
                else
                {
                    logger.warn( msg );
                }
            }
        }
    }
}
