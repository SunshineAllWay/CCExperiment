package org.apache.maven.eventspy;
import java.util.Map;
public interface EventSpy
{
    interface Context
    {
        Map<String, Object> getData();
    }
    void init( Context context )
        throws Exception;
    void onEvent( Object event )
        throws Exception;
    void close()
        throws Exception;
}
