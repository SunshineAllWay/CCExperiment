package org.apache.maven.plugin;
import java.lang.reflect.Array;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.logging.Logger;
@Deprecated
public class DebugConfigurationListener
    implements ConfigurationListener
{
    private Logger logger;
    public DebugConfigurationListener( Logger logger )
    {
        this.logger = logger;
    }
    public void notifyFieldChangeUsingSetter( String fieldName, Object value, Object target )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "  (s) " + fieldName + " = " + toString( value ) );
        }
    }
    public void notifyFieldChangeUsingReflection( String fieldName, Object value, Object target )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "  (f) " + fieldName + " = " + toString( value ) );
        }
    }
    private String toString( Object obj )
    {
        String str;
        if ( obj != null && obj.getClass().isArray() )
        {
            int n = Array.getLength( obj );
            StringBuilder buf = new StringBuilder( 256 );
            buf.append( '[' );
            for ( int i = 0; i < n; i++ )
            {
                if ( i > 0 )
                {
                    buf.append( ", " );
                }
                buf.append( String.valueOf( Array.get( obj, i ) ) );
            }
            buf.append( ']' );
            str = buf.toString();
        }
        else
        {
            str = String.valueOf( obj );
        }
        return str;
    }
}
