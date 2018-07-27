package org.apache.maven.plugin;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
public class MojoNotFoundException
    extends Exception
{
    private String goal;
    private PluginDescriptor pluginDescriptor;
    public MojoNotFoundException( String goal, PluginDescriptor pluginDescriptor )
    {
        super( toMessage( goal, pluginDescriptor ) );
        this.goal = goal;
        this.pluginDescriptor = pluginDescriptor;
    }
    public String getGoal()
    {
        return goal;
    }
    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }        
    private static String toMessage( String goal, PluginDescriptor pluginDescriptor )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "Could not find goal '" ).append( goal ).append( "'" );
        if ( pluginDescriptor != null )
        {
            buffer.append( " in plugin " ).append( pluginDescriptor.getId() );
            buffer.append( " among available goals " );
            List<MojoDescriptor> mojos = pluginDescriptor.getMojos();
            if ( mojos != null )
            {
                for ( Iterator<MojoDescriptor> it = mojos.iterator(); it.hasNext(); )
                {
                    MojoDescriptor mojo = it.next();
                    if ( mojo != null )
                    {
                        buffer.append( mojo.getGoal() );
                    }
                    if ( it.hasNext() )
                    {
                        buffer.append( ", " );
                    }
                }
            }
        }
        return buffer.toString();
    }
}
