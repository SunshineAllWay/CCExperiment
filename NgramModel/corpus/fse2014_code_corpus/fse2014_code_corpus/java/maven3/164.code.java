package org.apache.maven.repository.metadata;
public class GraphConflictResolutionException
    extends Exception
{
    private static final long serialVersionUID = 2677613140287940255L;
    public GraphConflictResolutionException()
    {
    }
    public GraphConflictResolutionException( String message )
    {
        super( message );
    }
    public GraphConflictResolutionException( Throwable cause )
    {
        super( cause );
    }
    public GraphConflictResolutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
