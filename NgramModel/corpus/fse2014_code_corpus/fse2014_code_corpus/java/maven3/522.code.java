package org.apache.maven.repository.legacy.metadata;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
public class ResolutionGroup
{
    private final Set<Artifact> artifacts;
    private final List<ArtifactRepository> resolutionRepositories;
    private final Artifact pomArtifact;
    private final Artifact relocatedArtifact;
    private final Map<String, Artifact> managedVersions;
    public ResolutionGroup( Artifact pomArtifact, Set<Artifact> artifacts,
                            List<ArtifactRepository> resolutionRepositories )
    {
        this( pomArtifact, null, artifacts, null, resolutionRepositories );
    }
    public ResolutionGroup( Artifact pomArtifact, Artifact relocatedArtifact, Set<Artifact> artifacts,
                            Map<String, Artifact> managedVersions, List<ArtifactRepository> resolutionRepositories )
    {
        this.pomArtifact = pomArtifact;
        this.relocatedArtifact = relocatedArtifact;
        this.artifacts = artifacts;
        this.managedVersions = managedVersions;
        this.resolutionRepositories = resolutionRepositories;
    }
    public Artifact getPomArtifact()
    {
        return pomArtifact;
    }
    public Artifact getRelocatedArtifact()
    {
        return relocatedArtifact;
    }
    public Set<Artifact> getArtifacts()
    {
        return artifacts;
    }
    public List<ArtifactRepository> getResolutionRepositories()
    {
        return resolutionRepositories;
    }
    public Map<String, Artifact> getManagedVersions()
    {
        return managedVersions;
    }
}
