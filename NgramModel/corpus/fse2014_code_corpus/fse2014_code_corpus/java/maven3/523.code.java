package org.apache.maven.rtinfo;
public interface RuntimeInformation
{
    String getMavenVersion();
    boolean isMavenVersion( String versionRange );
}
