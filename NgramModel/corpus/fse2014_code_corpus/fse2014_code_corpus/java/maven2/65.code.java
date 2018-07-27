package org.apache.maven.artifact;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.versioning.VersionRange;
import junit.framework.TestCase;
public class ArtifactUtilsTest
    extends TestCase
{
    public void testArtifactMapByArtifactIdOrdering()
        throws Exception
    {
        List list = new ArrayList();
        list.add( newArtifact( "b" ) );
        list.add( newArtifact( "a" ) );
        list.add( newArtifact( "c" ) );
        list.add( newArtifact( "e" ) );
        list.add( newArtifact( "d" ) );
        Map map = ArtifactUtils.artifactMapByArtifactId( list );
        assertNotNull( map );
        assertEquals( list, new ArrayList( map.values() ) );
    }
    public void testArtifactMapByVersionlessIdOrdering()
        throws Exception
    {
        List list = new ArrayList();
        list.add( newArtifact( "b" ) );
        list.add( newArtifact( "a" ) );
        list.add( newArtifact( "c" ) );
        list.add( newArtifact( "e" ) );
        list.add( newArtifact( "d" ) );
        Map map = ArtifactUtils.artifactMapByVersionlessId( list );
        assertNotNull( map );
        assertEquals( list, new ArrayList( map.values() ) );
    }
    private Artifact newArtifact( String aid )
    {
        return new DefaultArtifact( "org.apache.maven.ut", aid, VersionRange.createFromVersion( "1.0" ), "test", "jar",
                                    "tests", null );
    }
}
