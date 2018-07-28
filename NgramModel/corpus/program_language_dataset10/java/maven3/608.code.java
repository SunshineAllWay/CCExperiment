package org.apache.maven.repository;
import org.apache.maven.artifact.handler.ArtifactHandler;
class TestArtifactHandler
    implements ArtifactHandler
{
    private String type;
    private String extension;
    public TestArtifactHandler( String type )
    {
        this( type, type );
    }
    public TestArtifactHandler( String type, String extension )
    {
        this.type = type;
        this.extension = extension;
    }
    public String getClassifier()
    {
        return null;
    }
    public String getDirectory()
    {
        return getPackaging() + "s";
    }
    public String getExtension()
    {
        return extension;
    }
    public String getLanguage()
    {
        return "java";
    }
    public String getPackaging()
    {
        return type;
    }
    public boolean isAddedToClasspath()
    {
        return true;
    }
    public boolean isIncludesDependencies()
    {
        return false;
    }
}
