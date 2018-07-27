package org.apache.maven.artifact.repository;
import junit.framework.TestCase;
public class MavenArtifactRepositoryTest
    extends TestCase
{
    private static class MavenArtifactRepositorySubclass extends MavenArtifactRepository
    {
        String id;
        public MavenArtifactRepositorySubclass(String id)
        {
            this.id = id;
        }
        @Override
        public String getId()
        {
            return id;
        }
    }
    public void testHashCodeEquals()
    {
        MavenArtifactRepositorySubclass r1 = new MavenArtifactRepositorySubclass( "foo" );
        MavenArtifactRepositorySubclass r2 = new MavenArtifactRepositorySubclass( "foo" );
        MavenArtifactRepositorySubclass r3 = new MavenArtifactRepositorySubclass( "bar" );
        assertTrue( r1.hashCode() == r2.hashCode() );
        assertFalse( r1.hashCode() == r3.hashCode() );
        assertTrue( r1.equals( r2 ) );
        assertTrue( r2.equals( r1 ) );
        assertFalse( r1.equals( r3 ) );
        assertFalse( r3.equals( r1 ) );
    }
}
