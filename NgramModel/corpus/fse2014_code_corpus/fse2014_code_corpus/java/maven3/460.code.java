package org.apache.maven.plugin.version;
public interface PluginVersionResolver
{
    PluginVersionResult resolve( PluginVersionRequest request )
        throws PluginVersionResolutionException;
}
