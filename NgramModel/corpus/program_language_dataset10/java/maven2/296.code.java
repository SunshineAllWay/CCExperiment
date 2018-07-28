package org.apache.maven.project.artifact;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
public class ProjectArtifactFactory
    extends DefaultArtifactFactory
{
    public Artifact create( MavenProject project )
    {
        ArtifactHandler handler = getArtifactHandlerManager().getArtifactHandler( project.getPackaging() );
        return new DefaultArtifact( project.getGroupId(), project.getArtifactId(),
                                    VersionRange.createFromVersion( project.getVersion() ), null,
                                    project.getPackaging(), null, handler, false );
    }
    public Artifact create( MavenProject project, String type, String classifier, boolean optional )
    {
        ArtifactHandler handler = getArtifactHandlerManager().getArtifactHandler( type );
        return new DefaultArtifact( project.getGroupId(), project.getArtifactId(),
                                    VersionRange.createFromVersion( project.getVersion() ), null,
                                    project.getPackaging(), null, handler, optional );
    }
}
