package org.apache.maven.artifact;
public class InvalidArtifactRTException
    extends RuntimeException
{
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String baseMessage;
    public InvalidArtifactRTException( String groupId,
                                       String artifactId,
                                       String version,
                                       String type,
                                       String message )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.baseMessage = message;
    }
    public InvalidArtifactRTException( String groupId,
                                       String artifactId,
                                       String version,
                                       String type,
                                       String message,
                                       Throwable cause )
    {
        super( cause );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.baseMessage = message;
    }
    public String getMessage()
    {
        return "For artifact {" + getArtifactKey() + "}: " + getBaseMessage();
    }
    public String getBaseMessage()
    {
        return baseMessage;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
    public String getGroupId()
    {
        return groupId;
    }
    public String getType()
    {
        return type;
    }
    public String getVersion()
    {
        return version;
    }
    public String getArtifactKey()
    {
        return groupId + ":" + artifactId + ":" + version + ":" + type;
    }
}
