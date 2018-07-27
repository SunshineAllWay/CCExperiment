package org.apache.maven.plugin.prefix;
import java.util.List;
import org.apache.maven.model.Model;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
public interface PluginPrefixRequest
{
    String getPrefix();
    PluginPrefixRequest setPrefix( String prefix );
    List<String> getPluginGroups();
    PluginPrefixRequest setPluginGroups( List<String> pluginGroups );
    Model getPom();
    PluginPrefixRequest setPom( Model pom );
    List<RemoteRepository> getRepositories();
    PluginPrefixRequest setRepositories( List<RemoteRepository> repositories );
    RepositorySystemSession getRepositorySession();
    PluginPrefixRequest setRepositorySession( RepositorySystemSession repositorySession );
}
