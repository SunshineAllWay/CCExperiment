package org.apache.maven.settings.crypto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
public class DefaultSettingsDecryptionRequest
    implements SettingsDecryptionRequest
{
    private List<Server> servers;
    private List<Proxy> proxies;
    public DefaultSettingsDecryptionRequest()
    {
    }
    public DefaultSettingsDecryptionRequest( Settings settings )
    {
        setServers( settings.getServers() );
        setProxies( settings.getProxies() );
    }
    public DefaultSettingsDecryptionRequest( Server server )
    {
        this.servers = new ArrayList<Server>( Arrays.asList( server ) );
    }
    public DefaultSettingsDecryptionRequest( Proxy proxy )
    {
        this.proxies = new ArrayList<Proxy>( Arrays.asList( proxy ) );
    }
    public List<Server> getServers()
    {
        if ( servers == null )
        {
            servers = new ArrayList<Server>();
        }
        return servers;
    }
    public DefaultSettingsDecryptionRequest setServers( List<Server> servers )
    {
        this.servers = servers;
        return this;
    }
    public List<Proxy> getProxies()
    {
        if ( proxies == null )
        {
            proxies = new ArrayList<Proxy>();
        }
        return proxies;
    }
    public DefaultSettingsDecryptionRequest setProxies( List<Proxy> proxies )
    {
        this.proxies = proxies;
        return this;
    }
}
