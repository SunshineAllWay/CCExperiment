package org.apache.maven.repository;
import java.io.File;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Dependency;
public class VersionNotFoundException
    extends Exception
{
    private Dependency dependency;
    private String projectId;
    private File pomFile;
    private InvalidVersionSpecificationException cause;
    public VersionNotFoundException( String projectId, Dependency dependency, File pomFile,
                                     InvalidVersionSpecificationException cause )
    {
        super( projectId + ", " + formatLocationInPom( dependency ) + " " + dependency.getVersion() + ", pom file "
            + pomFile, cause );
        this.projectId = projectId;
        this.pomFile = pomFile;
        this.cause = cause;
        this.dependency = dependency;
    }
    private static String formatLocationInPom( Dependency dependency )
    {
        return "Dependency: " + ArtifactUtils.versionlessKey( dependency.getGroupId(), dependency.getArtifactId() );
    }
    public Dependency getDependency()
    {
        return dependency;
    }
    public String getProjectId()
    {
        return projectId;
    }
    public File getPomFile()
    {
        return pomFile;
    }
    public InvalidVersionSpecificationException getCauseException()
    {
        return cause;
    }
}
