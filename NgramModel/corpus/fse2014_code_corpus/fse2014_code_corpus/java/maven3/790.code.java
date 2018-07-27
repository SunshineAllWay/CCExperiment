package org.apache.maven.model.profile.activation;
import java.util.Properties;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
public class JdkVersionProfileActivatorTest
    extends AbstractProfileActivatorTest<JdkVersionProfileActivator>
{
    public JdkVersionProfileActivatorTest()
    {
        super( JdkVersionProfileActivator.class );
    }
    private Profile newProfile( String jdkVersion )
    {
        Activation a = new Activation();
        a.setJdk( jdkVersion );
        Profile p = new Profile();
        p.setActivation( a );
        return p;
    }
    private Properties newProperties( String javaVersion )
    {
        Properties props = new Properties();
        props.setProperty( "java.version", javaVersion );
        return props;
    }
    public void testNullSafe()
        throws Exception
    {
        Profile p = new Profile();
        assertActivation( false, p, newContext( null, null ) );
        p.setActivation( new Activation() );
        assertActivation( false, p, newContext( null, null ) );
    }
    public void testPrefix()
        throws Exception
    {
        Profile profile = newProfile( "1.4" );
        assertActivation( true, profile, newContext( null, newProperties( "1.4" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.4.2" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.4.2_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.4.2_09-b03" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.3" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.5" ) ) );
    }
    public void testPrefixNegated()
        throws Exception
    {
        Profile profile = newProfile( "!1.4" );
        assertActivation( false, profile, newContext( null, newProperties( "1.4" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2_09" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.3" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5" ) ) );
    }
    public void testVersionRangeInclusiveBounds()
        throws Exception
    {
        Profile profile = newProfile( "[1.5,1.6]" );
        assertActivation( false, profile, newContext( null, newProperties( "1.4" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2_09" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.1" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6.0" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6.0_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6.0_09-b03" ) ) );
    }
    public void testVersionRangeExclusiveBounds()
        throws Exception
    {
        Profile profile = newProfile( "(1.3,1.6)" );
        assertActivation( false, profile, newContext( null, newProperties( "1.3" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.3.0" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.3.0_09" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.3.0_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.3.1" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.3.1_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.3.1_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.1" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.6" ) ) );
    }
    public void testVersionRangeInclusiveLowerBound()
        throws Exception
    {
        Profile profile = newProfile( "[1.5,)" );
        assertActivation( false, profile, newContext( null, newProperties( "1.4" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2_09" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.4.2_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.1" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6.0" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6.0_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.6.0_09-b03" ) ) );
    }
    public void testVersionRangeExclusiveUpperBound()
        throws Exception
    {
        Profile profile = newProfile( "(,1.6)" );
        assertActivation( true, profile, newContext( null, newProperties( "1.5" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.0_09-b03" ) ) );
        assertActivation( true, profile, newContext( null, newProperties( "1.5.1" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.6" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.6.0" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.6.0_09" ) ) );
        assertActivation( false, profile, newContext( null, newProperties( "1.6.0_09-b03" ) ) );
    }
}
