package org.apache.maven.settings.building;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
public class UrlSettingsSource
    implements SettingsSource
{
    private URL settingsUrl;
    public UrlSettingsSource( URL settingsUrl )
    {
        if ( settingsUrl == null )
        {
            throw new IllegalArgumentException( "no settings URL specified" );
        }
        this.settingsUrl = settingsUrl;
    }
    public InputStream getInputStream()
        throws IOException
    {
        return settingsUrl.openStream();
    }
    public String getLocation()
    {
        return settingsUrl.toString();
    }
    public URL getSettingsUrl()
    {
        return settingsUrl;
    }
    @Override
    public String toString()
    {
        return getLocation();
    }
}
