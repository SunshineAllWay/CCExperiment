package org.apache.cassandra.auth;
import java.util.EnumSet;
import java.util.List;
import org.apache.cassandra.config.ConfigurationException;
public class AllowAllAuthority implements IAuthority
{
    @Override
    public EnumSet<Permission> authorize(AuthenticatedUser user, List<Object> resource)
    {
        return Permission.ALL;
    }
    @Override    
    public void validateConfiguration() throws ConfigurationException
    {
    }
}
