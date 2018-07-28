package org.apache.maven.plugin;
import java.util.List;
public class InvalidPluginDescriptorException
    extends Exception
{
    public InvalidPluginDescriptorException( String message, List<String> errors )
    {
        super( toMessage( message, errors ) );
    }
    private static String toMessage( String message, List<String> errors )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( message );
        for ( String error : errors )
        {
            buffer.append( ", " ).append( error );
        }
        return buffer.toString();
    }
}
