package org.apache.maven.plugin;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.model.Plugin;
public class PluginNotFoundException
    extends AbstractArtifactResolutionException
{
    private Plugin plugin;
    public PluginNotFoundException( Plugin plugin, ArtifactNotFoundException e )
    {
        super( "Plugin could not be found - check that the goal name is correct: " + e.getMessage(), e.getGroupId(),
               e.getArtifactId(), e.getVersion(), "maven-plugin", null, e.getRemoteRepositories(), null, e.getCause() );
        this.plugin = plugin;
    }
    public PluginNotFoundException( Plugin plugin, List<ArtifactRepository> remoteRepositories )
    {
        super( "Plugin could not be found, please check its coordinates for typos and ensure the required"
            + " plugin repositories are defined in the POM", plugin.getGroupId(), plugin.getArtifactId(),
               plugin.getVersion(), "maven-plugin", null, remoteRepositories, null );
        this.plugin = plugin;
    }
    public Plugin getPlugin()
    {
        return plugin;
    }
}
