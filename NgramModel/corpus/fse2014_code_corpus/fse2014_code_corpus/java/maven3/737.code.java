package org.apache.maven.model.io;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
@Component( role = ModelReader.class )
public class DefaultModelReader
    implements ModelReader
{
    public Model read( File input, Map<String, ?> options )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "input file missing" );
        }
        Model model = read( new FileInputStream( input ), options );
        model.setPomFile( input );
        return model;
    }
    public Model read( Reader input, Map<String, ?> options )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "input reader missing" );
        }
        try
        {
            return read( input, isStrict( options ), getSource( options ) );
        }
        finally
        {
            IOUtil.close( input );
        }
    }
    public Model read( InputStream input, Map<String, ?> options )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "input stream missing" );
        }
        try
        {
            return read( ReaderFactory.newXmlReader( input ), isStrict( options ), getSource( options ) );
        }
        finally
        {
            IOUtil.close( input );
        }
    }
    private boolean isStrict( Map<String, ?> options )
    {
        Object value = ( options != null ) ? options.get( IS_STRICT ) : null;
        return value == null || Boolean.parseBoolean( value.toString() );
    }
    private InputSource getSource( Map<String, ?> options )
    {
        Object value = ( options != null ) ? options.get( INPUT_SOURCE ) : null;
        return (InputSource) value;
    }
    private Model read( Reader reader, boolean strict, InputSource source )
        throws IOException
    {
        try
        {
            if ( source != null )
            {
                return new MavenXpp3ReaderEx().read( reader, strict, source );
            }
            else
            {
                return new MavenXpp3Reader().read( reader, strict );
            }
        }
        catch ( XmlPullParserException e )
        {
            throw new ModelParseException( e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e );
        }
    }
}
