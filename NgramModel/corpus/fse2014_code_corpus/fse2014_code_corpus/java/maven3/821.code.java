package org.apache.maven.settings.building;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
public class FileSettingsSource
    implements SettingsSource
{
    private File settingsFile;
    public FileSettingsSource( File settingsFile )
    {
        if ( settingsFile == null )
        {
            throw new IllegalArgumentException( "no settings file specified" );
        }
        this.settingsFile = settingsFile.getAbsoluteFile();
    }
    public InputStream getInputStream()
        throws IOException
    {
        return new FileInputStream( settingsFile );
    }
    public String getLocation()
    {
        return settingsFile.getPath();
    }
    public File getSettingsFile()
    {
        return settingsFile;
    }
    @Override
    public String toString()
    {
        return getLocation();
    }
}
