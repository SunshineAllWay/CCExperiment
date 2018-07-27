package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
import java.util.List;
public class ExcludesArtifactFilter
    extends IncludesArtifactFilter
{
    public ExcludesArtifactFilter( List patterns )
    {
        super( patterns );
    }
    public boolean include( Artifact artifact )
    {
        return !super.include( artifact );
    }
}
