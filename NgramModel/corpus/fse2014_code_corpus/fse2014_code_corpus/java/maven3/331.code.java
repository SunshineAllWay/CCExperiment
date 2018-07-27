package org.apache.maven.eventspy.internal;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositoryListener;
class EventSpyRepositoryListener
    extends AbstractRepositoryListener
{
    private final EventSpyDispatcher dispatcher;
    private final RepositoryListener delegate;
    public EventSpyRepositoryListener( EventSpyDispatcher dispatcher, RepositoryListener delegate )
    {
        this.dispatcher = dispatcher;
        this.delegate = delegate;
    }
    @Override
    public void artifactDeployed( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactDeployed( event );
    }
    @Override
    public void artifactDeploying( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactDeploying( event );
    }
    @Override
    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactDescriptorInvalid( event );
    }
    @Override
    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactDescriptorMissing( event );
    }
    @Override
    public void artifactInstalled( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactInstalled( event );
    }
    @Override
    public void artifactInstalling( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactInstalling( event );
    }
    @Override
    public void artifactResolved( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactResolved( event );
    }
    @Override
    public void artifactResolving( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactResolving( event );
    }
    @Override
    public void metadataDeployed( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataDeployed( event );
    }
    @Override
    public void metadataDeploying( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataDeploying( event );
    }
    @Override
    public void metadataInstalled( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataInstalled( event );
    }
    @Override
    public void metadataInstalling( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataInstalling( event );
    }
    @Override
    public void metadataInvalid( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataInvalid( event );
    }
    @Override
    public void metadataResolved( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataResolved( event );
    }
    @Override
    public void metadataResolving( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataResolving( event );
    }
    @Override
    public void artifactDownloaded( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactDownloaded( event );
    }
    @Override
    public void artifactDownloading( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.artifactDownloading( event );
    }
    @Override
    public void metadataDownloaded( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataDownloaded( event );
    }
    @Override
    public void metadataDownloading( RepositoryEvent event )
    {
        dispatcher.onEvent( event );
        delegate.metadataDownloading( event );
    }
}
