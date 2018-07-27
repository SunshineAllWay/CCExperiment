package org.apache.maven.plugin;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.codehaus.plexus.util.StringUtils;
public class PluginParameterException
    extends PluginConfigurationException
{
    private final List<Parameter> parameters;
    private final MojoDescriptor mojo;
    public PluginParameterException( MojoDescriptor mojo, List<Parameter> parameters )
    {
        super( mojo.getPluginDescriptor(), "The parameters " + format( parameters ) + " for goal "
            + mojo.getRoleHint() + " are missing or invalid" );
        this.mojo = mojo;
        this.parameters = parameters;
    }
    private static String format( List<Parameter> parameters )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        if ( parameters != null )
        {
            for ( Parameter parameter : parameters )
            {
                if ( buffer.length() > 0 )
                {
                    buffer.append( ", " );
                }
                buffer.append( '\'' ).append( parameter.getName() ).append( '\'' );
            }
        }
        return buffer.toString();
    }
    public MojoDescriptor getMojoDescriptor()
    {
        return mojo;
    }
    public List<Parameter> getParameters()
    {
        return parameters;
    }
    private static void decomposeParameterIntoUserInstructions( MojoDescriptor mojo, Parameter param,
                                                                StringBuilder messageBuffer )
    {
        String expression = param.getExpression();
        if ( param.isEditable() )
        {
            messageBuffer.append( "Inside the definition for plugin \'" + mojo.getPluginDescriptor().getArtifactId()
                + "\' specify the following:\n\n<configuration>\n  ...\n  <" + param.getName() + ">VALUE</"
                + param.getName() + ">\n</configuration>" );
            String alias = param.getAlias();
            if ( StringUtils.isNotEmpty( alias ) && !alias.equals( param.getName() ) )
            {
                messageBuffer.append(
                    "\n\n-OR-\n\n<configuration>\n  ...\n  <" + alias + ">VALUE</" + alias + ">\n</configuration>\n" );
            }
        }
        if ( StringUtils.isEmpty( expression ) )
        {
            messageBuffer.append( "." );
        }
        else
        {
            if ( param.isEditable() )
            {
                messageBuffer.append( "\n\n-OR-\n\n" );
            }
        }
    }
    public String buildDiagnosticMessage()
    {
        StringBuilder messageBuffer = new StringBuilder( 256 );
        List<Parameter> params = getParameters();
        MojoDescriptor mojo = getMojoDescriptor();
        messageBuffer.append( "One or more required plugin parameters are invalid/missing for \'" )
            .append( mojo.getPluginDescriptor().getGoalPrefix() ).append( ":" ).append( mojo.getGoal() )
            .append( "\'\n" );
        int idx = 0;
        for ( Iterator<Parameter> it = params.iterator(); it.hasNext(); idx++ )
        {
            Parameter param = it.next();
            messageBuffer.append( "\n[" ).append( idx ).append( "] " );
            decomposeParameterIntoUserInstructions( mojo, param, messageBuffer );
            messageBuffer.append( "\n" );
        }
        return messageBuffer.toString();
    }
}
