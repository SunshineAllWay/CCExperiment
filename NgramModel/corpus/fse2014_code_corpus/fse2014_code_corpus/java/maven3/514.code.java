package org.apache.maven.repository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
public abstract class LocalArtifactRepository
    extends MavenArtifactRepository
{   
    public static final String IDE_WORKSPACE = "ide-workspace";
    public abstract Artifact find( Artifact artifact );
    public abstract boolean hasLocalMetadata();    
}
