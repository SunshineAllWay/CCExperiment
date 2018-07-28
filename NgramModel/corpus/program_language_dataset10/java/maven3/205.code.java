package org.apache.maven.project;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
@Component( role = ArtifactResolver.class, hint = "classpath" )
public class ClasspathArtifactResolver
    implements ArtifactResolver
{
    public List<ArtifactResult> resolveArtifacts( RepositorySystemSession session,
                                                  Collection<? extends ArtifactRequest> requests )
        throws ArtifactResolutionException
    {
        List<ArtifactResult> results = new ArrayList<ArtifactResult>();
        for ( ArtifactRequest request : requests )
        {
            ArtifactResult result = new ArtifactResult( request );
            results.add( result );
            Artifact artifact = request.getArtifact();
            if ( "maven-test".equals( artifact.getGroupId() ) )
            {
                String scope = artifact.getArtifactId().substring( "scope-".length() );
                try
                {
                    artifact =
                        artifact.setFile( ProjectClasspathTest.getFileForClasspathResource( ProjectClasspathTest.dir
                            + "transitive-" + scope + "-dep.xml" ) );
                    result.setArtifact( artifact );
                }
                catch ( FileNotFoundException e )
                {
                    throw new IllegalStateException( "Missing test POM for " + artifact );
                }
            }
            else
            {
                result.addException( new ArtifactNotFoundException( artifact, null ) );
                throw new ArtifactResolutionException( results );
            }
        }
        return results;
    }
    public ArtifactResult resolveArtifact( RepositorySystemSession session, ArtifactRequest request )
        throws ArtifactResolutionException
    {
        return resolveArtifacts( session, Collections.singleton( request ) ).get( 0 );
    }
}
