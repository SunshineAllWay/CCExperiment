package org.apache.maven.lifecycle;
import java.util.Set;
import org.apache.maven.model.Plugin;
public interface LifeCyclePluginAnalyzer
{
    Set<Plugin> getPluginsBoundByDefaultToAllLifecycles( String packaging );
}
