package org.apache.maven.project;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Resource;
import org.apache.maven.project.artifact.AttachedArtifact;
import java.io.File;
import java.util.List;
public class DefaultMavenProjectHelper
    implements MavenProjectHelper
{
    private ArtifactHandlerManager artifactHandlerManager;
    public void attachArtifact( MavenProject project, String artifactType, String artifactClassifier, File artifactFile )
    {
        String type = artifactType;
        ArtifactHandler handler = null;
        if ( type != null )
        {
            handler = artifactHandlerManager.getArtifactHandler( artifactType );
        }
        if ( handler == null )
        {
            handler = artifactHandlerManager.getArtifactHandler( "jar" );
        }
        Artifact artifact = new AttachedArtifact( project.getArtifact(), artifactType, artifactClassifier, handler );
        artifact.setFile( artifactFile );
        artifact.setResolved( true );
        project.addAttachedArtifact( artifact );
    }
    public void attachArtifact( MavenProject project, String artifactType, File artifactFile )
    {
        ArtifactHandler handler = artifactHandlerManager.getArtifactHandler( artifactType );
        Artifact artifact = new AttachedArtifact( project.getArtifact(), artifactType, handler );
        artifact.setFile( artifactFile );
        artifact.setResolved( true );
        project.addAttachedArtifact( artifact );
    }
    public void attachArtifact( MavenProject project, File artifactFile, String artifactClassifier )
    {
        Artifact projectArtifact = project.getArtifact();
        Artifact artifact = new AttachedArtifact( projectArtifact, projectArtifact.getType(), artifactClassifier, projectArtifact.getArtifactHandler() );
        artifact.setFile( artifactFile );
        artifact.setResolved( true );
        project.addAttachedArtifact( artifact );
    }
    public void addResource( MavenProject project, String resourceDirectory, List includes, List excludes )
    {
        Resource resource = new Resource();
        resource.setDirectory( resourceDirectory );
        resource.setIncludes( includes );
        resource.setExcludes( excludes );
        project.addResource( resource );
    }
    public void addTestResource( MavenProject project, String resourceDirectory, List includes, List excludes )
    {
        Resource resource = new Resource();
        resource.setDirectory( resourceDirectory );
        resource.setIncludes( includes );
        resource.setExcludes( excludes );
        project.addTestResource( resource );
    }
}
