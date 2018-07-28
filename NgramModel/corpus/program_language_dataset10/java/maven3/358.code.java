package org.apache.maven.lifecycle;
public class LifecycleNotFoundException
    extends Exception
{
    private final String lifecycleId;
    public LifecycleNotFoundException( String lifecycleId )
    {
        super( "Unknown lifecycle " + lifecycleId );
        this.lifecycleId = ( lifecycleId != null ) ? lifecycleId : "";
    }
    public String getLifecycleId()
    {
        return lifecycleId;
    }
}
