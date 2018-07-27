package org.apache.maven.lifecycle;
public class LifecycleExecutionException
    extends Exception
{
    public LifecycleExecutionException( String message )
    {
        super( message );
    }
    public LifecycleExecutionException( Throwable cause )
    {
        super( cause );
    }
    public LifecycleExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
