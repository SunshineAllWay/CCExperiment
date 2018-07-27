package org.apache.maven.repository;
import java.io.IOException;
public class LocalRepositoryNotAccessibleException
    extends IOException
{
    public LocalRepositoryNotAccessibleException( String message, Throwable cause )
    {
        super( message );
        initCause( cause );
    }
    public LocalRepositoryNotAccessibleException( String message )
    {
        super( message );
    }
}
