package org.apache.maven.artifact;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
public class UnknownRepositoryLayoutException
    extends InvalidRepositoryException
{
    private final String layoutId;
    public UnknownRepositoryLayoutException( String repositoryId, String layoutId )
    {
        super( "Cannot find ArtifactRepositoryLayout instance for: " + layoutId, repositoryId );
        this.layoutId = layoutId;
    }
    public UnknownRepositoryLayoutException( String repositoryId, String layoutId, ComponentLookupException e )
    {
        super( "Cannot find ArtifactRepositoryLayout instance for: " + layoutId, repositoryId, e );
        this.layoutId = layoutId;
    }
    public String getLayoutId()
    {
        return layoutId;
    }
}
