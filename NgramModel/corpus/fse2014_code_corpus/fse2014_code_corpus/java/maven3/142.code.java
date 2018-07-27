package org.apache.maven.repository.legacy.resolver.conflict;
import org.apache.maven.artifact.resolver.ResolutionNode;
public interface ConflictResolver
{
    String ROLE = ConflictResolver.class.getName();
    ResolutionNode resolveConflict( ResolutionNode node1, ResolutionNode node2 );
}
