package org.apache.maven;
public class BuildAbort
    extends Error
{
    public BuildAbort( String message )
    {
        super( message );
    }
    public BuildAbort( String message, Throwable cause )
    {
        super( message, cause );
    }
}
