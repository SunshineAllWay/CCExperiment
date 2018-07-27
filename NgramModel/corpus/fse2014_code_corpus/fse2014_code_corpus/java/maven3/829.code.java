package org.apache.maven.settings.building;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
public class StringSettingsSource
    implements SettingsSource
{
    private String settings;
    private String location;
    public StringSettingsSource( CharSequence settings )
    {
        this( settings, null );
    }
    public StringSettingsSource( CharSequence settings, String location )
    {
        this.settings = ( settings != null ) ? settings.toString() : "";
        this.location = ( location != null ) ? location : "(memory)";
    }
    public InputStream getInputStream()
        throws IOException
    {
        return new ByteArrayInputStream( settings.getBytes( "UTF-8" ) );
    }
    public String getLocation()
    {
        return location;
    }
    public String getSettings()
    {
        return settings;
    }
    @Override
    public String toString()
    {
        return getLocation();
    }
}
