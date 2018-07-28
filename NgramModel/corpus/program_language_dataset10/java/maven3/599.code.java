package org.apache.maven.project.artifact;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusTestCase;
public class MavenMetadataSourceTest
    extends PlexusTestCase
{
    private RepositorySystem repositorySystem;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        repositorySystem = lookup( RepositorySystem.class );
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        repositorySystem = null;
        super.tearDown();
    }
    public void testShouldNotCarryExclusionsOverFromDependencyToDependency()
        throws Exception
    {
    }
}
