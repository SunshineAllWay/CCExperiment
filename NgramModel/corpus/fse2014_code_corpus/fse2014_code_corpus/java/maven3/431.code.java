package org.apache.maven.plugin;
import org.apache.maven.model.Plugin;
public class PluginIncompatibleException
    extends PluginManagerException
{
    public PluginIncompatibleException( Plugin plugin, String message )
    {
        super( plugin, message, (Throwable) null );
    }
}
