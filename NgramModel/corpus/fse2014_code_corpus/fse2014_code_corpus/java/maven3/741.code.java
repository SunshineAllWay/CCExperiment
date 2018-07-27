package org.apache.maven.model.io;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import org.apache.maven.model.Model;
public interface ModelWriter
{
    void write( File output, Map<String, Object> options, Model model )
        throws IOException;
    void write( Writer output, Map<String, Object> options, Model model )
        throws IOException;
    void write( OutputStream output, Map<String, Object> options, Model model )
        throws IOException;
}
