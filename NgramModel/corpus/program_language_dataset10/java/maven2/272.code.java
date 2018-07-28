package org.apache.maven.profiles.activation;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.StringUtils;
public class JdkPrefixProfileActivator
    extends DetectedProfileActivator
{
    private static final String JDK_VERSION = System.getProperty( "java.version" );
    public boolean isActive( Profile profile )
        throws ProfileActivationException
    {
        Activation activation = profile.getActivation();
        String jdk = activation.getJdk();
        if ( jdk.startsWith( "[" ) || jdk.startsWith( "(" ) )
        {
            try
            {
                if ( matchJdkVersionRange( jdk ) )
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch ( InvalidVersionSpecificationException e )
            {
                throw new ProfileActivationException( "Invalid JDK version in profile '" + profile.getId() + "': "
                    + e.getMessage() );
            }
        }
        boolean reverse = false;
        if ( jdk.startsWith( "!" ) )
        {
            reverse = true;
            jdk = jdk.substring( 1 );
        }
        if ( getJdkVersion().startsWith( jdk ) )
        {
            return !reverse;
        }
        else
        {
            return reverse;
        }
    }
    private boolean matchJdkVersionRange( String jdk )
        throws InvalidVersionSpecificationException
    {
        VersionRange jdkVersionRange = VersionRange.createFromVersionSpec( convertJdkToMavenVersion( jdk ) );
        DefaultArtifactVersion jdkVersion = new DefaultArtifactVersion( convertJdkToMavenVersion( getJdkVersion() ) );
        return jdkVersionRange.containsVersion( jdkVersion );
    }
    private String convertJdkToMavenVersion( String jdk )
    {
        return jdk.replaceAll( "_", "-" );
    }
    protected String getJdkVersion()
    {
        return JDK_VERSION;
    }
    protected boolean canDetectActivation( Profile profile )
    {
        return profile.getActivation() != null && StringUtils.isNotEmpty( profile.getActivation().getJdk() );
    }
}
