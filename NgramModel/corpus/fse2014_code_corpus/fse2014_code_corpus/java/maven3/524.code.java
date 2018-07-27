package org.apache.maven.rtinfo.internal;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;
import org.sonatype.aether.version.VersionScheme;
@Component( role = RuntimeInformation.class )
public class DefaultRuntimeInformation
    implements RuntimeInformation
{
    @Requirement
    private Logger logger;
    private String mavenVersion;
    public String getMavenVersion()
    {
        if ( mavenVersion == null )
        {
            Properties props = new Properties();
            String resource = "META-INF/maven/org.apache.maven/maven-core/pom.properties";
            InputStream is = DefaultRuntimeInformation.class.getResourceAsStream( "/" + resource );
            if ( is != null )
            {
                try
                {
                    props.load( is );
                }
                catch ( IOException e )
                {
                    String msg = "Could not parse " + resource + ", Maven runtime information not available";
                    if ( logger.isDebugEnabled() )
                    {
                        logger.warn( msg, e );
                    }
                    else
                    {
                        logger.warn( msg );
                    }
                }
                finally
                {
                    IOUtil.close( is );
                }
            }
            else
            {
                logger.warn( "Could not locate " + resource + " on classpath, Maven runtime information not available" );
            }
            String version = props.getProperty( "version", "" ).trim();
            if ( !version.startsWith( "${" ) )
            {
                mavenVersion = version;
            }
            else
            {
                mavenVersion = "";
            }
        }
        return mavenVersion;
    }
    public boolean isMavenVersion( String versionRange )
    {
        VersionScheme versionScheme = new GenericVersionScheme();
        if ( versionRange == null )
        {
            throw new IllegalArgumentException( "Version range must not be null" );
        }
        if ( StringUtils.isBlank( versionRange ) )
        {
            throw new IllegalArgumentException( "Version range must not be empty" );
        }
        VersionConstraint constraint;
        try
        {
            constraint = versionScheme.parseVersionConstraint( versionRange );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            throw new IllegalArgumentException( e.getMessage(), e );
        }
        Version current;
        try
        {
            String mavenVersion = getMavenVersion();
            if ( mavenVersion.length() <= 0 )
            {
                throw new IllegalStateException( "Could not determine current Maven version" );
            }
            current = versionScheme.parseVersion( mavenVersion );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            throw new IllegalStateException( "Could not parse current Maven version: " + e.getMessage(), e );
        }
        if ( constraint.getRanges().isEmpty() )
        {
            return constraint.getVersion().compareTo( current ) <= 0;
        }
        return constraint.containsVersion( current );
    }
}
