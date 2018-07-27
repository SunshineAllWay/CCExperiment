package org.apache.maven.eventspy;
public abstract class AbstractEventSpy
    implements EventSpy
{
    public void init( Context context )
        throws Exception
    {
    }
    public void onEvent( Object event )
        throws Exception
    {
    }
    public void close()
        throws Exception
    {
    }
}
