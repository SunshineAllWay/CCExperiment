package org.apache.maven.plugin.version;
public class PluginVersionResolutionException
    extends Exception
{
    private final String groupId;
    private final String artifactId;
    private final String baseMessage;
    public PluginVersionResolutionException( String groupId, String artifactId, String baseMessage, Throwable cause )
    {
        super( "Error resolving version for \'" + groupId + ":" + artifactId + "\': " + baseMessage, cause );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseMessage = baseMessage;
    }
    public PluginVersionResolutionException( String groupId, String artifactId, String baseMessage )
    {
        super( "Error resolving version for \'" + groupId + ":" + artifactId + "\': " + baseMessage );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseMessage = baseMessage;
    }
    public String getGroupId()
    {
        return groupId;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
    public String getBaseMessage()
    {
        return baseMessage;
    }
}
