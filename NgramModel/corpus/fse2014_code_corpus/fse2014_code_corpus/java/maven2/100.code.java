package org.apache.maven.artifact.deployer;
import org.apache.maven.artifact.AbstractArtifactComponentTestCase;
import org.apache.maven.artifact.Artifact;
import java.io.File;
public class ArtifactDeployerTest
    extends AbstractArtifactComponentTestCase
{
    private ArtifactDeployer artifactDeployer;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        artifactDeployer = (ArtifactDeployer) lookup( ArtifactDeployer.ROLE );
    }
    protected String component()
    {
        return "deployer";
    }
    public void testArtifactInstallation()
        throws Exception
    {
        String artifactBasedir = new File( getBasedir(), "src/test/resources/artifact-install" ).getAbsolutePath();
        Artifact artifact = createArtifact( "artifact", "1.0" );
        File file = new File( artifactBasedir, "artifact-1.0.jar" );
        artifactDeployer.deploy( file, artifact, remoteRepository(), localRepository() );
        assertRemoteArtifactPresent( artifact );
    }
}