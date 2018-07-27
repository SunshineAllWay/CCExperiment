package org.apache.maven.plugin.version;
public class PluginVersionNotFoundException
    extends Exception
{
    private final String groupId;
    private final String artifactId;
    public PluginVersionNotFoundException( String groupId, String artifactId )
    {
        super( "The plugin \'" + groupId + ":" + artifactId + "\' does not exist or no valid version could be found" );
        this.groupId = groupId;
        this.artifactId = artifactId;
    }
    public String getGroupId()
    {
        return groupId;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
}
