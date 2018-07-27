package org.apache.maven.execution;
import java.util.List;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
public interface MavenExecutionResult
{
    MavenExecutionResult setProject( MavenProject project );
    MavenProject getProject();
    MavenExecutionResult setTopologicallySortedProjects( List<MavenProject> projects );
    List<MavenProject> getTopologicallySortedProjects();
    MavenExecutionResult setDependencyResolutionResult( DependencyResolutionResult result );
    DependencyResolutionResult getDependencyResolutionResult();
    List<Throwable> getExceptions();
    MavenExecutionResult addException( Throwable e );
    boolean hasExceptions();
    BuildSummary getBuildSummary( MavenProject project );
    void addBuildSummary( BuildSummary summary );
}
