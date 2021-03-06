package org.apache.maven.model.interpolation;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.codehaus.plexus.interpolation.AbstractValueSource;
class BuildTimestampValueSource
    extends AbstractValueSource
{
    private final Date startTime;
    private final String format;
    private String formattedDate;
    public BuildTimestampValueSource( Date startTime, String format )
    {
        super( false );
        this.startTime = startTime;
        this.format = format;
    }
    public Object getValue( String expression )
    {
        if ( "build.timestamp".equals( expression ) || "maven.build.timestamp".equals( expression ) )
        {
            if ( formattedDate == null && startTime != null )
            {
                formattedDate = new SimpleDateFormat( format ).format( startTime );
            }
            return formattedDate;
        }
        return null;
    }
}
