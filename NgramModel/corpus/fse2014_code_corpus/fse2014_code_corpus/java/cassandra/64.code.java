package org.apache.cassandra.auth;
import java.util.Map;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.thrift.AuthenticationException;
public class AllowAllAuthenticator implements IAuthenticator
{
    private final static AuthenticatedUser USER = new AuthenticatedUser("allow_all");
    @Override
    public AuthenticatedUser defaultUser()
    {
        return USER;
    }
    @Override
    public AuthenticatedUser authenticate(Map<? extends CharSequence,? extends CharSequence> credentials) throws AuthenticationException
    {
        return USER;
    }
    @Override    
    public void validateConfiguration() throws ConfigurationException
    {
    }
}
