package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import java.util.List;
public class ArtifactNotFoundException
    extends AbstractArtifactResolutionException
{
    private String downloadUrl;
    protected ArtifactNotFoundException( String message, Artifact artifact, List remoteRepositories )
    {
        super( message, artifact, remoteRepositories );
    }
    public ArtifactNotFoundException( String message, Artifact artifact )
    {
        this( message, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(),
              artifact.getClassifier(), null, artifact.getDownloadUrl(), artifact.getDependencyTrail() );
    }
    protected ArtifactNotFoundException( String message, Artifact artifact, List remoteRepositories, Throwable t )
    {
        this( message, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), artifact.getClassifier(),
              remoteRepositories, artifact.getDownloadUrl(), artifact.getDependencyTrail(), t );
    }
    public ArtifactNotFoundException( String message, String groupId, String artifactId, String version, String type, String classifier,
                                      List remoteRepositories, String downloadUrl, List path, Throwable t )
    {
        super( constructMissingArtifactMessage( message, "", groupId, artifactId, version, type, classifier, downloadUrl, path ), groupId, artifactId,
               version, type, classifier, remoteRepositories, null, t );
        this.downloadUrl = downloadUrl;
    }
    private ArtifactNotFoundException( String message, String groupId, String artifactId, String version, String type,
                                       String classifier, List remoteRepositories, String downloadUrl, List path )
    {
        super( constructMissingArtifactMessage( message, "", groupId, artifactId, version, type, classifier, downloadUrl, path ), groupId, artifactId,
               version, type, classifier, remoteRepositories, null );
        this.downloadUrl = downloadUrl;
    }
    public String getDownloadUrl()
    {
        return downloadUrl;
    }
}
