package org.apache.maven.artifact.resolver.filter;
import java.util.List;
import org.apache.maven.artifact.Artifact;
public class ExcludesArtifactFilter
    extends IncludesArtifactFilter
{
    public ExcludesArtifactFilter( List<String> patterns )
    {
        super( patterns );
    }
    public boolean include( Artifact artifact )
    {
        return !super.include( artifact );
    }
}
