package org.apache.maven.lifecycle.internal;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.BuildSuccess;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import java.util.HashSet;
@Component( role = LifecycleModuleBuilder.class )
public class LifecycleModuleBuilder
{
    @Requirement
    private MojoExecutor mojoExecutor;
    @Requirement
    private BuilderCommon builderCommon;
    @Requirement
    private ExecutionEventCatapult eventCatapult;
    public void buildProject( MavenSession session, ReactorContext reactorContext, MavenProject currentProject,
                              TaskSegment taskSegment )
    {
        buildProject( session, session, reactorContext, currentProject, taskSegment );
    }
    public void buildProject( MavenSession session, MavenSession rootSession, ReactorContext reactorContext,
                              MavenProject currentProject, TaskSegment taskSegment )
    {
        session.setCurrentProject( currentProject );
        long buildStartTime = System.currentTimeMillis();
        try
        {
            if ( reactorContext.getReactorBuildStatus().isHaltedOrBlacklisted( currentProject ) )
            {
                eventCatapult.fire( ExecutionEvent.Type.ProjectSkipped, session, null );
                return;
            }
            eventCatapult.fire( ExecutionEvent.Type.ProjectStarted, session, null );
            BuilderCommon.attachToThread( currentProject );
            MavenExecutionPlan executionPlan =
                builderCommon.resolveBuildPlan( session, currentProject, taskSegment, new HashSet<Artifact>() );
            mojoExecutor.execute( session, executionPlan.getMojoExecutions(), reactorContext.getProjectIndex() );
            long buildEndTime = System.currentTimeMillis();
            reactorContext.getResult().addBuildSummary(
                new BuildSuccess( currentProject, buildEndTime - buildStartTime ) );
            eventCatapult.fire( ExecutionEvent.Type.ProjectSucceeded, session, null );
        }
        catch ( Exception e )
        {
            builderCommon.handleBuildError( reactorContext, rootSession, currentProject, e, buildStartTime );
        }
        finally
        {
            session.setCurrentProject( null );
            Thread.currentThread().setContextClassLoader( reactorContext.getOriginalContextClassLoader() );
        }
    }
}