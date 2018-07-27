package org.apache.maven.artifact.repository;
import java.util.List;
public interface RepositoryRequest
{
    boolean isOffline();
    RepositoryRequest setOffline( boolean offline );
    boolean isForceUpdate();
    RepositoryRequest setForceUpdate( boolean forceUpdate );
    ArtifactRepository getLocalRepository();
    RepositoryRequest setLocalRepository( ArtifactRepository localRepository );
    List<ArtifactRepository> getRemoteRepositories();
    RepositoryRequest setRemoteRepositories( List<ArtifactRepository> remoteRepositories );
}
