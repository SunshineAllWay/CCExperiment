package org.apache.maven.project;
import java.io.File;
import java.util.List;
import org.apache.maven.model.building.ModelProblem;
public interface ProjectBuildingResult
{
    String getProjectId();
    File getPomFile();
    MavenProject getProject();
    List<ModelProblem> getProblems();
    DependencyResolutionResult getDependencyResolutionResult();
}
