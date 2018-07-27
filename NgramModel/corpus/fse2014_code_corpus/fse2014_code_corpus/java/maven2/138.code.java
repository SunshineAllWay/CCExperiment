package org.apache.maven.extension;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Extension;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.codehaus.plexus.PlexusContainerException;
import java.util.Map;
public interface ExtensionManager
{
    void addExtension( Extension extension, MavenProject project, ArtifactRepository localRepository )
        throws ArtifactResolutionException, PlexusContainerException, ArtifactNotFoundException;
    void registerWagons();
    Map<String, ArtifactHandler> getArtifactTypeHandlers();
    void addExtension( Extension extension, MavenProject project, ProjectBuilderConfiguration builderConfig )
        throws ArtifactResolutionException, PlexusContainerException, ArtifactNotFoundException;
}
