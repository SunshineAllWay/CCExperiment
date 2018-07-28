package org.apache.maven.repository.legacy.resolver.conflict;
public class ConflictResolverNotFoundException
    extends Exception
{
    private static final long serialVersionUID = 3372412184339653914L;
    public ConflictResolverNotFoundException( String message )
    {
        super( message );
    }
}
