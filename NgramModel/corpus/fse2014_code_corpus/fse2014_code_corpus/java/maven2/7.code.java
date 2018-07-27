package org.apache.maven.artifact;
public class InvalidRepositoryException
    extends Exception
{
    public InvalidRepositoryException( String message, Throwable throwable )
    {
        super( message, throwable );
    }
}
