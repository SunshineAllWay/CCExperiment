package org.apache.maven.plugin;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.util.xml.Xpp3Dom;
public class MojoExecution
{
    private Plugin plugin;
    private String goal;
    private String executionId;
    private MojoDescriptor mojoDescriptor;
    private Xpp3Dom configuration;
    public enum Source
    {
        CLI,
        LIFECYCLE,
    }
    private Source source;
    private String lifecyclePhase;
    private Map<String, List<MojoExecution>> forkedExecutions = new LinkedHashMap<String, List<MojoExecution>>();
    public MojoExecution( Plugin plugin, String goal, String executionId )
    {
        this.plugin = plugin;
        this.goal = goal;
        this.executionId = executionId;
    }
    public MojoExecution( MojoDescriptor mojoDescriptor )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.executionId = null;
        this.configuration = null;
    }
    public MojoExecution( MojoDescriptor mojoDescriptor, String executionId, Source source )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.executionId = executionId;
        this.configuration = null;
        this.source = source;
    }
    public MojoExecution( MojoDescriptor mojoDescriptor, String executionId )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.executionId = executionId;
        this.configuration = null;
    }
    public MojoExecution( MojoDescriptor mojoDescriptor, Xpp3Dom configuration )
    {
        this.mojoDescriptor = mojoDescriptor;
        this.configuration = configuration;
        this.executionId = null;
    }
    public Source getSource()
    {
        return source;
    }
    public String getExecutionId()
    {
        return executionId;
    }
    public Plugin getPlugin()
    {
        if ( mojoDescriptor != null )
        {
            return mojoDescriptor.getPluginDescriptor().getPlugin();
        }
        return plugin;
    }
    public MojoDescriptor getMojoDescriptor()
    {
        return mojoDescriptor;
    }
    public Xpp3Dom getConfiguration()
    {
        return configuration;
    }
    public void setConfiguration( Xpp3Dom configuration )
    {
        this.configuration = configuration;
    }
    public String identify()
    {
        StringBuilder sb = new StringBuilder( 256 );
        sb.append( executionId );
        sb.append( configuration.toString() );
        return sb.toString();
    }
    public String getLifecyclePhase()
    {
        return lifecyclePhase;
    }
    public void setLifecyclePhase( String lifecyclePhase )
    {
        this.lifecyclePhase = lifecyclePhase;
    }        
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        if ( mojoDescriptor != null )
        {
            buffer.append( mojoDescriptor.getId() );
        }
        buffer.append( " {execution: " ).append( executionId ).append( "}" );
        return buffer.toString();
    }
    public String getGroupId()
    {
        if ( mojoDescriptor != null )
        {
            return mojoDescriptor.getPluginDescriptor().getGroupId();
        }
        return plugin.getGroupId();
    }
    public String getArtifactId()
    {
        if ( mojoDescriptor != null )
        {
            return mojoDescriptor.getPluginDescriptor().getArtifactId();
        }
        return plugin.getArtifactId();
    }
    public String getVersion()
    {
        if ( mojoDescriptor != null )
        {
            return mojoDescriptor.getPluginDescriptor().getVersion();
        }        
        return plugin.getVersion();
    }
    public String getGoal()
    {
        if ( mojoDescriptor != null )
        {
            return mojoDescriptor.getGoal();
        }
        return goal;
    }
    public void setMojoDescriptor( MojoDescriptor mojoDescriptor )
    {
        this.mojoDescriptor = mojoDescriptor;
    }
    public Map<String, List<MojoExecution>> getForkedExecutions()
    {
        return forkedExecutions;
    }
    public void setForkedExecutions( String projectKey, List<MojoExecution> forkedExecutions )
    {
        this.forkedExecutions.put( projectKey, forkedExecutions );
    }
}
