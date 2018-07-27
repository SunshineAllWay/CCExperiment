package org.apache.maven.artifact.repository.metadata.io;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import org.apache.maven.artifact.repository.metadata.Metadata;
public interface MetadataReader
{
    final String IS_STRICT = "org.apache.maven.artifact.repository.metadata.io.isStrict";
    Metadata read( File input, Map<String, ?> options )
        throws IOException, MetadataParseException;
    Metadata read( Reader input, Map<String, ?> options )
        throws IOException, MetadataParseException;
    Metadata read( InputStream input, Map<String, ?> options )
        throws IOException, MetadataParseException;
}
