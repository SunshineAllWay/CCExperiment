package org.apache.maven.project;
public class DuplicateProjectException
    extends Exception
{
    public DuplicateProjectException( String message )
    {
        super( message );
    }
    public DuplicateProjectException( String message, Exception e )
    {
        super( message, e );
    }
}
