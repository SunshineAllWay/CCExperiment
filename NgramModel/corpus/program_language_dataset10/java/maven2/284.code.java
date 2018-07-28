package org.apache.maven.project;
import org.apache.maven.project.validation.ModelValidationResult;
public class MavenProjectBuildingResult
{
    private MavenProject project;
    private ModelValidationResult modelValidationResult;
    private boolean successful;
    public MavenProjectBuildingResult( MavenProject project )
    {
        this.project = project;
        successful = true;
    }
    public MavenProjectBuildingResult( ModelValidationResult modelValidationResult )
    {
        this.modelValidationResult = modelValidationResult;
        successful = modelValidationResult.getMessageCount() == 0;
    }
    public ModelValidationResult getModelValidationResult()
    {
        return modelValidationResult;
    }
    public MavenProject getProject()
    {
        return project;
    }
    public boolean isSuccessful()
    {
        return successful;
    }
}
