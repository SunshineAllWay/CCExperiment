package org.apache.maven;
public class InternalErrorException
    extends MavenExecutionException
{
    public InternalErrorException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
