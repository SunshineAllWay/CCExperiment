package org.apache.maven.profiles.activation;
import java.util.Properties;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
public class SystemPropertyProfileActivator
    extends DetectedProfileActivator implements Contextualizable
{
    private Properties properties;
    public void contextualize(Context context) throws ContextException 
    {
        properties = (Properties)context.get("SystemProperties");
    }
    protected boolean canDetectActivation( Profile profile )
    {
        return profile.getActivation() != null && profile.getActivation().getProperty() != null;
    }
    public boolean isActive( Profile profile )
        throws ProfileActivationException
    {
        Activation activation = profile.getActivation();
        ActivationProperty property = activation.getProperty();
        if ( property != null )
        {
            String name = property.getName();
            boolean reverseName = false;
            if ( name == null )
            {
                throw new ProfileActivationException( "The property name is required to activate the profile '"
                    + profile.getId() + "'" );
            }
            if ( name.startsWith("!") )
            {
                reverseName = true;
                name = name.substring( 1 );
            }
            String sysValue = properties.getProperty( name );
            String propValue = property.getValue();
            if ( StringUtils.isNotEmpty( propValue ) )
            {
                boolean reverseValue = false;
                if ( propValue.startsWith( "!" ) )
                {
                    reverseValue = true;
                    propValue = propValue.substring( 1 );
                }
                boolean result = propValue.equals( sysValue );
                if ( reverseValue )
                {
                    return !result;
                }
                else
                {
                    return result;
                }
            }
            else
            {
                boolean result = StringUtils.isNotEmpty( sysValue );
                if ( reverseName )
                {
                    return !result;
                }
                else
                {
                    return result;
                }
            }
        }
        return false;
    }
}
