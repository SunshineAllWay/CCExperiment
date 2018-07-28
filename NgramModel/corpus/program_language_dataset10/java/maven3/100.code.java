package org.apache.maven.project;
import java.io.File;
import org.apache.maven.project.validation.ModelValidationResult;
@Deprecated
public class InvalidProjectModelException
    extends ProjectBuildingException
{
    private ModelValidationResult validationResult;
    public InvalidProjectModelException( String projectId, String message, File pomLocation )
    {
        super( projectId, message, pomLocation );
    }
    public InvalidProjectModelException( String projectId, String pomLocation, String message,
                                         ModelValidationResult validationResult )
    {
        this( projectId, message, new File( pomLocation ), validationResult );
    }
    public InvalidProjectModelException( String projectId, String message, File pomFile,
                                         ModelValidationResult validationResult )
    {
        super( projectId, message, pomFile );
        this.validationResult = validationResult;
    }
    public InvalidProjectModelException( String projectId, String pomLocation, String message )
    {
        this( projectId, message, new File( pomLocation ) );
    }
    public final ModelValidationResult getValidationResult()
    {
        return validationResult;
    }
}
