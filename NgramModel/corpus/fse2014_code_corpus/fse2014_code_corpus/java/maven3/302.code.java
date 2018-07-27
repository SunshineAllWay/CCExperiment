package org.apache.maven.artifact.resolver;
public interface ResolutionErrorHandler
{
    void throwErrors( ArtifactResolutionRequest request, ArtifactResolutionResult result )
        throws ArtifactResolutionException;
}
