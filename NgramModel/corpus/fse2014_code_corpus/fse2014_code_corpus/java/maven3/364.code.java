package org.apache.maven.lifecycle;
import org.apache.maven.plugin.MojoExecution;
@SuppressWarnings( { "UnusedDeclaration" } )
public class Schedule
{
    private String phase;
    private String upstreamPhase; 
    private String pluginKey;
    private String mojoGoal;
    private boolean mojoSynchronized;
    private boolean parallel;
    public Schedule()
    {
    }
    public Schedule( String phase, boolean mojoSynchronized, boolean parallel )
    {
        this.phase = phase;
        this.mojoSynchronized = mojoSynchronized;
        this.parallel = parallel;
    }
    public String getPhase()
    {
        return phase;
    }
    public void setPhase( String phase )
    {
        this.phase = phase;
    }
    public String getPluginKey()
    {
        return pluginKey;
    }
    public void setPluginKey( String pluginKey )
    {
        this.pluginKey = pluginKey;
    }
    public boolean isMojoSynchronized()
    {
        return mojoSynchronized;
    }
    public void setMojoSynchronized( boolean mojoSynchronized )
    {
        this.mojoSynchronized = mojoSynchronized;
    }
    public boolean isParallel()
    {
        return parallel;
    }
    public void setParallel( boolean parallel )
    {
        this.parallel = parallel;
    }
    public String getUpstreamPhase()
    {
        return upstreamPhase;
    }
    public void setUpstreamPhase( String upstreamPhase )
    {
        this.upstreamPhase = upstreamPhase;
    }
    public String getMojoGoal()
    {
        return mojoGoal;
    }
    public void setMojoGoal( String mojoGoal )
    {
        this.mojoGoal = mojoGoal;
    }
    public boolean hasUpstreamPhaseDefined()
    {
        return getUpstreamPhase() != null;
    }
    public boolean appliesTo( MojoExecution mojoExecution )
    {
        boolean pluginKeyMatches = true;
        boolean pluginGoalMatches = true;
        if ( pluginKey == null && mojoGoal == null )
        {
            return false;
        }
        if ( pluginKey != null )
        {
            pluginKeyMatches = pluginKey.equals( mojoExecution.getPlugin().getKey() );
        }
        if ( mojoGoal != null )
        {
            pluginGoalMatches = mojoGoal.equals( mojoExecution.getGoal() );
        }
        if ( pluginKeyMatches && pluginGoalMatches )
        {
            return true;
        }
        return false;
    }
    @Override
    public String toString()
    {
        return "Schedule{" + "phase='" + phase + '\'' + ", upstreamPhase='" + upstreamPhase + '\'' + ", pluginKey='"
            + pluginKey + '\'' + ", mojoGoal='" + mojoGoal + '\'' + ", mojoSynchronized=" + mojoSynchronized
            + ", parallel=" + parallel + '}';
    }
}
