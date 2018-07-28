package org.apache.maven.repository;
public class MavenArtifactMetadata
{
    public static final String DEFAULT_TYPE = "jar";
    String groupId;
    String artifactId;
    String version;
    String classifier;
    String type;
    String scope;
    transient Object datum;
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
    public String getVersion()
    {
        return version;
    }
    public void setVersion( String version )
    {
        this.version = version;
    }
    public String getClassifier()
    {
        return classifier;
    }
    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }
    public String getType()
    {
        return type;
    }
    public void setType( String type )
    {
        this.type = type;
    }
    public Object getDatum()
    {
        return datum;
    }
    public void setDatum( Object datum )
    {
        this.datum = datum;
    }
    public String getScope()
    {
        return scope;
    }
    public void setScope( String scope )
    {
        this.scope = scope;
    }
    @Override
    public String toString()
    {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() + ":"
            + ( getClassifier() == null ? "" : getClassifier() ) + ":"
            + ( getType() == null ? DEFAULT_TYPE : getType() );
    }
}
