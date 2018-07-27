package org.apache.maven.settings.crypto;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.building.DefaultSettingsProblem;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.building.SettingsProblem.Severity;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
@Component( role = SettingsDecrypter.class )
public class DefaultSettingsDecrypter
    implements SettingsDecrypter
{
    @Requirement( hint = "maven" )
    private SecDispatcher securityDispatcher;
    public SettingsDecryptionResult decrypt( SettingsDecryptionRequest request )
    {
        List<SettingsProblem> problems = new ArrayList<SettingsProblem>();
        List<Server> servers = new ArrayList<Server>();
        for ( Server server : request.getServers() )
        {
            server = server.clone();
            servers.add( server );
            try
            {
                server.setPassword( decrypt( server.getPassword() ) );
            }
            catch ( SecDispatcherException e )
            {
                problems.add( new DefaultSettingsProblem( "Failed to decrypt password for server " + server.getId()
                    + ": " + e.getMessage(), Severity.ERROR, "server: " + server.getId(), -1, -1, e ) );
            }
            try
            {
                server.setPassphrase( decrypt( server.getPassphrase() ) );
            }
            catch ( SecDispatcherException e )
            {
                problems.add( new DefaultSettingsProblem( "Failed to decrypt passphrase for server " + server.getId()
                    + ": " + e.getMessage(), Severity.ERROR, "server: " + server.getId(), -1, -1, e ) );
            }
        }
        List<Proxy> proxies = new ArrayList<Proxy>();
        for ( Proxy proxy : request.getProxies() )
        {
            proxy = proxy.clone();
            proxies.add( proxy );
            try
            {
                proxy.setPassword( decrypt( proxy.getPassword() ) );
            }
            catch ( SecDispatcherException e )
            {
                problems.add( new DefaultSettingsProblem( "Failed to decrypt password for proxy " + proxy.getId()
                    + ": " + e.getMessage(), Severity.ERROR, "proxy: " + proxy.getId(), -1, -1, e ) );
            }
        }
        return new DefaultSettingsDecryptionResult( servers, proxies, problems );
    }
    private String decrypt( String str )
        throws SecDispatcherException
    {
        return ( str == null ) ? null : securityDispatcher.decrypt( str );
    }
}
