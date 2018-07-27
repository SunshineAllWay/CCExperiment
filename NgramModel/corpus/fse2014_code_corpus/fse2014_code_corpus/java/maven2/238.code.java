package org.apache.maven.monitor.event;
public interface EventMonitor
{
    void startEvent( String eventName, String target, long timestamp );
    void endEvent( String eventName, String target, long timestamp );
    void errorEvent( String eventName, String target, long timestamp, Throwable cause ); 
}