package org.apache.maven.plugin;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
public class PluginConfigurationException
    extends Exception
{
    private PluginDescriptor pluginDescriptor;
    private String originalMessage;
    public PluginConfigurationException( PluginDescriptor pluginDescriptor, String originalMessage )
    {
        super( originalMessage );
        this.pluginDescriptor = pluginDescriptor;
        this.originalMessage = originalMessage;
    }
    public PluginConfigurationException( PluginDescriptor pluginDescriptor, String originalMessage, Throwable cause )
    {
        super( originalMessage, cause );
        this.pluginDescriptor = pluginDescriptor;
        this.originalMessage = originalMessage;
    }
    public PluginConfigurationException( PluginDescriptor pluginDescriptor, String originalMessage,
                                         ExpressionEvaluationException cause )
    {
        super( originalMessage, cause );
        this.pluginDescriptor = pluginDescriptor;
        this.originalMessage = originalMessage;
    }
    public PluginConfigurationException( PluginDescriptor pluginDescriptor, String originalMessage,
                                         ComponentConfigurationException cause )
    {
        super( originalMessage, cause );
        this.pluginDescriptor = pluginDescriptor;
        this.originalMessage = originalMessage;
    }
    public PluginConfigurationException( PluginDescriptor pluginDescriptor, String originalMessage,
                                         ComponentLookupException cause )
    {
        super( originalMessage, cause );
        this.pluginDescriptor = pluginDescriptor;
        this.originalMessage = originalMessage;
    }
}
