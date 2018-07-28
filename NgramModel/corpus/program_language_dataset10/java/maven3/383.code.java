package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleNotFoundException;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.project.MavenProject;
import java.util.List;
public interface LifecycleExecutionPlanCalculator
{
    MavenExecutionPlan calculateExecutionPlan( MavenSession session, MavenProject project, List<Object> tasks )
        throws PluginNotFoundException, PluginResolutionException, LifecyclePhaseNotFoundException,
        PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException,
        NoPluginFoundForPrefixException, LifecycleNotFoundException, PluginVersionResolutionException;
    MavenExecutionPlan calculateExecutionPlan( MavenSession session, MavenProject project, List<Object> tasks,
                                               boolean setup )
        throws PluginNotFoundException, PluginResolutionException, LifecyclePhaseNotFoundException,
        PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException,
        NoPluginFoundForPrefixException, LifecycleNotFoundException, PluginVersionResolutionException;
    void calculateForkedExecutions( MojoExecution mojoExecution, MavenSession session )
        throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
        PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
        LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException;
    void setupMojoExecution( MavenSession session, MavenProject project, MojoExecution mojoExecution )
        throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
        MojoNotFoundException, InvalidPluginDescriptorException, NoPluginFoundForPrefixException,
        LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException;
}
