package org.apache.maven.repository.legacy;
import org.apache.maven.repository.ArtifactTransferResource;
import org.apache.maven.wagon.resource.Resource;
class MavenArtifact
    implements ArtifactTransferResource
{
    private String repositoryUrl;
    private Resource resource;
    private long transferStartTime;
    public MavenArtifact( String repositoryUrl, Resource resource )
    {
        if ( repositoryUrl == null )
        {
            this.repositoryUrl = "";
        }
        else if ( !repositoryUrl.endsWith( "/" ) && repositoryUrl.length() > 0 )
        {
            this.repositoryUrl = repositoryUrl + '/';
        }
        else
        {
            this.repositoryUrl = repositoryUrl;
        }
        this.resource = resource;
        this.transferStartTime = System.currentTimeMillis();
    }
    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }
    public String getName()
    {
        String name = resource.getName();
        if ( name == null )
        {
            name = "";
        }
        else if ( name.startsWith( "/" ) )
        {
            name = name.substring( 1 );
        }
        return name;
    }
    public String getUrl()
    {
        return getRepositoryUrl() + getName();
    }
    public long getContentLength()
    {
        return resource.getContentLength();
    }
    public long getTransferStartTime()
    {
        return transferStartTime;
    }
    @Override
    public String toString()
    {
        return getUrl();
    }
}
