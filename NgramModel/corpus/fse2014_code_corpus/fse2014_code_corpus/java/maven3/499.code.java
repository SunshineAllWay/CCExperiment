package org.apache.maven.project.artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ArtifactMetadataSource.class )
public class DefaultMetadataSource
    extends MavenMetadataSource
{
}
