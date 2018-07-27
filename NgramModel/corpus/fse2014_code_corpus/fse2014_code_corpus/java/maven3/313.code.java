package org.apache.maven.classrealm;
import java.io.File;
public interface ClassRealmConstituent
{
    String getGroupId();
    String getArtifactId();
    String getType();
    String getClassifier();
    String getVersion();
    File getFile();
}
