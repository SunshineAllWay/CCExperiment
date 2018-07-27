package org.apache.maven.artifact.handler.manager;
import org.apache.maven.artifact.handler.ArtifactHandler;
import java.util.Map;
public interface ArtifactHandlerManager
{
    String ROLE = ArtifactHandlerManager.class.getName();
    ArtifactHandler getArtifactHandler( String type );
    void addHandlers( Map handlers );
}