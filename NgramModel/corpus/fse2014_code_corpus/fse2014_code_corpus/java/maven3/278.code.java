package org.apache.maven.artifact.handler.manager;
import java.util.Map;
import org.apache.maven.artifact.handler.ArtifactHandler;
public interface ArtifactHandlerManager
{
    String ROLE = ArtifactHandlerManager.class.getName();
    ArtifactHandler getArtifactHandler( String type );
    @Deprecated
    void addHandlers( Map<String, ArtifactHandler> handlers );
}
