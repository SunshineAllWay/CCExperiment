package org.apache.maven.profiles.activation;
import org.apache.maven.model.Profile;
@Deprecated
public abstract class DetectedProfileActivator
    implements ProfileActivator
{
    public boolean canDetermineActivation( Profile profile )
    {
        return canDetectActivation( profile );
    }
    protected abstract boolean canDetectActivation( Profile profile );
}
