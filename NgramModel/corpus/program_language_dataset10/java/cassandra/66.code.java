package org.apache.cassandra.auth;
import java.util.Collections;
import java.util.Set;
public class AuthenticatedUser
{
    public final String username;
    public final Set<String> groups;
    public AuthenticatedUser(String username)
    {
        this.username = username;
        this.groups = Collections.emptySet();
    }
    public AuthenticatedUser(String username, Set<String> groups)
    {
        this.username = username;
        this.groups = Collections.unmodifiableSet(groups);
    }
    @Override
    public String toString()
    {
        return String.format("#<User %s groups=%s>", username, groups);
    }
}
