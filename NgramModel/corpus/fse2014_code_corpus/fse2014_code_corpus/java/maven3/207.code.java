package org.apache.maven.project;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.lifecycle.LifeCyclePluginAnalyzer;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
public class EmptyLifecyclePluginAnalyzer
    implements LifeCyclePluginAnalyzer
{
    public Set<Plugin> getPluginsBoundByDefaultToAllLifecycles( String packaging )
    {
        Set<Plugin> plugins;
        if ( "JAR".equals( packaging ) )
        {
            plugins = new LinkedHashSet<Plugin>();
            plugins.add( newPlugin( "maven-compiler-plugin", "compile", "testCompile" ) );
            plugins.add( newPlugin( "maven-resources-plugin", "resources", "testResources" ) );
            plugins.add( newPlugin( "maven-surefire-plugin", "test" ) );
            plugins.add( newPlugin( "maven-jar-plugin", "jar" ) );
            plugins.add( newPlugin( "maven-install-plugin", "install" ) );
            plugins.add( newPlugin( "maven-deploy-plugin", "deploy" ) );
        }
        else
        {
            plugins = Collections.emptySet();
        }
        return plugins;
    }
    private Plugin newPlugin( String artifactId, String... goals )
    {
        Plugin plugin = new Plugin();
        plugin.setGroupId( "org.apache.maven.plugins" );
        plugin.setArtifactId( artifactId );
        for ( String goal : goals )
        {
            PluginExecution pluginExecution = new PluginExecution();
            pluginExecution.setId( "default-" + goal );
            pluginExecution.addGoal( goal );
            plugin.addExecution( pluginExecution );
        }
        return plugin;
    }
}
