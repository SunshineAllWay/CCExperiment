package org.apache.maven.artifact.manager;
public interface WagonProviderMapping
{
    String ROLE = WagonProviderMapping.class.getName();
    void setWagonProvider( String protocol, String provider );
    String getWagonProvider( String protocol );
}
