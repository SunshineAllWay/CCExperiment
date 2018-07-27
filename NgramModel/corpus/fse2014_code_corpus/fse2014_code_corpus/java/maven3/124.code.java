package org.apache.maven.repository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
public class MetadataResolutionRequest
{
    private MavenArtifactMetadata mad;
    private String scope;
    private Set<Artifact> artifactDependencies;
    private ArtifactRepository localRepository;
    private List<ArtifactRepository> remoteRepositories;
    private Map managedVersionMap;
    private boolean asList = true;
    private boolean asDirtyTree = false;
    private boolean asResolvedTree = false;
    private boolean asGraph = false;
    public MetadataResolutionRequest()
    {
    }
    public MetadataResolutionRequest( MavenArtifactMetadata md, ArtifactRepository localRepository,
                                      List<ArtifactRepository> remoteRepositories )
    {
        this.mad = md;
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
    }
    public MavenArtifactMetadata getArtifactMetadata()
    {
        return mad;
    }
    public MetadataResolutionRequest setArtifactMetadata( MavenArtifactMetadata md )
    {
        this.mad = md;
        return this;
    }
    public MetadataResolutionRequest setArtifactDependencies( Set<Artifact> artifactDependencies )
    {
        this.artifactDependencies = artifactDependencies;
        return this;
    }
    public Set<Artifact> getArtifactDependencies()
    {
        return artifactDependencies;
    }
    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }
    public MetadataResolutionRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
        return this;
    }
    public List<ArtifactRepository> getRemoteRepostories()
    {
        return remoteRepositories;
    }
    public MetadataResolutionRequest setRemoteRepostories( List<ArtifactRepository> remoteRepostories )
    {
        this.remoteRepositories = remoteRepostories;
        return this;
    }
    public Map getManagedVersionMap()
    {
        return managedVersionMap;
    }
    public MetadataResolutionRequest setManagedVersionMap( Map managedVersionMap )
    {
        this.managedVersionMap = managedVersionMap;
        return this;
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder()
                .append( "REQUEST: " ).append(  "\n" )
                .append( "artifact: " ).append( mad ).append(  "\n" )
                .append( artifactDependencies ).append(  "\n" )
                .append( "localRepository: " ).append(  localRepository ).append(  "\n" )
                .append( "remoteRepositories: " ).append(  remoteRepositories ).append(  "\n" )
                ;
        return sb.toString();
    }
    public boolean isAsList()
    {
        return asList;
    }
    public MetadataResolutionRequest setAsList( boolean asList )
    {
        this.asList = asList;
        return this;
    }
    public boolean isAsDirtyTree()
    {
        return asDirtyTree;
    }
    public MetadataResolutionRequest setAsDirtyTree( boolean asDirtyTree )
    {
        this.asDirtyTree = asDirtyTree;
        return this;
    }
    public boolean isAsResolvedTree()
    {
        return asResolvedTree;
    }
    public MetadataResolutionRequest setAsResolvedTree( boolean asResolvedTree )
    {
        this.asResolvedTree = asResolvedTree;
        return this;
    }
    public boolean isAsGraph()
    {
        return asGraph;
    }
    public MetadataResolutionRequest setAsGraph( boolean asGraph )
    {
        this.asGraph = asGraph;
        return this;
    }
    public MetadataResolutionRequest setScope( String scope )
    {
        this.scope = scope;
        return this;
    }
    public String getScope()
    {
        return scope;
    }
}
