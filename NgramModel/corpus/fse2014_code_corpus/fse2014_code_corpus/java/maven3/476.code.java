package org.apache.maven.project;
import java.util.List;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
public interface DependencyResolutionResult
{
    DependencyNode getDependencyGraph();
    List<Dependency> getDependencies();
    List<Dependency> getResolvedDependencies();
    List<Dependency> getUnresolvedDependencies();
    List<Exception> getCollectionErrors();
    List<Exception> getResolutionErrors( Dependency dependency );
}
