package org.apache.maven.project;
import java.io.File;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;
public class LegacyLocalRepositoryManager
    extends SimpleLocalRepositoryManager
{
    public LegacyLocalRepositoryManager( File basedir )
    {
        super( basedir );
    }
    public String getPathForLocalArtifact( Artifact artifact )
    {
        StringBuilder path = new StringBuilder( 128 );
        path.append( artifact.getGroupId() ).append( '/' );
        path.append( artifact.getExtension() ).append( 's' ).append( '/' );
        path.append( artifact.getArtifactId() ).append( '-' ).append( artifact.getVersion() );
        if ( artifact.getClassifier().length() > 0 )
        {
            path.append( '-' ).append( artifact.getClassifier() );
        }
        path.append( '.' ).append( artifact.getExtension() );
        return path.toString();
    }
}
