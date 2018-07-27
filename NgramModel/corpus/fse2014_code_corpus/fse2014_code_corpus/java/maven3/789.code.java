package org.apache.maven.model.profile.activation;
import java.util.Properties;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.SimpleProblemCollector;
import org.apache.maven.model.profile.DefaultProfileActivationContext;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.annotations.Component;
public abstract class AbstractProfileActivatorTest<T extends ProfileActivator>
    extends PlexusTestCase
{
    private Class<T> activatorClass;
    private String roleHint;
    protected T activator;
    public AbstractProfileActivatorTest( Class<T> activatorClass )
    {
        if ( activatorClass == null )
        {
            throw new IllegalArgumentException( "class of profile activator to test is not specified" );
        }
        this.activatorClass = activatorClass;
        roleHint = activatorClass.getAnnotation( Component.class ).hint();
    }
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        activator = activatorClass.cast( lookup( ProfileActivator.class, roleHint ) );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        activator = null;
        super.tearDown();
    }
    protected ProfileActivationContext newContext( final Properties userProperties, final Properties systemProperties )
    {
        DefaultProfileActivationContext context = new DefaultProfileActivationContext();
        return context.setUserProperties( userProperties ).setSystemProperties( systemProperties );
    }
    protected void assertActivation( boolean active, Profile profile, ProfileActivationContext context )
    {
        SimpleProblemCollector problems = new SimpleProblemCollector();
        assertEquals( active, activator.isActive( profile, context, problems ) );
        assertEquals( problems.getErrors().toString(), 0, problems.getErrors().size() );
        assertEquals( problems.getWarnings().toString(), 0, problems.getWarnings().size() );
    }
}
