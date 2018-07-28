package org.apache.maven.artifact.handler;
public class ArtifactHandlerMock
    implements ArtifactHandler
{
    private String extension, directory, classifier, packaging, language;
    private boolean includesDependencies, addedToClasspath;
    public void setExtension( String extension )
    {
        this.extension = extension;
    }
    public String getExtension()
    {
        return extension;
    }
    public void setDirectory( String directory )
    {
        this.directory = directory;
    }
    public String getDirectory()
    {
        return directory;
    }
    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }
    public String getClassifier()
    {
        return classifier;
    }
    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }
    public String getPackaging()
    {
        return packaging;
    }
    public void setIncludesDependencies( boolean includesDependencies )
    {
        this.includesDependencies = includesDependencies;
    }
    public boolean isIncludesDependencies()
    {
        return includesDependencies;
    }
    public void setLanguage( String language )
    {
        this.language = language;
    }
    public String getLanguage()
    {
        return language;
    }
    public void setAddedToClasspath( boolean addedToClasspath )
    {
        this.addedToClasspath = addedToClasspath;
    }
    public boolean isAddedToClasspath()
    {
        return addedToClasspath;
    }
}
