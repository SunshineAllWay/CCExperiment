package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import java.util.List;
public class ArtifactResolutionException
    extends AbstractArtifactResolutionException
{
    public ArtifactResolutionException( String message, String groupId, String artifactId, String version, String type,
                                        String classifier, List remoteRepositories, List path, Throwable t )
    {
        super( message, groupId, artifactId, version, type, classifier, remoteRepositories, path, t );
    }
    public ArtifactResolutionException( String message, String groupId, String artifactId, String version, String type,
                                        String classifier, Throwable t )
    {
        super( message, groupId, artifactId, version, type, classifier, null, null, t );
    }
    public ArtifactResolutionException( String message, Artifact artifact )
    {
        super( message, artifact );
    }
    public ArtifactResolutionException( String message, Artifact artifact, List remoteRepositories )
    {
        super( message, artifact, remoteRepositories );
    }
    public ArtifactResolutionException( String message, Artifact artifact, Throwable t )
    {
        super( message, artifact, null, t );
    }
    protected ArtifactResolutionException( String message, Artifact artifact, List remoteRepositories, Throwable t )
    {
        super( message, artifact, remoteRepositories, t );
    }
}
