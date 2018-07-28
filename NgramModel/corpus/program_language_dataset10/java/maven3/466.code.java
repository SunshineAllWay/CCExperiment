package org.apache.maven.project;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Resource;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
@SuppressWarnings( "deprecation" )
@Component( role = MavenProjectHelper.class )
public class DefaultMavenProjectHelper
    extends AbstractLogEnabled
    implements MavenProjectHelper
{
    @Requirement
    private ArtifactHandlerManager artifactHandlerManager;
    public void attachArtifact( MavenProject project, String artifactType, String artifactClassifier,
                                File artifactFile )
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
        attachArtifact( project, artifact );
    }
    public void attachArtifact( MavenProject project, String artifactType, File artifactFile )
    {
        ArtifactHandler handler = artifactHandlerManager.getArtifactHandler( artifactType );
        Artifact artifact = new AttachedArtifact( project.getArtifact(), artifactType, handler );
        artifact.setFile( artifactFile );
        artifact.setResolved( true );
        attachArtifact( project, artifact );
    }
    public void attachArtifact( MavenProject project, File artifactFile, String artifactClassifier )
    {
        Artifact projectArtifact = project.getArtifact();
        Artifact artifact = new AttachedArtifact( projectArtifact, projectArtifact.getType(), artifactClassifier,
                                                  projectArtifact.getArtifactHandler() );
        artifact.setFile( artifactFile );
        artifact.setResolved( true );
        attachArtifact( project, artifact );
    }
    public void attachArtifact( MavenProject project, Artifact artifact )
    {
        try
        {
            project.addAttachedArtifact( artifact );
        }
        catch ( DuplicateArtifactAttachmentException dae )
        {
            getLogger().warn( dae.getMessage() );
            throw dae;
        }
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
