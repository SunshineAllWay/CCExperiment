package org.apache.maven.artifact.versioning;
public interface ArtifactVersion
    extends Comparable
{
    int getMajorVersion();
    int getMinorVersion();
    int getIncrementalVersion();
    int getBuildNumber();
    String getQualifier();
    void parseVersion( String version );
}
