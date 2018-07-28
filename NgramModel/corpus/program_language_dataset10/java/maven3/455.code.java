package org.apache.maven.plugin.prefix.internal;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.prefix.PluginPrefixResult;
import org.sonatype.aether.repository.ArtifactRepository;
class DefaultPluginPrefixResult
    implements PluginPrefixResult
{
    private String groupId;
    private String artifactId;
    private ArtifactRepository repository;
    public DefaultPluginPrefixResult()
    {
    }
    public DefaultPluginPrefixResult( Plugin plugin )
    {
        groupId = plugin.getGroupId();
        artifactId = plugin.getArtifactId();
    }
    public DefaultPluginPrefixResult( String groupId, String artifactId, ArtifactRepository repository )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.repository = repository;
    }
    public String getGroupId()
    {
        return groupId;
    }
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
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
