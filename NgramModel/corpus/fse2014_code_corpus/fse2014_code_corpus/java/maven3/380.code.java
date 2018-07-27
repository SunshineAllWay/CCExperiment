package org.apache.maven.lifecycle.internal;
public final class GoalTask
{
    final String pluginGoal;
    public GoalTask( String pluginGoal )
    {
        this.pluginGoal = pluginGoal;
    }
    @Override
    public String toString()
    {
        return pluginGoal;
    }
}
