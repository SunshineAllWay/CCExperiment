package org.apache.maven.artifact.repository;
@Deprecated
public interface RepositoryCache
{
    void put( RepositoryRequest request, Object key, Object data );
    Object get( RepositoryRequest request, Object key );
}
