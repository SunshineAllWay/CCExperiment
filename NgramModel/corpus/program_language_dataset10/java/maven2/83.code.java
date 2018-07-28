package org.apache.maven.artifact.repository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import java.util.HashMap;
import java.util.Map;
public class DefaultArtifactRepositoryFactory
    implements ArtifactRepositoryFactory
{
    private String globalUpdatePolicy;
    private String globalChecksumPolicy;
    private final Map artifactRepositories = new HashMap();
    public ArtifactRepository createDeploymentArtifactRepository( String id, String url,
                                                                  ArtifactRepositoryLayout repositoryLayout,
                                                                  boolean uniqueVersion )
    {
        return new DefaultArtifactRepository( id, url, repositoryLayout, uniqueVersion );
    }
    public ArtifactRepository createArtifactRepository( String id, String url,
                                                        ArtifactRepositoryLayout repositoryLayout,
                                                        ArtifactRepositoryPolicy snapshots,
                                                        ArtifactRepositoryPolicy releases )
    {
        boolean blacklisted = false;
        if ( artifactRepositories.containsKey( id ) )
        {
            ArtifactRepository repository = (ArtifactRepository) artifactRepositories.get( id );
            if ( repository.getUrl().equals( url ) )
            {
                blacklisted = repository.isBlacklisted();
            }
        }
        if ( snapshots == null )
        {
            snapshots = new ArtifactRepositoryPolicy();
        }
        if ( releases == null )
        {
            releases = new ArtifactRepositoryPolicy();
        }
        if ( globalUpdatePolicy != null )
        {
            snapshots.setUpdatePolicy( globalUpdatePolicy );
            releases.setUpdatePolicy( globalUpdatePolicy );
        }
        if ( globalChecksumPolicy != null )
        {
            snapshots.setChecksumPolicy( globalChecksumPolicy );
            releases.setChecksumPolicy( globalChecksumPolicy );
        }
        DefaultArtifactRepository repository = new DefaultArtifactRepository( id, url, repositoryLayout, snapshots,
                                                                              releases );
        repository.setBlacklisted( blacklisted );
        artifactRepositories.put( id, repository );
        return repository;
    }
    public void setGlobalUpdatePolicy( String updatePolicy )
    {
        this.globalUpdatePolicy = updatePolicy;
    }
    public void setGlobalChecksumPolicy( String checksumPolicy )
    {
        this.globalChecksumPolicy = checksumPolicy;
    }
}
