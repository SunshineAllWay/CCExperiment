package org.apache.maven.settings;
import java.io.File;
import junit.framework.TestCase;
public class SettingsTest
    extends TestCase
{
    private Settings settingsNoProxies;
    private Settings settingsOneInactiveProxy;
    private Settings settingsOneActiveProxy;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        DefaultMavenSettingsBuilder settingsBuilder = new DefaultMavenSettingsBuilder();
        File dir = new File( "src/test/resources/org/apache/maven/settings" );
        settingsNoProxies = settingsBuilder.buildSettings( new File( dir, "settings-no-proxies.xml" ), false );
        settingsOneInactiveProxy =
            settingsBuilder.buildSettings( new File( dir, "settings-one-inactive-proxy.xml" ), false );
        settingsOneActiveProxy =
            settingsBuilder.buildSettings( new File( dir, "settings-one-active-proxy.xml" ), false );
    }
    public void testProxySettings()
    {
        assertNull( settingsNoProxies.getActiveProxy() );
        assertNull( settingsOneInactiveProxy.getActiveProxy() );
        assertNotNull( settingsOneActiveProxy.getActiveProxy() );
    }
}
