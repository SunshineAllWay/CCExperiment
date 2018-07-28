package org.apache.maven;
import java.util.Set;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
public interface ArtifactFilterManager
{
    ArtifactFilter getArtifactFilter();
    ArtifactFilter getCoreArtifactFilter();
    void excludeArtifact( String artifactId );
    Set<String> getCoreArtifactExcludes();
}
