package org.apache.cassandra.auth;
import java.util.Map;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.thrift.AuthenticationException;
public interface IAuthenticator
{
    public AuthenticatedUser defaultUser();
    public AuthenticatedUser authenticate(Map<? extends CharSequence,? extends CharSequence> credentials) throws AuthenticationException;
    public void validateConfiguration() throws ConfigurationException;
}
