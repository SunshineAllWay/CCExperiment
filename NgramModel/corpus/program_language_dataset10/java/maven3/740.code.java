package org.apache.maven.model.io;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import org.apache.maven.model.Model;
public interface ModelReader
{
    String IS_STRICT = "org.apache.maven.model.io.isStrict";
    String INPUT_SOURCE = "org.apache.maven.model.io.inputSource";
    Model read( File input, Map<String, ?> options )
        throws IOException, ModelParseException;
    Model read( Reader input, Map<String, ?> options )
        throws IOException, ModelParseException;
    Model read( InputStream input, Map<String, ?> options )
        throws IOException, ModelParseException;
}
