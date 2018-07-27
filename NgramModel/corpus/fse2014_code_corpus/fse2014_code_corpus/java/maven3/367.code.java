package org.apache.maven.lifecycle.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Component( role = BuildListCalculator.class )
public class BuildListCalculator
{
    public ProjectBuildList calculateProjectBuilds( MavenSession session, List<TaskSegment> taskSegments )
    {
        List<ProjectSegment> projectBuilds = new ArrayList<ProjectSegment>();
        MavenProject rootProject = session.getTopLevelProject();
        for ( TaskSegment taskSegment : taskSegments )
        {
            List<MavenProject> projects;
            if ( taskSegment.isAggregating() )
            {
                projects = Collections.singletonList( rootProject );
            }
            else
            {
                projects = session.getProjects();
            }
            for ( MavenProject project : projects )
            {
                BuilderCommon.attachToThread( project ); 
                MavenSession copiedSession = session.clone();
                copiedSession.setCurrentProject( project );
                projectBuilds.add( new ProjectSegment( project, taskSegment, copiedSession ) );
            }
        }
        return new ProjectBuildList( projectBuilds );
    }
}
