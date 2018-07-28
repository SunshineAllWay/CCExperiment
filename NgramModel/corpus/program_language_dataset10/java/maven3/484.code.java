package org.apache.maven.project;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.building.ModelSource;
public interface ProjectBuilder
{
    ProjectBuildingResult build( File projectFile, ProjectBuildingRequest request )
        throws ProjectBuildingException;
    ProjectBuildingResult build( Artifact projectArtifact, ProjectBuildingRequest request )
        throws ProjectBuildingException;
    ProjectBuildingResult build( Artifact projectArtifact, boolean allowStubModel, ProjectBuildingRequest request )
        throws ProjectBuildingException;
    ProjectBuildingResult build( ModelSource modelSource, ProjectBuildingRequest request )
        throws ProjectBuildingException;
    List<ProjectBuildingResult> build( List<File> pomFiles, boolean recursive, ProjectBuildingRequest config )
        throws ProjectBuildingException;
}
