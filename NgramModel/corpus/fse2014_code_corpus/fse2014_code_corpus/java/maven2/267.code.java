package org.apache.maven.profiles;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.apache.maven.profiles.activation.ProfileActivationException;
import org.apache.maven.profiles.activation.ProfileActivator;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.SettingsUtils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
public class DefaultProfileManager
    implements ProfileManager
{
    private PlexusContainer container;
    private List activatedIds = new ArrayList();
    private List deactivatedIds = new ArrayList();
    private List defaultIds = new ArrayList();
    private Map profilesById = new LinkedHashMap();
    private Properties requestProperties;
    public DefaultProfileManager( PlexusContainer container )
    {
        this( container, (Settings) null );
    }
    public DefaultProfileManager( PlexusContainer container, Properties props )
    {
        this( container, (Settings) null, props );
    }
    public DefaultProfileManager( PlexusContainer container, Settings settings )
    {
        this.container = container;
        loadSettingsProfiles( settings );
    }
    public DefaultProfileManager( PlexusContainer container, Settings settings, Properties props )
    {
        this.container = container;
        loadSettingsProfiles( settings );
        if ( props != null )
        {
            requestProperties = props;
        }
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
            container.getLogger().warn( "Overriding profile: \'" + profileId + "\' (source: " + existing.getSource()
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
            container.getLogger().debug( "Profile with id: \'" + profileId + "\' has been explicitly activated." );
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
            container.getLogger().debug( "Profile with id: \'" + profileId + "\' has been explicitly deactivated." );
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
        List activeFromPom = new ArrayList();
        List activeExternal = new ArrayList();
        for ( Iterator it = profilesById.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Entry) it.next();
            String profileId = (String) entry.getKey();
            Profile profile = (Profile) entry.getValue();
            boolean shouldAdd = false;
            if ( activatedIds.contains( profileId ) )
            {
                shouldAdd = true;
            }
            else if ( isActive( profile ) )
            {
                shouldAdd = true;
            }
            if ( !deactivatedIds.contains( profileId ) && shouldAdd )
            {
                if ( "pom".equals( profile.getSource() ) )
                {
                    activeFromPom.add( profile );
                }
                else
                {
                    activeExternal.add( profile );
                }
            }
        }
        if ( activeFromPom.isEmpty() )
        {
            for ( Iterator it = defaultIds.iterator(); it.hasNext(); )
            {
                String profileId = (String) it.next();
                if ( deactivatedIds.contains( profileId ) )
                {
                    continue;
                }
                Profile profile = (Profile) profilesById.get( profileId );
                activeFromPom.add( profile );
            }
        }
        List allActive = new ArrayList( activeFromPom.size() + activeExternal.size() );
        allActive.addAll( activeExternal );
        allActive.addAll( activeFromPom );
        return allActive;
    }
    private boolean isActive( Profile profile )
        throws ProfileActivationException
    {
        List activators = null;
        Properties systemProperties = new Properties( System.getProperties() );
        if ( requestProperties != null )
        {
            systemProperties.putAll( requestProperties );
        }
        container.addContextValue( "SystemProperties", systemProperties );
        try
        {
            activators = container.lookupList( ProfileActivator.ROLE );
            for ( Iterator activatorIterator = activators.iterator(); activatorIterator.hasNext(); )
            {
                ProfileActivator activator = (ProfileActivator) activatorIterator.next();
                if ( activator.canDetermineActivation( profile ) )
                {
                    if ( activator.isActive( profile ) )
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        catch ( ComponentLookupException e )
        {
            throw new ProfileActivationException( "Cannot retrieve list of profile activators.", e );
        }
        finally
        {
            container.getContext().put( "SystemProperties", null );
            if ( activators != null )
            {
                try
                {
                    container.releaseAll( activators );
                }
                catch ( ComponentLifecycleException e )
                {
                    container.getLogger().debug( "Error releasing profile activators - ignoring.", e );
                }
            }
        }
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
    public void loadSettingsProfiles( Settings settings )
    {
        if ( settings == null )
        {
            return;
        }
        List settingsProfiles = settings.getProfiles();
        List settingsActiveProfileIds = settings.getActiveProfiles();
        explicitlyActivate( settingsActiveProfileIds );
        if ( settingsProfiles != null && !settingsProfiles.isEmpty() )
        {
            for ( Iterator it = settings.getProfiles().iterator(); it.hasNext(); )
            {
                org.apache.maven.settings.Profile rawProfile = (org.apache.maven.settings.Profile) it.next();
                Profile profile = SettingsUtils.convertFromSettingsProfile( rawProfile );
                addProfile( profile );
            }
        }
    }
}
