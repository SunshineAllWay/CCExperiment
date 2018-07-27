package org.apache.maven.repository.legacy.resolver.conflict;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ConflictResolver.class, hint = "newest" )
public class NewestConflictResolver
    implements ConflictResolver
{
    public ResolutionNode resolveConflict( ResolutionNode node1, ResolutionNode node2 )
    {
        try
        {
            ArtifactVersion version1 = node1.getArtifact().getSelectedVersion();
            ArtifactVersion version2 = node2.getArtifact().getSelectedVersion();
            return version1.compareTo( version2 ) > 0 ? node1 : node2;
        }
        catch ( OverConstrainedVersionException exception )
        {
            return null;
        }
    }
}
