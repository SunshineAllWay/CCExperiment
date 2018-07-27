package org.apache.maven.artifact.installer;
import java.io.File;
import org.apache.maven.artifact.AbstractArtifactComponentTestCase;
import org.apache.maven.artifact.Artifact;
public class ArtifactInstallerTest
    extends AbstractArtifactComponentTestCase
{
    private ArtifactInstaller artifactInstaller;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        artifactInstaller = (ArtifactInstaller) lookup( ArtifactInstaller.ROLE );
    }
    protected String component()
    {
        return "installer";
    }
    public void testArtifactInstallation()
        throws Exception
    {
        String artifactBasedir = new File( getBasedir(), "src/test/resources/artifact-install" ).getAbsolutePath();
        Artifact artifact = createArtifact( "artifact", "1.0" );
        File source = new File( artifactBasedir, "artifact-1.0.jar" );
        artifactInstaller.install( source, artifact, localRepository() );
        assertLocalArtifactPresent( artifact );
    }
}