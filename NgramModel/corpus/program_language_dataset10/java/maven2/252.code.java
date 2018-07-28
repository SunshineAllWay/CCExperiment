package org.apache.maven.plugin.descriptor;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
public class MojoDescriptor
    extends ComponentDescriptor
    implements Cloneable
{
    public static final String MAVEN_PLUGIN = "maven-plugin";
    public static final String SINGLE_PASS_EXEC_STRATEGY = "once-per-session";
    public static final String MULTI_PASS_EXEC_STRATEGY = "always";
    private static final String DEFAULT_INSTANTIATION_STRATEGY = "per-lookup";
    private static final String DEFAULT_LANGUAGE = "java";
    private List parameters;
    private Map parameterMap;
    private String executionStrategy = SINGLE_PASS_EXEC_STRATEGY;
    private String goal;
    private String phase;
    private String since;
    private String executePhase;
    private String executeGoal;
    private String executeLifecycle;
    private String deprecated;
    private boolean aggregator = false;
    private String dependencyResolutionRequired = null;
    private boolean projectRequired = true;
    private boolean onlineRequired = false;
    private PlexusConfiguration mojoConfiguration;
    private PluginDescriptor pluginDescriptor;
    private boolean inheritedByDefault = true;
    private boolean directInvocationOnly = false;
    private boolean requiresReports = false;
    public MojoDescriptor()
    {
        setInstantiationStrategy( DEFAULT_INSTANTIATION_STRATEGY );
        setComponentFactory( DEFAULT_LANGUAGE );
    }
    public String getLanguage()
    {
        return getComponentFactory();
    }
    public void setLanguage( String language )
    {
        setComponentFactory( language );
    }
    public String getDeprecated()
    {
        return deprecated;
    }
    public void setDeprecated( String deprecated )
    {
        this.deprecated = deprecated;
    }
    public List getParameters()
    {
        return parameters;
    }
    public void setParameters( List parameters )
        throws DuplicateParameterException
    {
        for ( Iterator it = parameters.iterator(); it.hasNext(); )
        {
            Parameter parameter = (Parameter) it.next();
            addParameter( parameter );
        }
    }
    public void addParameter( Parameter parameter )
        throws DuplicateParameterException
    {
        if ( parameters != null && parameters.contains( parameter ) )
        {
            throw new DuplicateParameterException( parameter.getName()
                + " has been declared multiple times in mojo with goal: " + getGoal() + " (implementation: "
                + getImplementation() + ")" );
        }
        if ( parameters == null )
        {
            parameters = new LinkedList();
        }
        parameters.add( parameter );
        parameterMap = null;
    }
    public Map getParameterMap()
    {
        if ( parameterMap == null )
        {
            parameterMap = new HashMap();
            if ( parameters != null )
            {
                for ( Iterator iterator = parameters.iterator(); iterator.hasNext(); )
                {
                    Parameter pd = (Parameter) iterator.next();
                    parameterMap.put( pd.getName(), pd );
                }
            }
        }
        return parameterMap;
    }
    public void setDependencyResolutionRequired( String requiresDependencyResolution )
    {
        this.dependencyResolutionRequired = requiresDependencyResolution;
    }
    public String isDependencyResolutionRequired()
    {
        return dependencyResolutionRequired;
    }
    public void setProjectRequired( boolean requiresProject )
    {
        this.projectRequired = requiresProject;
    }
    public boolean isProjectRequired()
    {
        return projectRequired;
    }
    public void setOnlineRequired( boolean requiresOnline )
    {
        this.onlineRequired = requiresOnline;
    }
    public boolean isOnlineRequired()
    {
        return onlineRequired;
    }
    public boolean requiresOnline()
    {
        return onlineRequired;
    }
    public String getPhase()
    {
        return phase;
    }
    public void setPhase( String phase )
    {
        this.phase = phase;
    }
    public String getSince()
    {
        return since;
    }
    public void setSince( String since )
    {
        this.since = since;
    }
    public String getGoal()
    {
        return goal;
    }
    public void setGoal( String goal )
    {
        this.goal = goal;
    }
    public String getExecutePhase()
    {
        return executePhase;
    }
    public void setExecutePhase( String executePhase )
    {
        this.executePhase = executePhase;
    }
    public boolean alwaysExecute()
    {
        return MULTI_PASS_EXEC_STRATEGY.equals( executionStrategy );
    }
    public String getExecutionStrategy()
    {
        return executionStrategy;
    }
    public void setExecutionStrategy( String executionStrategy )
    {
        this.executionStrategy = executionStrategy;
    }
    public PlexusConfiguration getMojoConfiguration()
    {
        if ( mojoConfiguration == null )
        {
            mojoConfiguration = new XmlPlexusConfiguration( "configuration" );
        }
        return mojoConfiguration;
    }
    public void setMojoConfiguration( PlexusConfiguration mojoConfiguration )
    {
        this.mojoConfiguration = mojoConfiguration;
    }
    public String getRole()
    {
        return Mojo.ROLE;
    }
    public String getRoleHint()
    {
        return getId();
    }
    public String getId()
    {
        return getPluginDescriptor().getId() + ":" + getGoal();
    }
    public String getFullGoalName()
    {
        return getPluginDescriptor().getGoalPrefix() + ":" + getGoal();
    }
    public String getComponentType()
    {
        return MAVEN_PLUGIN;
    }
    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }
    public void setPluginDescriptor( PluginDescriptor pluginDescriptor )
    {
        this.pluginDescriptor = pluginDescriptor;
    }
    public boolean isInheritedByDefault()
    {
        return inheritedByDefault;
    }
    public void setInheritedByDefault( boolean inheritedByDefault )
    {
        this.inheritedByDefault = inheritedByDefault;
    }
    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }
        if ( object instanceof MojoDescriptor )
        {
            MojoDescriptor other = (MojoDescriptor) object;
            if ( !compareObjects( getPluginDescriptor(), other.getPluginDescriptor() ) )
            {
                return false;
            }
            if ( !compareObjects( getGoal(), other.getGoal() ) )
            {
                return false;
            }
            return true;
        }
        return false;
    }
    private boolean compareObjects( Object first, Object second )
    {
        if ( ( first == null && second != null ) || ( first != null && second == null ) )
        {
            return false;
        }
        if ( !first.equals( second ) )
        {
            return false;
        }
        return true;
    }
    public int hashCode()
    {
        int result = 1;
        String goal = getGoal();
        if ( goal != null )
        {
            result += goal.hashCode();
        }
        PluginDescriptor pd = getPluginDescriptor();
        if ( pd != null )
        {
            result -= pd.hashCode();
        }
        return result;
    }
    public String getExecuteLifecycle()
    {
        return executeLifecycle;
    }
    public void setExecuteLifecycle( String executeLifecycle )
    {
        this.executeLifecycle = executeLifecycle;
    }
    public void setAggregator( boolean aggregator )
    {
        this.aggregator = aggregator;
    }
    public boolean isAggregator()
    {
        return aggregator;
    }
    public boolean isDirectInvocationOnly()
    {
        return directInvocationOnly;
    }
    public void setDirectInvocationOnly( boolean directInvocationOnly )
    {
        this.directInvocationOnly = directInvocationOnly;
    }
    public boolean isRequiresReports()
    {
        return requiresReports;
    }
    public void setRequiresReports( boolean requiresReports )
    {
        this.requiresReports = requiresReports;
    }
    public void setExecuteGoal( String executeGoal )
    {
        this.executeGoal = executeGoal;
    }
    public String getExecuteGoal()
    {
        return executeGoal;
    }
}
