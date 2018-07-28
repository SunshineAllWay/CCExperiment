package org.apache.maven.profiles;
import org.apache.maven.model.Activation;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.profile.DefaultProfileActivationContext;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.profiles.activation.ProfileActivationException;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
@Deprecated
public class DefaultProfileManager
    implements ProfileManager
{
    @Requirement
    private Logger logger;
    @Requirement
    private ProfileSelector profileSelector;
    private List activatedIds = new ArrayList();
    private List deactivatedIds = new ArrayList();
    private List defaultIds = new ArrayList();
    private Map profilesById = new LinkedHashMap();
    private Properties requestProperties;
    public DefaultProfileManager( PlexusContainer container )
    {
        this( container, null );
    }
    public DefaultProfileManager( PlexusContainer container, Properties props )
    {
        try
        {
            this.profileSelector = container.lookup( ProfileSelector.class );
            this.logger = ( (MutablePlexusContainer) container ).getLogger();
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( e );
        }
        this.requestProperties = props;
    }
    public Properties getRequestProperties()
    {
        return requestProperties;
    }
    public Map getProfilesById()
    {
        return profilesById;
    }
    public void addProfile( Profile profile )
    {
        String profileId = profile.getId();
        Profile existing = (Profile) profilesById.get( profileId );
        if ( existing != null )
        {
            logger.warn( "Overriding profile: \'" + profileId + "\' (source: " + existing.getSource()
                + ") with new instance from source: " + profile.getSource() );
        }
        profilesById.put( profile.getId(), profile );
        Activation activation = profile.getActivation();
        if ( activation != null && activation.isActiveByDefault() )
        {
            activateAsDefault( profileId );
        }
    }
    public void explicitlyActivate( String profileId )
    {
        if ( !activatedIds.contains( profileId ) )
        {
            logger.debug( "Profile with id: \'" + profileId + "\' has been explicitly activated." );
            activatedIds.add( profileId );
        }
    }
    public void explicitlyActivate( List profileIds )
    {
        for ( Iterator it = profileIds.iterator(); it.hasNext(); )
        {
            String profileId = (String) it.next();
            explicitlyActivate( profileId );
        }
    }
    public void explicitlyDeactivate( String profileId )
    {
        if ( !deactivatedIds.contains( profileId ) )
        {
            logger.debug( "Profile with id: \'" + profileId + "\' has been explicitly deactivated." );
            deactivatedIds.add( profileId );
        }
    }
    public void explicitlyDeactivate( List profileIds )
    {
        for ( Iterator it = profileIds.iterator(); it.hasNext(); )
        {
            String profileId = (String) it.next();
            explicitlyDeactivate( profileId );
        }
    }
    public List getActiveProfiles()
        throws ProfileActivationException
    {
        DefaultProfileActivationContext context = new DefaultProfileActivationContext();
        context.setActiveProfileIds( activatedIds );
        context.setInactiveProfileIds( deactivatedIds );
        context.setSystemProperties( System.getProperties() );
        context.setUserProperties( requestProperties );
        final List<ProfileActivationException> errors = new ArrayList<ProfileActivationException>();
        List<Profile> profiles =
            profileSelector.getActiveProfiles( profilesById.values(), context, new ModelProblemCollector()
            {
                public void add( Severity severity, String message, InputLocation location, Exception cause )
                {
                    if ( !ModelProblem.Severity.WARNING.equals( severity ) )
                    {
                        errors.add( new ProfileActivationException( message, cause ) );
                    }
                }
            } );
        if ( !errors.isEmpty() )
        {
            throw errors.get( 0 );
        }
        return profiles;
    }
    public void addProfiles( List profiles )
    {
        for ( Iterator it = profiles.iterator(); it.hasNext(); )
        {
            Profile profile = (Profile) it.next();
            addProfile( profile );
        }
    }
    public void activateAsDefault( String profileId )
    {
        if ( !defaultIds.contains( profileId ) )
        {
            defaultIds.add( profileId );
        }
    }
    public List getExplicitlyActivatedIds()
    {
        return activatedIds;
    }
    public List getExplicitlyDeactivatedIds()
    {
        return deactivatedIds;
    }
    public List getIdsActivatedByDefault()
    {
        return defaultIds;
    }
}
