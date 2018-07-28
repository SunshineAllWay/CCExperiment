package org.apache.maven.properties.internal;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.codehaus.plexus.util.Os;
public class EnvironmentUtils
{
    private static Properties envVars;
    public static void addEnvVars( Properties props )
    {
        if ( props != null )
        {
            if ( envVars == null )
            {
                Properties tmp = new Properties();
                boolean caseSensitive = !Os.isFamily( Os.FAMILY_WINDOWS );
                for ( Map.Entry<String, String> entry : System.getenv().entrySet() )
                {
                    String key =
                        "env." + ( caseSensitive ? entry.getKey() : entry.getKey().toUpperCase( Locale.ENGLISH ) );
                    tmp.setProperty( key, entry.getValue() );
                }
                envVars = tmp;
            }
            props.putAll( envVars );
        }
    }
}
