package org.apache.maven.model.profile.activation;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
@Component( role = ProfileActivator.class, hint = "property" )
public class PropertyProfileActivator
    implements ProfileActivator
{
    public boolean isActive( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        boolean active = false;
        Activation activation = profile.getActivation();
        if ( activation != null )
        {
            ActivationProperty property = activation.getProperty();
            if ( property != null )
            {
                String name = property.getName();
                boolean reverseName = false;
                if ( name != null && name.startsWith( "!" ) )
                {
                    reverseName = true;
                    name = name.substring( 1 );
                }
                if ( name == null || name.length() <= 0 )
                {
                    problems.add( Severity.ERROR, "The property name is required to activate the profile "
                        + profile.getId(), property.getLocation( "" ), null );
                    return false;
                }
                String sysValue = context.getUserProperties().get( name );
                if ( sysValue == null )
                {
                    sysValue = context.getSystemProperties().get( name );
                }
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
                        active = !result;
                    }
                    else
                    {
                        active = result;
                    }
                }
                else
                {
                    boolean result = StringUtils.isNotEmpty( sysValue );
                    if ( reverseName )
                    {
                        active = !result;
                    }
                    else
                    {
                        active = result;
                    }
                }
            }
        }
        return active;
    }
}
