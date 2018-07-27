package org.apache.maven.settings.io;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import org.apache.maven.settings.Settings;
public interface SettingsWriter
{
    void write( File output, Map<String, Object> options, Settings settings )
        throws IOException;
    void write( Writer output, Map<String, Object> options, Settings settings )
        throws IOException;
    void write( OutputStream output, Map<String, Object> options, Settings settings )
        throws IOException;
}
