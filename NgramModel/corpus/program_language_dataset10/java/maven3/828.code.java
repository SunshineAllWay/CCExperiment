package org.apache.maven.settings.building;
import java.io.IOException;
import java.io.InputStream;
public interface SettingsSource
{
    InputStream getInputStream()
        throws IOException;
    String getLocation();
}
