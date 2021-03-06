package org.apache.maven;
import java.io.File;
import org.apache.maven.project.ProjectBuildingException;
public class MavenExecutionException
    extends Exception
{
    private File pomFile;
    public MavenExecutionException( String message, File pomFile )
    {
        super( message );
        this.pomFile = pomFile;
    }
    public MavenExecutionException( String message, File pomFile, ProjectBuildingException cause )
    {
        super( message, cause );
        this.pomFile = pomFile;
    }
    public MavenExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
    public File getPomFile()
    {
        return pomFile;
    }
}
