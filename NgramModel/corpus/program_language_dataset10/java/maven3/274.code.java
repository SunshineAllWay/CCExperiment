package org.apache.maven.artifact;
import java.net.MalformedURLException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
public class InvalidRepositoryException
    extends Exception
{
    private final String repositoryId;
    public InvalidRepositoryException( String message, String repositoryId, MalformedURLException cause )
    {
        super( message, cause );
        this.repositoryId = repositoryId;
    }
    protected InvalidRepositoryException( String message, String repositoryId, ComponentLookupException cause )
    {
        super( message, cause );
        this.repositoryId = repositoryId;
    }
    @Deprecated
    public InvalidRepositoryException( String message, Throwable t )
    {
        super( message );
        this.repositoryId = null;
    }
    public InvalidRepositoryException( String message, String repositoryId )
    {
        super( message );
        this.repositoryId = repositoryId;
    }
    public String getRepositoryId()
    {
        return repositoryId;
    }
}
