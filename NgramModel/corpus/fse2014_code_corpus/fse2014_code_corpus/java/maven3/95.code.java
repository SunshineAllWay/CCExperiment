package org.apache.maven.profiles.activation;
@Deprecated
public class ProfileActivationException
    extends Exception
{
    private static final long serialVersionUID = -90820222109103638L;
    public ProfileActivationException( String message, Throwable cause )
    {
        super( message, cause );
    }
    public ProfileActivationException( String message )
    {
        super( message );
    }
}
