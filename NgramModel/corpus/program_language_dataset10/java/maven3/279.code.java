package org.apache.maven.artifact.handler.manager;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
@Component( role = ArtifactHandlerManager.class )
public class DefaultArtifactHandlerManager
    implements ArtifactHandlerManager
{
    @Requirement( role = ArtifactHandler.class )
    private Map<String, ArtifactHandler> artifactHandlers;
    private Map<String, ArtifactHandler> unmanagedHandlers = new ConcurrentHashMap<String, ArtifactHandler>();
    public ArtifactHandler getArtifactHandler( String type )
    {
        ArtifactHandler handler = unmanagedHandlers.get( type );
        if ( handler == null )
        {
            handler = artifactHandlers.get( type );
            if ( handler == null )
            {
                handler = new DefaultArtifactHandler( type );
            }
        }
        return handler;
    }
    public void addHandlers( Map<String, ArtifactHandler> handlers )
    {
        unmanagedHandlers.putAll( handlers );
    }
    @Deprecated
    public Set<String> getHandlerTypes()
    {
        return artifactHandlers.keySet();
    }
}
