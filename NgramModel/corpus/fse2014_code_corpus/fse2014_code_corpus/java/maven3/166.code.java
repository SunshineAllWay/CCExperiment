package org.apache.maven.repository.metadata;
import org.apache.maven.artifact.ArtifactScopeEnum;
public interface GraphConflictResolver
{
    String ROLE = GraphConflictResolver.class.getName();
    MetadataGraph resolveConflicts( MetadataGraph graph, ArtifactScopeEnum scope )
        throws GraphConflictResolutionException;
}
