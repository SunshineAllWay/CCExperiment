package org.apache.maven.plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import java.util.Map;
public abstract class AbstractMojo
    implements Mojo, ContextEnabled
{
    private Log log;
    private Map pluginContext;
    public void setLog( Log log )
    {
        this.log = log;
    }
    public Log getLog()
    {
        if ( log == null )
        {
            log = new SystemStreamLog();
        }
        return log;
    }
    public Map getPluginContext()
    {
        return pluginContext;
    }
    public void setPluginContext( Map pluginContext )
    {
        this.pluginContext = pluginContext;
    }
}
