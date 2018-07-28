package org.apache.maven.project;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import junit.framework.TestCase;
public class ExtensionDescriptorBuilderTest
    extends TestCase
{
    private ExtensionDescriptorBuilder builder;
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        builder = new ExtensionDescriptorBuilder();
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        builder = null;
        super.tearDown();
    }
    private InputStream toStream( String xml )
    {
        try
        {
            return new ByteArrayInputStream( xml.getBytes( "UTF-8" ) );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new IllegalStateException( e );
        }
    }
    public void testEmptyDescriptor()
        throws Exception
    {
        String xml = "<extension></extension>";
        ExtensionDescriptor ed = builder.build( toStream( xml ) );
        assertNotNull( ed );
        assertNotNull( ed.getExportedPackages() );
        assertTrue( ed.getExportedPackages().isEmpty() );
        assertNotNull( ed.getExportedArtifacts() );
        assertTrue( ed.getExportedArtifacts().isEmpty() );
    }
    public void testCompleteDescriptor()
        throws Exception
    {
        String xml =
            "<?xml version='1.0' encoding='UTF-8'?>" + "<extension>" + "<exportedPackages>"
                + "<exportedPackage>a</exportedPackage>" + "<exportedPackage>b</exportedPackage>"
                + "<exportedPackage>c</exportedPackage>" + "</exportedPackages>" + "<exportedArtifacts>"
                + "<exportedArtifact>x</exportedArtifact>" + "<exportedArtifact>y</exportedArtifact>"
                + "<exportedArtifact> z </exportedArtifact>" + "</exportedArtifacts>" + "</extension>";
        ExtensionDescriptor ed = builder.build( toStream( xml ) );
        assertNotNull( ed );
        assertEquals( Arrays.asList( "a", "b", "c" ), ed.getExportedPackages() );
        assertEquals( Arrays.asList( "x", "y", "z" ), ed.getExportedArtifacts() );
    }
}
