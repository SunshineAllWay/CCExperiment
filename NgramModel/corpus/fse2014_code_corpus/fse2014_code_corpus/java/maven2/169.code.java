package org.apache.maven.reactor;
public class MavenExecutionException
    extends Exception
{
    public MavenExecutionException()
    {
    }
    public MavenExecutionException( String message )
    {
        super( message );
    }
    public MavenExecutionException( Throwable cause )
    {
        super( cause );
    }
    public MavenExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
