package org.apache.maven.model.building;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
public class StringModelSource
    implements ModelSource
{
    private String pom;
    private String location;
    public StringModelSource( CharSequence pom )
    {
        this( pom, null );
    }
    public StringModelSource( CharSequence pom, String location )
    {
        this.pom = ( pom != null ) ? pom.toString() : "";
        this.location = ( location != null ) ? location : "(memory)";
    }
    public InputStream getInputStream()
        throws IOException
    {
        return new ByteArrayInputStream( pom.getBytes( "UTF-8" ) );
    }
    public String getLocation()
    {
        return location;
    }
    public String getModel()
    {
        return pom;
    }
    @Override
    public String toString()
    {
        return getLocation();
    }
}
