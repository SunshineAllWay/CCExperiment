package org.apache.maven.plugin.version;
import org.sonatype.aether.repository.ArtifactRepository;
public interface PluginVersionResult
{
    String getVersion();
    ArtifactRepository getRepository();
}
