package org.apache.maven.usability;
import java.io.IOException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.usability.diagnostics.ErrorDiagnoser;
import org.codehaus.plexus.PlexusTestCase;
public class ArtifactResolverDiagnoserTest
    extends PlexusTestCase
{
    public void testNullMessage()
        throws Exception
    {
        ErrorDiagnoser diagnoser =
            (ArtifactResolverDiagnoser) lookup( ErrorDiagnoser.ROLE, "ArtifactResolverDiagnoser" );
        Throwable error = new ArtifactResolutionException( null, null, null, null, null, null, new IOException() );
        assertTrue( diagnoser.canDiagnose( error ) );
        diagnoser.diagnose( error );
    }
}
