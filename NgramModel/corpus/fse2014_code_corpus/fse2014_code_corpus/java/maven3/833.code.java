package org.apache.maven.settings.crypto;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.building.SettingsProblem;
class DefaultSettingsDecryptionResult
    implements SettingsDecryptionResult
{
    private List<Server> servers;
    private List<Proxy> proxies;
    private List<SettingsProblem> problems;
    public DefaultSettingsDecryptionResult( List<Server> servers, List<Proxy> proxies, List<SettingsProblem> problems )
    {
        this.servers = ( servers != null ) ? servers : new ArrayList<Server>();
        this.proxies = ( proxies != null ) ? proxies : new ArrayList<Proxy>();
        this.problems = ( problems != null ) ? problems : new ArrayList<SettingsProblem>();
    }
    public Server getServer()
    {
        return servers.isEmpty() ? null : servers.get( 0 );
    }
    public List<Server> getServers()
    {
        return servers;
    }
    public Proxy getProxy()
    {
        return proxies.isEmpty() ? null : proxies.get( 0 );
    }
    public List<Proxy> getProxies()
    {
        return proxies;
    }
    public List<SettingsProblem> getProblems()
    {
        return problems;
    }
}
