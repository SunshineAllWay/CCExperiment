package org.apache.maven.lifecycle;
public class LifecyclePhaseNotFoundException
    extends Exception
{
    private final String lifecyclePhase;
    public LifecyclePhaseNotFoundException( String message, String lifecyclePhase )
    {
        super( message );
        this.lifecyclePhase = ( lifecyclePhase != null ) ? lifecyclePhase : "";
    }
    public String getLifecyclePhase()
    {
        return lifecyclePhase;
    }
}
