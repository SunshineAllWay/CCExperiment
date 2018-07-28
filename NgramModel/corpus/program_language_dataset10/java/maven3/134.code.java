package org.apache.maven.repository.legacy;
import java.util.IdentityHashMap;
import java.util.Map;
import org.apache.maven.repository.ArtifactTransferEvent;
import org.apache.maven.repository.ArtifactTransferListener;
import org.apache.maven.repository.ArtifactTransferResource;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
public class TransferListenerAdapter
    implements TransferListener
{
    private final ArtifactTransferListener listener;
    private final Map<Resource, ArtifactTransferResource> artifacts;
    private final Map<Resource, Long> transfers;
    public static TransferListener newAdapter( ArtifactTransferListener listener )
    {
        if ( listener == null )
        {
            return null;
        }
        else
        {
            return new TransferListenerAdapter( listener );
        }
    }
    private TransferListenerAdapter( ArtifactTransferListener listener )
    {
        this.listener = listener;
        this.artifacts = new IdentityHashMap<Resource, ArtifactTransferResource>();
        this.transfers = new IdentityHashMap<Resource, Long>();
    }
    public void debug( String message )
    {
    }
    public void transferCompleted( TransferEvent transferEvent )
    {
        ArtifactTransferEvent event = wrap( transferEvent );
        Long transferred = null;
        synchronized ( transfers )
        {
            transferred = transfers.remove( transferEvent.getResource() );
        }
        if ( transferred != null )
        {
            event.setTransferredBytes( transferred.longValue() );
        }
        synchronized ( artifacts )
        {
            artifacts.remove( transferEvent.getResource() );
        }
        listener.transferCompleted( event );
    }
    public void transferError( TransferEvent transferEvent )
    {
        synchronized ( transfers )
        {
            transfers.remove( transferEvent.getResource() );
        }
        synchronized ( artifacts )
        {
            artifacts.remove( transferEvent.getResource() );
        }
    }
    public void transferInitiated( TransferEvent transferEvent )
    {
        listener.transferInitiated( wrap( transferEvent ) );
    }
    public void transferProgress( TransferEvent transferEvent, byte[] buffer, int length )
    {
        Long transferred;
        synchronized ( transfers )
        {
            transferred = transfers.get( transferEvent.getResource() );
            if ( transferred == null )
            {
                transferred = Long.valueOf( length );
            }
            else
            {
                transferred = Long.valueOf( transferred.longValue() + length );
            }
            transfers.put( transferEvent.getResource(), transferred );
        }
        ArtifactTransferEvent event = wrap( transferEvent );
        event.setDataBuffer( buffer );
        event.setDataOffset( 0 );
        event.setDataLength( length );
        event.setTransferredBytes( transferred.longValue() );
        listener.transferProgress( event );
    }
    public void transferStarted( TransferEvent transferEvent )
    {
        listener.transferStarted( wrap( transferEvent ) );
    }
    private ArtifactTransferEvent wrap( TransferEvent event )
    {
        if ( event == null )
        {
            return null;
        }
        else
        {
            String wagon = event.getWagon().getClass().getName();
            ArtifactTransferResource artifact = wrap( event.getWagon().getRepository(), event.getResource() );
            ArtifactTransferEvent evt;
            if ( event.getException() != null )
            {
                evt = new ArtifactTransferEvent( wagon, event.getException(), event.getRequestType(), artifact );
            }
            else
            {
                evt = new ArtifactTransferEvent( wagon, event.getEventType(), event.getRequestType(), artifact );
            }
            evt.setLocalFile( event.getLocalFile() );
            return evt;
        }
    }
    private ArtifactTransferResource wrap( Repository repository, Resource resource )
    {
        if ( resource == null )
        {
            return null;
        }
        else
        {
            synchronized ( artifacts )
            {
                ArtifactTransferResource artifact = artifacts.get( resource );
                if ( artifact == null )
                {
                    artifact = new MavenArtifact( repository.getUrl(), resource );
                    artifacts.put( resource, artifact );
                }
                return artifact;
            }
        }
    }
}
