package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
@Component( role = LifecycleDebugLogger.class )
public class LifecycleDebugLogger
{
    @Requirement
    private Logger logger;
    @SuppressWarnings( { "UnusedDeclaration" } )
    public LifecycleDebugLogger()
    {
    }
    public LifecycleDebugLogger( Logger logger )
    {
        this.logger = logger;
    }
    public void debug( String s )
    {
        logger.debug( s );
    }
    public void info( String s )
    {
        logger.info( s );
    }
    public void debugReactorPlan( ProjectBuildList projectBuilds )
    {
        if ( !logger.isDebugEnabled() )
        {
            return;
        }
        logger.debug( "=== REACTOR BUILD PLAN ================================================" );
        for ( Iterator<ProjectSegment> it = projectBuilds.iterator(); it.hasNext(); )
        {
            ProjectSegment projectBuild = it.next();
            logger.debug( "Project: " + projectBuild.getProject().getId() );
            logger.debug( "Tasks:   " + projectBuild.getTaskSegment().getTasks() );
            logger.debug( "Style:   " + ( projectBuild.getTaskSegment().isAggregating() ? "Aggregating" : "Regular" ) );
            if ( it.hasNext() )
            {
                logger.debug( "-----------------------------------------------------------------------" );
            }
        }
        logger.debug( "=======================================================================" );
    }
    public void debugProjectPlan( MavenProject currentProject, MavenExecutionPlan executionPlan )
    {
        if ( !logger.isDebugEnabled() )
        {
            return;
        }
        logger.debug( "=== PROJECT BUILD PLAN ================================================" );
        logger.debug( "Project:       " + BuilderCommon.getKey( currentProject ) );
        debugDependencyRequirements( executionPlan.getMojoExecutions() );
        logger.debug( "Repositories (dependencies): " + currentProject.getRemoteProjectRepositories() );
        logger.debug( "Repositories (plugins)     : " + currentProject.getRemotePluginRepositories() );
        for ( ExecutionPlanItem mojoExecution : executionPlan )
        {
            debugMojoExecution( mojoExecution.getMojoExecution() );
        }
        logger.debug( "=======================================================================" );
    }
    private void debugMojoExecution( MojoExecution mojoExecution )
    {
        String mojoExecId =
            mojoExecution.getGroupId() + ':' + mojoExecution.getArtifactId() + ':' + mojoExecution.getVersion() + ':'
                + mojoExecution.getGoal() + " (" + mojoExecution.getExecutionId() + ')';
        Map<String, List<MojoExecution>> forkedExecutions = mojoExecution.getForkedExecutions();
        if ( !forkedExecutions.isEmpty() )
        {
            for ( Map.Entry<String, List<MojoExecution>> fork : forkedExecutions.entrySet() )
            {
                logger.debug( "--- init fork of " + fork.getKey() + " for " + mojoExecId + " ---" );
                debugDependencyRequirements( fork.getValue() );
                for ( MojoExecution forkedExecution : fork.getValue() )
                {
                    debugMojoExecution( forkedExecution );
                }
                logger.debug( "--- exit fork of " + fork.getKey() + " for " + mojoExecId + " ---" );
            }
        }
        logger.debug( "-----------------------------------------------------------------------" );
        logger.debug( "Goal:          " + mojoExecId );
        logger.debug(
            "Style:         " + ( mojoExecution.getMojoDescriptor().isAggregator() ? "Aggregating" : "Regular" ) );
        logger.debug( "Configuration: " + mojoExecution.getConfiguration() );
    }
    private void debugDependencyRequirements( List<MojoExecution> mojoExecutions )
    {
        Set<String> scopesToCollect = new TreeSet<String>();
        Set<String> scopesToResolve = new TreeSet<String>();
        for ( MojoExecution mojoExecution : mojoExecutions )
        {
            MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();
            String scopeToCollect = mojoDescriptor.getDependencyCollectionRequired();
            if ( StringUtils.isNotEmpty( scopeToCollect ) )
            {
                scopesToCollect.add( scopeToCollect );
            }
            String scopeToResolve = mojoDescriptor.getDependencyResolutionRequired();
            if ( StringUtils.isNotEmpty( scopeToResolve ) )
            {
                scopesToResolve.add( scopeToResolve );
            }
        }
        logger.debug( "Dependencies (collect): " + scopesToCollect );
        logger.debug( "Dependencies (resolve): " + scopesToResolve );
    }
    public void logWeavePlan( MavenSession session )
    {
        if ( !logger.isInfoEnabled() )
        {
            return;
        }
        final ProjectDependencyGraph dependencyGraph = session.getProjectDependencyGraph();
        logger.info( "=== WEAVE CONCURRENCY BUILD PLAN ======================================" );
        for ( MavenProject mavenProject : dependencyGraph.getSortedProjects() )
        {
            StringBuilder item = new StringBuilder();
            item.append( "Project: " );
            item.append( mavenProject.getArtifactId() );
            final List<MavenProject> upstreamProjects = dependencyGraph.getUpstreamProjects( mavenProject, false );
            if ( upstreamProjects.size() > 0 )
            {
                item.append( " ( " );
                for ( Iterator<MavenProject> it = upstreamProjects.iterator(); it.hasNext(); )
                {
                    final MavenProject kid = it.next();
                    item.append( kid.getArtifactId() );
                    if ( it.hasNext() )
                    {
                        item.append( ", " );
                    }
                }
                item.append( ")" );
            }
            logger.info( item.toString() );
        }
        logger.info( "=======================================================================" );
    }
}