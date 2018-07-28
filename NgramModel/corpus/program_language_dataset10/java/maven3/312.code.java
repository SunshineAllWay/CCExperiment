package org.apache.maven.classrealm;
import java.io.File;
import org.sonatype.aether.artifact.Artifact;
class ArtifactClassRealmConstituent
    implements ClassRealmConstituent
{
    private final Artifact artifact;
    public ArtifactClassRealmConstituent( Artifact artifact )
    {
        this.artifact = artifact;
    }
    public String getGroupId()
    {
        return artifact.getGroupId();
    }
    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }
    public String getType()
    {
        return artifact.getExtension();
    }
    public String getClassifier()
    {
        return artifact.getClassifier();
    }
    public String getVersion()
    {
        return artifact.getBaseVersion();
    }
    public File getFile()
    {
        return artifact.getFile();
    }
    @Override
    public String toString()
    {
        return artifact.toString();
    }
}
