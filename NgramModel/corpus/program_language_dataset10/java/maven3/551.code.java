package org.apache.maven.lifecycle;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.annotations.Requirement;
import java.util.List;
public class DefaultLifecyclesTest
    extends PlexusTestCase
{
    @Requirement
    private DefaultLifecycles defaultLifeCycles;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        defaultLifeCycles = lookup( DefaultLifecycles.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        defaultLifeCycles = null;
        super.tearDown();
    }
    public void testLifecycle()
        throws Exception
    {
        final List<Lifecycle> cycles = defaultLifeCycles.getLifeCycles();
        assertNotNull( cycles );
        final Lifecycle lifecycle = cycles.get( 0 );
        assertEquals( "default", lifecycle.getId() );
        assertEquals( 23, lifecycle.getPhases().size() );
    }
}