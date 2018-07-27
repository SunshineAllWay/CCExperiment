package org.apache.cassandra.auth;
import java.util.EnumSet;
import java.util.List;
import org.apache.cassandra.config.ConfigurationException;
public interface IAuthority
{
    public EnumSet<Permission> authorize(AuthenticatedUser user, List<Object> resource);
    public void validateConfiguration() throws ConfigurationException;
}
