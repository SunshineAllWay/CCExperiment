package org.apache.maven.profiles.activation;
import org.apache.maven.model.Profile;
@Deprecated
public interface ProfileActivator
{
    final String ROLE = ProfileActivator.class.getName();
    boolean canDetermineActivation( Profile profile );
    boolean isActive( Profile profile )
        throws ProfileActivationException;
}
