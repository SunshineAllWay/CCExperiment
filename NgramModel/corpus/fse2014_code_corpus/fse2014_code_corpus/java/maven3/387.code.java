package org.apache.maven.lifecycle.internal;
public final class LifecycleTask
{
    private final String lifecyclePhase;
    public LifecycleTask( String lifecyclePhase )
    {
        this.lifecyclePhase = lifecyclePhase;
    }
    @Override
    public String toString()
    {
        return getLifecyclePhase();
    }
    public String getLifecyclePhase()
    {
        return lifecyclePhase;
    }
}
