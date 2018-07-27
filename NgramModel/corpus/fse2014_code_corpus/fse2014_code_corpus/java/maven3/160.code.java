package org.apache.maven.repository.metadata;
import org.apache.maven.artifact.ArtifactScopeEnum;
public interface ClasspathTransformation
{
    String ROLE = ClasspathTransformation.class.getName();
    ClasspathContainer transform( MetadataGraph dirtyGraph, ArtifactScopeEnum scope, boolean resolve )
        throws MetadataGraphTransformationException;
}
