package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
@Component( role = LifecycleThreadedBuilder.class )
public class LifecycleThreadedBuilder
{
    @Requirement
    private Logger logger;
    @Requirement
    private LifecycleModuleBuilder lifecycleModuleBuilder;
    @SuppressWarnings( { "UnusedDeclaration" } )
    public LifecycleThreadedBuilder()
    {
    }
    public void build( MavenSession session, ReactorContext reactorContext, ProjectBuildList projectBuilds,
                       List<TaskSegment> currentTaskSegment, ConcurrencyDependencyGraph analyzer,
                       CompletionService<ProjectSegment> service )
    {
        ThreadOutputMuxer muxer = null; 
        for ( TaskSegment taskSegment : currentTaskSegment )
        {
            Map<MavenProject, ProjectSegment> projectBuildMap = projectBuilds.selectSegment( taskSegment );
                try
                {
                multiThreadedProjectTaskSegmentBuild( analyzer, reactorContext, session, service, taskSegment,
                                                      projectBuildMap, muxer );
                    if ( reactorContext.getReactorBuildStatus().isHalted( ) )
                    {
                        break;
                    }
                }
                catch ( Exception e )
                {
                    break;  
                }
        }
    }
    private void multiThreadedProjectTaskSegmentBuild( ConcurrencyDependencyGraph analyzer,
                                                       ReactorContext reactorContext, MavenSession rootSession,
                                                       CompletionService<ProjectSegment> service,
                                                       TaskSegment taskSegment,
                                                       Map<MavenProject, ProjectSegment> projectBuildList,
                                                       ThreadOutputMuxer muxer )
    {
        for ( MavenProject mavenProject : analyzer.getRootSchedulableBuilds() )
        {
            ProjectSegment projectSegment = projectBuildList.get( mavenProject );
            logger.debug( "Scheduling: " + projectSegment.getProject() );
            Callable<ProjectSegment> cb =
                createBuildCallable( rootSession, projectSegment, reactorContext, taskSegment, muxer );
            service.submit( cb );
        }
        for ( int i = 0; i < analyzer.getNumberOfBuilds(); i++ )
        {
            try
            {
                ProjectSegment projectBuild = service.take().get();
                if ( reactorContext.getReactorBuildStatus().isHalted() )
                {
                    break;
                }
                final List<MavenProject> newItemsThatCanBeBuilt =
                    analyzer.markAsFinished( projectBuild.getProject() );
                for ( MavenProject mavenProject : newItemsThatCanBeBuilt )
                {
                    ProjectSegment scheduledDependent = projectBuildList.get( mavenProject );
                    logger.debug( "Scheduling: " + scheduledDependent );
                    Callable<ProjectSegment> cb =
                        createBuildCallable( rootSession, scheduledDependent, reactorContext, taskSegment, muxer );
                    service.submit( cb );
                }
            }
            catch ( InterruptedException e )
            {
                break;
            }
            catch ( ExecutionException e )
            {
                break;
            }
        }
        Future<ProjectSegment> unprocessed;
        while ( ( unprocessed = service.poll() ) != null )
        {
            try
            {
                unprocessed.get();
            }
            catch ( InterruptedException e )
            {
                throw new RuntimeException( e );
            }
            catch ( ExecutionException e )
            {
                throw new RuntimeException( e );
            }
        }
    }
    private Callable<ProjectSegment> createBuildCallable( final MavenSession rootSession,
                                                          final ProjectSegment projectBuild,
                                                          final ReactorContext reactorContext,
                                                          final TaskSegment taskSegment, final ThreadOutputMuxer muxer )
    {
        return new Callable<ProjectSegment>()
        {
            public ProjectSegment call()
            {
                lifecycleModuleBuilder.buildProject( projectBuild.getSession(), rootSession, reactorContext,
                                                     projectBuild.getProject(), taskSegment );
                return projectBuild;
            }
        };
    }
}