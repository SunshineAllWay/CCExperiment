package org.apache.maven.plugin.version;
import java.util.List;
import org.apache.maven.model.Model;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
public interface PluginVersionRequest
{
    String getGroupId();
    PluginVersionRequest setGroupId( String groupId );
    String getArtifactId();
    PluginVersionRequest setArtifactId( String artifactId );
    Model getPom();
    PluginVersionRequest setPom( Model pom );
    List<RemoteRepository> getRepositories();
    PluginVersionRequest setRepositories( List<RemoteRepository> repositories );
    RepositorySystemSession getRepositorySession();
    PluginVersionRequest setRepositorySession( RepositorySystemSession repositorySession );
}
