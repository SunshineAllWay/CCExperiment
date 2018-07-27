package org.apache.maven.model.building;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
public class UrlModelSource
    implements ModelSource
{
    private URL pomUrl;
    public UrlModelSource( URL pomUrl )
    {
        if ( pomUrl == null )
        {
            throw new IllegalArgumentException( "no POM URL specified" );
        }
        this.pomUrl = pomUrl;
    }
    public InputStream getInputStream()
        throws IOException
    {
        return pomUrl.openStream();
    }
    public String getLocation()
    {
        return pomUrl.toString();
    }
    public URL getPomUrl()
    {
        return pomUrl;
    }
    @Override
    public String toString()
    {
        return getLocation();
    }
}
