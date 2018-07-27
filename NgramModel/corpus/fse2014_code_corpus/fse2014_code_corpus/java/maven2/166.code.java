package org.apache.maven.plugin.version;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.InvalidPluginException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
public interface PluginVersionManager
{
    String ROLE = PluginVersionManager.class.getName();
    String resolvePluginVersion( String groupId, String artifactId, MavenProject project, Settings settings,
                                 ArtifactRepository localRepository )
        throws PluginVersionResolutionException, InvalidPluginException, PluginVersionNotFoundException;
    String resolveReportPluginVersion( String groupId, String artifactId, MavenProject project, Settings settings,
                                       ArtifactRepository localRepository )
        throws PluginVersionResolutionException, InvalidPluginException, PluginVersionNotFoundException;
}
