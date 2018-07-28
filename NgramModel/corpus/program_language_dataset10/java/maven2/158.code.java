package org.apache.maven.plugin;
public class PluginManagerException
    extends Exception
{
    public PluginManagerException( String message )
    {
        super( message );
    }
    public PluginManagerException( String message, Throwable e )
    {
        super( message, e );
    }
}
