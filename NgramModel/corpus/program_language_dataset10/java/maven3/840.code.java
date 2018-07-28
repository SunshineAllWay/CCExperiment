package org.apache.maven.settings.io;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import org.apache.maven.settings.Settings;
public interface SettingsReader
{
    String IS_STRICT = "org.apache.maven.settings.io.isStrict";
    Settings read( File input, Map<String, ?> options )
        throws IOException, SettingsParseException;
    Settings read( Reader input, Map<String, ?> options )
        throws IOException, SettingsParseException;
    Settings read( InputStream input, Map<String, ?> options )
        throws IOException, SettingsParseException;
}
