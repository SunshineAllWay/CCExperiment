package org.apache.maven.profiles.activation;
import org.apache.maven.model.Profile;
public interface ProfileActivator
{
    static final String ROLE = ProfileActivator.class.getName();
    boolean canDetermineActivation( Profile profile );
    boolean isActive( Profile profile )
        throws ProfileActivationException;
}
