package org.apache.maven.profiles;
import org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
@Deprecated
@Component( role = MavenProfilesBuilder.class )
public class DefaultMavenProfilesBuilder
    extends AbstractLogEnabled
    implements MavenProfilesBuilder
{
    private static final String PROFILES_XML_FILE = "profiles.xml";
    public ProfilesRoot buildProfiles( File basedir )
        throws IOException, XmlPullParserException
    {
        File profilesXml = new File( basedir, PROFILES_XML_FILE );
        ProfilesRoot profilesRoot = null;
        if ( profilesXml.exists() )
        {
            ProfilesXpp3Reader reader = new ProfilesXpp3Reader();
            Reader profileReader = null;
            try
            {
                profileReader = ReaderFactory.newXmlReader( profilesXml );
                StringWriter sWriter = new StringWriter();
                IOUtil.copy( profileReader, sWriter );
                String rawInput = sWriter.toString();
                try
                {
                    RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
                    interpolator.addValueSource( new EnvarBasedValueSource() );
                    rawInput = interpolator.interpolate( rawInput, "settings" );
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Failed to initialize environment variable resolver. Skipping environment "
                                          + "substitution in " + PROFILES_XML_FILE + "." );
                    getLogger().debug( "Failed to initialize envar resolver. Skipping resolution.", e );
                }
                StringReader sReader = new StringReader( rawInput );
                profilesRoot = reader.read( sReader );
            }
            finally
            {
                IOUtil.close( profileReader );
            }
        }
        return profilesRoot;
    }
}
