package org.apache.maven.plugin.version.internal;
import org.apache.maven.plugin.version.PluginVersionResult;
import org.sonatype.aether.repository.ArtifactRepository;
class DefaultPluginVersionResult
    implements PluginVersionResult
{
    private String version;
    private ArtifactRepository repository;
    public DefaultPluginVersionResult()
    {
    }
    public DefaultPluginVersionResult( String version )
    {
        this.version = version;
    }
    public String getVersion()
    {
        return version;
    }
    public void setVersion( String version )
    {
        this.version = version;
    }
    public ArtifactRepository getRepository()
    {
        return repository;
    }
    public void setRepository( ArtifactRepository repository )
    {
        this.repository = repository;
    }
}
