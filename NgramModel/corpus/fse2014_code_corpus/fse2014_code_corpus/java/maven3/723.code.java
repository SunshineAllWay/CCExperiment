package org.apache.maven.model.building;
import java.io.IOException;
import java.io.InputStream;
public interface ModelSource
{
    InputStream getInputStream()
        throws IOException;
    String getLocation();
}
