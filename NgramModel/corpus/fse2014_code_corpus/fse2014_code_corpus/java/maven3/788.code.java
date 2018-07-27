package org.apache.maven.model.path;
import junit.framework.TestCase;
public class DefaultUrlNormalizerTest
    extends TestCase
{
    private UrlNormalizer normalizer;
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        normalizer = new DefaultUrlNormalizer();
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        normalizer = null;
        super.tearDown();
    }
    private String normalize( String url )
    {
        return normalizer.normalize( url );
    }
    public void testNullSafe()
    {
        assertNull( normalize( null ) );
    }
    public void testTrailingSlash()
    {
        assertEquals( "", normalize( "" ) );
        assertEquals( "http://server.org/dir", normalize( "http://server.org/dir" ) );
        assertEquals( "http://server.org/dir/", normalize( "http://server.org/dir/" ) );
    }
    public void testRemovalOfParentRefs()
    {
        assertEquals( "http://server.org/child", normalize( "http://server.org/parent/../child" ) );
        assertEquals( "http://server.org/child", normalize( "http://server.org/grand/parent/../../child" ) );
        assertEquals( "http://server.org//child", normalize( "http://server.org/parent/..//child" ) );
        assertEquals( "http://server.org/child", normalize( "http://server.org/parent//../child" ) );
    }
    public void testPreservationOfDoubleSlashes()
    {
        assertEquals( "scm:hg:ssh://localhost//home/user", normalize( "scm:hg:ssh://localhost//home/user" ) );
        assertEquals( "file:////UNC/server", normalize( "file:////UNC/server" ) );
        assertEquals( "[fetch=]http://server.org/[push=]ssh://server.org/",
                      normalize( "[fetch=]http://server.org/[push=]ssh://server.org/" ) );
    }
}
