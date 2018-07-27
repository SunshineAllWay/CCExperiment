package org.apache.maven.artifact.handler.manager;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import java.util.Map;
import java.util.Set;
public class DefaultArtifactHandlerManager
    implements ArtifactHandlerManager
{
    private Map artifactHandlers;
    public ArtifactHandler getArtifactHandler( String type )
    {
        ArtifactHandler handler = (ArtifactHandler) artifactHandlers.get( type );
        if ( handler == null )
        {
            handler = new DefaultArtifactHandler( type );
        }
        return handler;
    }
    public void addHandlers( Map handlers )
    {
        artifactHandlers.putAll( handlers );
    }
    public Set getHandlerTypes()
    {
        return artifactHandlers.keySet();
    }
}
