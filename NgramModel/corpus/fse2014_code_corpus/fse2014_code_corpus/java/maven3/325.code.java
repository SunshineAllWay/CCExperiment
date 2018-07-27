package org.apache.maven.configuration;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.codehaus.plexus.util.StringUtils;
public class DefaultBeanConfigurationRequest
    implements BeanConfigurationRequest
{
    private Object bean;
    private Object configuration;
    private String configurationElement;
    private ClassLoader classLoader;
    private BeanConfigurationValuePreprocessor valuePreprocessor;
    private BeanConfigurationPathTranslator pathTranslator;
    public Object getBean()
    {
        return bean;
    }
    public DefaultBeanConfigurationRequest setBean( Object bean )
    {
        this.bean = bean;
        return this;
    }
    public Object getConfiguration()
    {
        return configuration;
    }
    public String getConfigurationElement()
    {
        return configurationElement;
    }
    public DefaultBeanConfigurationRequest setConfiguration( Object configuration )
    {
        return setConfiguration( configuration, null );
    }
    public DefaultBeanConfigurationRequest setConfiguration( Object configuration, String element )
    {
        this.configuration = configuration;
        this.configurationElement = element;
        return this;
    }
    public DefaultBeanConfigurationRequest setConfiguration( Model model, String pluginGroupId,
                                                             String pluginArtifactId, String pluginExecutionId )
    {
        Plugin plugin = findPlugin( model, pluginGroupId, pluginArtifactId );
        if ( plugin != null )
        {
            if ( StringUtils.isNotEmpty( pluginExecutionId ) )
            {
                for ( PluginExecution execution : plugin.getExecutions() )
                {
                    if ( pluginExecutionId.equals( execution.getId() ) )
                    {
                        setConfiguration( execution.getConfiguration() );
                        break;
                    }
                }
            }
            else
            {
                setConfiguration( plugin.getConfiguration() );
            }
        }
        return this;
    }
    private Plugin findPlugin( Model model, String groupId, String artifactId )
    {
        if ( StringUtils.isEmpty( groupId ) )
        {
            throw new IllegalArgumentException( "group id for plugin has not been specified" );
        }
        if ( StringUtils.isEmpty( artifactId ) )
        {
            throw new IllegalArgumentException( "artifact id for plugin has not been specified" );
        }
        if ( model != null )
        {
            Build build = model.getBuild();
            if ( build != null )
            {
                for ( Plugin plugin : build.getPlugins() )
                {
                    if ( groupId.equals( plugin.getGroupId() ) && artifactId.equals( plugin.getArtifactId() ) )
                    {
                        return plugin;
                    }
                }
                PluginManagement mngt = build.getPluginManagement();
                if ( mngt != null )
                {
                    for ( Plugin plugin : mngt.getPlugins() )
                    {
                        if ( groupId.equals( plugin.getGroupId() ) && artifactId.equals( plugin.getArtifactId() ) )
                        {
                            return plugin;
                        }
                    }
                }
            }
        }
        return null;
    }
    public ClassLoader getClassLoader()
    {
        return classLoader;
    }
    public DefaultBeanConfigurationRequest setClassLoader( ClassLoader classLoader )
    {
        this.classLoader = classLoader;
        return this;
    }
    public BeanConfigurationValuePreprocessor getValuePreprocessor()
    {
        return valuePreprocessor;
    }
    public DefaultBeanConfigurationRequest setValuePreprocessor( BeanConfigurationValuePreprocessor valuePreprocessor )
    {
        this.valuePreprocessor = valuePreprocessor;
        return this;
    }
    public BeanConfigurationPathTranslator getPathTranslator()
    {
        return pathTranslator;
    }
    public DefaultBeanConfigurationRequest setPathTranslator( BeanConfigurationPathTranslator pathTranslator )
    {
        this.pathTranslator = pathTranslator;
        return this;
    }
}
