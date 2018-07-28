package org.apache.maven.model.resolution;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
public interface ModelResolver
{
    ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException;
    void addRepository( Repository repository )
        throws InvalidRepositoryException;
    ModelResolver newCopy();
}
