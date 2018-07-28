package org.apache.maven.project;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.building.ModelProblem;
class DefaultProjectBuildingResult
    implements ProjectBuildingResult
{
    private String projectId;
    private File pomFile;
    private MavenProject project;
    private List<ModelProblem> problems;
    private DependencyResolutionResult dependencyResolutionResult;
    public DefaultProjectBuildingResult( MavenProject project, List<ModelProblem> problems,
                                         DependencyResolutionResult dependencyResolutionResult )
    {
        this.projectId =
            ( project != null ) ? project.getGroupId() + ':' + project.getArtifactId() + ':' + project.getVersion()
                            : "";
        this.pomFile = ( project != null ) ? project.getFile() : null;
        this.project = project;
        this.problems = problems;
        this.dependencyResolutionResult = dependencyResolutionResult;
    }
    public DefaultProjectBuildingResult( String projectId, File pomFile, List<ModelProblem> problems )
    {
        this.projectId = ( projectId != null ) ? projectId : "";
        this.pomFile = pomFile;
        this.problems = problems;
    }
    public String getProjectId()
    {
        return projectId;
    }
    public File getPomFile()
    {
        return pomFile;
    }
    public MavenProject getProject()
    {
        return project;
    }
    public List<ModelProblem> getProblems()
    {
        if ( problems == null )
        {
            problems = new ArrayList<ModelProblem>();
        }
        return problems;
    }
    public DependencyResolutionResult getDependencyResolutionResult()
    {
        return dependencyResolutionResult;
    }
}
