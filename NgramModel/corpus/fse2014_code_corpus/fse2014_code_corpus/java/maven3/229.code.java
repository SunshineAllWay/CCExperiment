package org.apache.maven.project.path;
import java.io.File;
import junit.framework.TestCase;
@SuppressWarnings( "deprecation" )
public class DefaultPathTranslatorTest
    extends TestCase
{
    public void testAlignToBasedirWhereBasedirExpressionIsTheCompleteValue()
    {
        File basedir = new File( System.getProperty( "java.io.tmpdir" ), "test" ).getAbsoluteFile();
        String aligned = new DefaultPathTranslator().alignToBaseDirectory( "${basedir}", basedir );
        assertEquals( basedir.getAbsolutePath(), aligned );
    }
    public void testAlignToBasedirWhereBasedirExpressionIsTheValuePrefix()
    {
        File basedir = new File( System.getProperty( "java.io.tmpdir" ), "test" ).getAbsoluteFile();
        String aligned = new DefaultPathTranslator().alignToBaseDirectory( "${basedir}/dir", basedir );
        assertEquals( new File( basedir, "dir" ).getAbsolutePath(), aligned );
    }
    public void testUnalignToBasedirWherePathEqualsBasedir()
    {
        File basedir = new File( System.getProperty( "java.io.tmpdir" ), "test" ).getAbsoluteFile();
        String unaligned = new DefaultPathTranslator().unalignFromBaseDirectory( basedir.getAbsolutePath(), basedir );
        assertEquals( ".", unaligned );
    }
}
