package org.apache.maven.plugin.version;
import java.util.List;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
public class PluginVersionResolutionException
    extends Exception
{
    private final String groupId;
    private final String artifactId;
    private final String baseMessage;
    public PluginVersionResolutionException( String groupId, String artifactId, String baseMessage, Throwable cause )
    {
        super( "Error resolving version for plugin \'" + groupId + ":" + artifactId + "\': " + baseMessage, cause );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseMessage = baseMessage;
    }
    public PluginVersionResolutionException( String groupId, String artifactId, String baseMessage )
    {
        super( "Error resolving version for plugin \'" + groupId + ":" + artifactId + "\': " + baseMessage );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseMessage = baseMessage;
    }
    public PluginVersionResolutionException( String groupId, String artifactId, LocalRepository localRepository,
                                             List<RemoteRepository> remoteRepositories, String baseMessage )
    {
        super( "Error resolving version for plugin \'" + groupId + ":" + artifactId + "\' from the repositories "
            + format( localRepository, remoteRepositories ) + ": " + baseMessage );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseMessage = baseMessage;
    }
    public String getGroupId()
    {
        return groupId;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
    public String getBaseMessage()
    {
        return baseMessage;
    }
    private static String format( LocalRepository localRepository, List<RemoteRepository> remoteRepositories )
    {
        String repos = "[";
        if ( localRepository != null )
        {
            repos += localRepository.getId() + " (" + localRepository.getBasedir() + ")";
        }
        if ( remoteRepositories != null && !remoteRepositories.isEmpty() )
        {
            for ( RemoteRepository repository : remoteRepositories )
            {
                repos += ", ";
                if ( repository != null )
                {
                    repos += repository.getId() + " (" + repository.getUrl() + ")";
                }
            }
        }
        repos += "]";
        return repos;
    }
}
