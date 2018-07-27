package org.apache.maven.model.building;
import java.io.File;
import junit.framework.TestCase;
public class DefaultModelBuilderFactoryTest
    extends TestCase
{
    private File getPom( String name )
    {
        return new File( "src/test/resources/poms/factory/" + name + ".xml" ).getAbsoluteFile();
    }
    public void testCompleteWiring()
        throws Exception
    {
        ModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
        assertNotNull( builder );
        DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
        request.setProcessPlugins( true );
        request.setPomFile( getPom( "simple" ) );
        ModelBuildingResult result = builder.build( request );
        assertNotNull( result );
        assertNotNull( result.getEffectiveModel() );
        assertEquals( "activated", result.getEffectiveModel().getProperties().get( "profile.file" ) );
    }
}
