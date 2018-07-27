package org.apache.maven.model.building;
public interface ModelCache
{
    void put( String groupId, String artifactId, String version, String tag, Object data );
    Object get( String groupId, String artifactId, String version, String tag );
}
