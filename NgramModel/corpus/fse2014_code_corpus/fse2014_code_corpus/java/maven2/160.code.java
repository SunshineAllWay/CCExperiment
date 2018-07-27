package org.apache.maven.plugin;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
public class PluginNotFoundException
    extends AbstractArtifactResolutionException
{
    public PluginNotFoundException( ArtifactNotFoundException e )
    {
        super( "Plugin could not be found - check that the goal name is correct: " + e.getMessage(), e.getGroupId(),
               e.getArtifactId(), e.getVersion(), "maven-plugin", null, e.getRemoteRepositories(), null, e.getCause() );
    }
}
