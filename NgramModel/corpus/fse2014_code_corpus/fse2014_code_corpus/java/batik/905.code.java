package org.apache.batik.ext.awt.image.spi;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.util.ParsedURL;
public interface StreamRegistryEntry extends RegistryEntry {
    int getReadlimit();
    boolean isCompatibleStream(InputStream is)
        throws StreamCorruptedException;
    Filter handleStream(InputStream is,
                               ParsedURL   origURL,
                               boolean     needRawData);
}
