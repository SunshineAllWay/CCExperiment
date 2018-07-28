package org.apache.maven.repository.internal;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import junit.framework.TestCase;
public class DefaultServiceLocatorTest
    extends TestCase
{
    public void testGetRepositorySystem()
    {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        RepositorySystem repoSys = locator.getService( RepositorySystem.class );
        assertNotNull( repoSys );
    }
    public void testGetMetadataGeneratorFactories()
    {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        assertEquals( 2, locator.getServices( MetadataGeneratorFactory.class ).size() );
    }
}
