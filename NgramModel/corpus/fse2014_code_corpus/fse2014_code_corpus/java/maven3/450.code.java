package org.apache.maven.plugin.prefix;
import java.util.List;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
public class NoPluginFoundForPrefixException
    extends Exception
{
    public NoPluginFoundForPrefixException( String prefix, List<String> pluginGroups, LocalRepository localRepository,
                                            List<RemoteRepository> remoteRepositories )
    {
        super( "No plugin found for prefix '" + prefix + "' in the current project and in the plugin groups "
            + pluginGroups + " available from the repositories " + format( localRepository, remoteRepositories ) );
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
