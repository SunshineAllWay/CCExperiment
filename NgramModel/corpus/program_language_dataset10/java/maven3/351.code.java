package org.apache.maven.execution;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.RuntimeInfo;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
class SettingsAdapter
    extends Settings
{
    private MavenExecutionRequest request;
    private RuntimeInfo runtimeInfo;
    public SettingsAdapter( MavenExecutionRequest request )
    {
        this.request = request;
        File userSettings = request.getUserSettingsFile();
        this.runtimeInfo = new RuntimeInfo( ( userSettings != null && userSettings.isFile() ) ? userSettings : null );
    }
    @Override
    public String getLocalRepository()
    {
        if ( request.getLocalRepositoryPath() != null )
        {
            return request.getLocalRepositoryPath().getAbsolutePath();
        }
        return null;
    }
    @Override
    public boolean isInteractiveMode()
    {
        return request.isInteractiveMode();
    }
    @Override
    public boolean isOffline()
    {
        return request.isOffline();
    }
    @Override
    public List<Proxy> getProxies()
    {
        return request.getProxies();
    }
    @Override
    public List<Server> getServers()
    {
        return request.getServers();
    }
    @Override
    public List<Mirror> getMirrors()
    {
        return request.getMirrors();
    }
    @Override
    public List<Profile> getProfiles()
    {
        return new ArrayList<Profile>();
    }
    @Override
    public List<String> getActiveProfiles()
    {
        return request.getActiveProfiles();
    }
    @Override
    public List<String> getPluginGroups()
    {
        return request.getPluginGroups();
    }
    @Override
    public RuntimeInfo getRuntimeInfo()
    {
        return runtimeInfo;
    }
}
