package org.apache.maven.repository;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.settings.Mirror;
public interface MirrorSelector
{
    Mirror getMirror( ArtifactRepository repository, List<Mirror> mirrors );
}
