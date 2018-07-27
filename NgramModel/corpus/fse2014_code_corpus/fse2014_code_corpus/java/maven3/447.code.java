package org.apache.maven.plugin.internal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
class ValidatingConfigurationListener
    implements ConfigurationListener
{
    private final Object mojo;
    private final ConfigurationListener delegate;
    private final Map<String, Parameter> missingParameters;
    public ValidatingConfigurationListener( Object mojo, MojoDescriptor mojoDescriptor, ConfigurationListener delegate )
    {
        this.mojo = mojo;
        this.delegate = delegate;
        this.missingParameters = new HashMap<String, Parameter>();
        if ( mojoDescriptor.getParameters() != null )
        {
            for ( Parameter param : mojoDescriptor.getParameters() )
            {
                if ( param.isRequired() )
                {
                    missingParameters.put( param.getName(), param );
                }
            }
        }
    }
    public Collection<Parameter> getMissingParameters()
    {
        return missingParameters.values();
    }
    public void notifyFieldChangeUsingSetter( String fieldName, Object value, Object target )
    {
        delegate.notifyFieldChangeUsingSetter( fieldName, value, target );
        if ( mojo == target )
        {
            notify( fieldName, value );
        }
    }
    public void notifyFieldChangeUsingReflection( String fieldName, Object value, Object target )
    {
        delegate.notifyFieldChangeUsingReflection( fieldName, value, target );
        if ( mojo == target )
        {
            notify( fieldName, value );
        }
    }
    private void notify( String fieldName, Object value )
    {
        if ( value != null )
        {
            missingParameters.remove( fieldName );
        }
    }
}
