package org.apache.maven.execution;
public class MavenExecutionRequestPopulationException
    extends Exception
{
    public MavenExecutionRequestPopulationException( String message )
    {
        super( message );
    }
    public MavenExecutionRequestPopulationException( Throwable cause )
    {
        super( cause );
    }
    public MavenExecutionRequestPopulationException( String message,
                                   Throwable cause )
    {
        super( message, cause );
    }
}
