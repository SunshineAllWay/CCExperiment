package org.apache.maven.settings;
import java.util.List;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.settings.merge.MavenSettingsMerger;
public final class SettingsUtils
{
    private SettingsUtils()
    {
    }
    public static void merge( Settings dominant, Settings recessive, String recessiveSourceLevel )
    {
        new MavenSettingsMerger().merge( dominant, recessive, recessiveSourceLevel );
    }
    public static org.apache.maven.model.Profile convertFromSettingsProfile( Profile settingsProfile )
    {
        org.apache.maven.model.Profile profile = new org.apache.maven.model.Profile();
        profile.setId( settingsProfile.getId() );
        profile.setSource( "settings.xml" );
        Activation settingsActivation = settingsProfile.getActivation();
        if ( settingsActivation != null )
        {
            org.apache.maven.model.Activation activation = new org.apache.maven.model.Activation();
            activation.setActiveByDefault( settingsActivation.isActiveByDefault() );
            activation.setJdk( settingsActivation.getJdk() );
            ActivationProperty settingsProp = settingsActivation.getProperty();
            if ( settingsProp != null )
            {
                org.apache.maven.model.ActivationProperty prop = new org.apache.maven.model.ActivationProperty();
                prop.setName( settingsProp.getName() );
                prop.setValue( settingsProp.getValue() );
                activation.setProperty( prop );
            }
            ActivationOS settingsOs = settingsActivation.getOs();
            if ( settingsOs != null )
            {
                org.apache.maven.model.ActivationOS os = new org.apache.maven.model.ActivationOS();
                os.setArch( settingsOs.getArch() );
                os.setFamily( settingsOs.getFamily() );
                os.setName( settingsOs.getName() );
                os.setVersion( settingsOs.getVersion() );
                activation.setOs( os );
            }
            org.apache.maven.settings.ActivationFile settingsFile = settingsActivation.getFile();
            if ( settingsFile != null )
            {
                ActivationFile file = new ActivationFile();
                file.setExists( settingsFile.getExists() );
                file.setMissing( settingsFile.getMissing() );
                activation.setFile( file );
            }
            profile.setActivation( activation );
        }
        profile.setProperties( settingsProfile.getProperties() );
        List<Repository> repos = settingsProfile.getRepositories();
        if ( repos != null )
        {
            for ( Repository repo : repos )
            {
                profile.addRepository( convertFromSettingsRepository( repo ) );
            }
        }
        List<Repository> pluginRepos = settingsProfile.getPluginRepositories();
        if ( pluginRepos != null )
        {
            for ( Repository pluginRepo : pluginRepos )
            {
                profile.addPluginRepository( convertFromSettingsRepository( pluginRepo ) );
            }
        }
        return profile;
    }
    private static org.apache.maven.model.Repository convertFromSettingsRepository( Repository settingsRepo )
    {
        org.apache.maven.model.Repository repo = new org.apache.maven.model.Repository();
        repo.setId( settingsRepo.getId() );
        repo.setLayout( settingsRepo.getLayout() );
        repo.setName( settingsRepo.getName() );
        repo.setUrl( settingsRepo.getUrl() );
        if ( settingsRepo.getSnapshots() != null )
        {
            repo.setSnapshots( convertRepositoryPolicy( settingsRepo.getSnapshots() ) );
        }
        if ( settingsRepo.getReleases() != null )
        {
            repo.setReleases( convertRepositoryPolicy( settingsRepo.getReleases() ) );
        }
        return repo;
    }
    private static org.apache.maven.model.RepositoryPolicy convertRepositoryPolicy( RepositoryPolicy settingsPolicy )
    {
        org.apache.maven.model.RepositoryPolicy policy = new org.apache.maven.model.RepositoryPolicy();
        policy.setEnabled( settingsPolicy.isEnabled() );
        policy.setUpdatePolicy( settingsPolicy.getUpdatePolicy() );
        policy.setChecksumPolicy( settingsPolicy.getChecksumPolicy() );
        return policy;
    }
    public static Settings copySettings( Settings settings )
    {
        if ( settings == null )
        {
            return null;
        }
        Settings clone = new Settings();
        clone.setActiveProfiles( settings.getActiveProfiles() );
        clone.setInteractiveMode( settings.isInteractiveMode() );
        clone.setLocalRepository( settings.getLocalRepository() );
        clone.setMirrors( settings.getMirrors() );
        clone.setModelEncoding( settings.getModelEncoding() );
        clone.setOffline( settings.isOffline() );
        clone.setPluginGroups( settings.getPluginGroups() );
        clone.setProfiles( settings.getProfiles() );
        clone.setProxies( settings.getProxies() );
        clone.setServers( settings.getServers() );
        clone.setSourceLevel( settings.getSourceLevel() );
        clone.setUsePluginRegistry( settings.isUsePluginRegistry() );
        return clone;
    }
}
