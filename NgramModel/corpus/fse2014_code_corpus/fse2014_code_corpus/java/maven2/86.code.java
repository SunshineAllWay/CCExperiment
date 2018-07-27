package org.apache.maven.artifact.repository.metadata;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class DefaultRepositoryMetadataManager
    extends AbstractLogEnabled
    implements RepositoryMetadataManager
{
    private WagonManager wagonManager;
    private Set cachedMetadata = new HashSet();
    public void resolve( RepositoryMetadata metadata, List remoteRepositories, ArtifactRepository localRepository )
        throws RepositoryMetadataResolutionException
    {
        boolean alreadyResolved = alreadyResolved( metadata );
        if ( !alreadyResolved )
        {
            for ( Iterator i = remoteRepositories.iterator(); i.hasNext(); )
            {
                ArtifactRepository repository = (ArtifactRepository) i.next();
                ArtifactRepositoryPolicy policy =
                    metadata.isSnapshot() ? repository.getSnapshots() : repository.getReleases();
                if ( !policy.isEnabled() )
                {
                    getLogger().debug( "Skipping disabled repository " + repository.getId() );
                }
                else if ( repository.isBlacklisted() )
                {
                    getLogger().debug( "Skipping blacklisted repository " + repository.getId() );
                }
                else
                {
                    File file = new File( localRepository.getBasedir(),
                                          localRepository.pathOfLocalRepositoryMetadata( metadata, repository ) );
                    boolean checkForUpdates =
                        !file.exists() || policy.checkOutOfDate( new Date( file.lastModified() ) );
                    if ( checkForUpdates )
                    {
                        if ( wagonManager.isOnline() )
                        {
                            getLogger().info( metadata.getKey() + ": checking for updates from " + repository.getId() );
                            boolean storeMetadata = false;
                            try
                            {
                                wagonManager.getArtifactMetadata( metadata, repository, file,
                                                                  policy.getChecksumPolicy() );
                                storeMetadata = true;
                            }
                            catch ( ResourceDoesNotExistException e )
                            {
                                getLogger().debug(
                                    metadata + " could not be found on repository: " + repository.getId() );
                                if ( file.exists() )
                                {
                                    file.delete();
                                }
                                storeMetadata = true;
                            }
                            catch ( TransferFailedException e )
                            {
                                getLogger().warn( metadata + " could not be retrieved from repository: "
                                    + repository.getId() + " due to an error: " + e.getMessage() );
                                getLogger().debug( "Exception", e );
                                getLogger().info( "Repository '" + repository.getId() + "' will be blacklisted" );
                                repository.setBlacklisted( true );
                            }
                            if ( storeMetadata )
                            {
                                if ( file.exists() )
                                {
                                    file.setLastModified( System.currentTimeMillis() );
                                }
                                else
                                {
                                    try
                                    {
                                        metadata.storeInLocalRepository( localRepository, repository );
                                    }
                                    catch ( RepositoryMetadataStoreException e )
                                    {
                                        throw new RepositoryMetadataResolutionException(
                                            "Unable to store local copy of metadata: " + e.getMessage(), e );
                                    }
                                }
                            }
                        }
                        else
                        {
                            getLogger().debug( "System is offline. Cannot resolve metadata:\n"
                                + metadata.extendedToString() + "\n\n" );
                        }
                    }
                }
            }
            cachedMetadata.add( metadata.getKey() );
        }
        try
        {
            mergeMetadata( metadata, remoteRepositories, localRepository );
        }
        catch ( RepositoryMetadataStoreException e )
        {
            throw new RepositoryMetadataResolutionException(
                "Unable to store local copy of metadata: " + e.getMessage(), e );
        }
        catch ( RepositoryMetadataReadException e )
        {
            throw new RepositoryMetadataResolutionException( "Unable to read local copy of metadata: " + e.getMessage(),
                                                             e );
        }
    }
    private void mergeMetadata( RepositoryMetadata metadata, List remoteRepositories,
                                ArtifactRepository localRepository )
        throws RepositoryMetadataStoreException, RepositoryMetadataReadException
    {
        Map previousMetadata = new HashMap();
        ArtifactRepository selected = null;
        for ( Iterator i = remoteRepositories.iterator(); i.hasNext(); )
        {
            ArtifactRepository repository = (ArtifactRepository) i.next();
            ArtifactRepositoryPolicy policy =
                metadata.isSnapshot() ? repository.getSnapshots() : repository.getReleases();
            if ( policy.isEnabled() && loadMetadata( metadata, repository, localRepository, previousMetadata ) )
            {
                metadata.setRepository( repository );
                selected = repository;
            }
        }
        if ( loadMetadata( metadata, localRepository, localRepository, previousMetadata ) )
        {
            metadata.setRepository( null );
            selected = localRepository;
        }
        updateSnapshotMetadata( metadata, previousMetadata, selected, localRepository );
    }
    private void updateSnapshotMetadata( RepositoryMetadata metadata, Map previousMetadata, ArtifactRepository selected,
                                         ArtifactRepository localRepository )
        throws RepositoryMetadataStoreException
    {
        if ( metadata.isSnapshot() )
        {
            Metadata prevMetadata = metadata.getMetadata();
            for ( Iterator i = previousMetadata.keySet().iterator(); i.hasNext(); )
            {
                ArtifactRepository repository = (ArtifactRepository) i.next();
                Metadata m = (Metadata) previousMetadata.get( repository );
                if ( repository.equals( selected ) )
                {
                    if ( m.getVersioning() == null )
                    {
                        m.setVersioning( new Versioning() );
                    }
                    if ( m.getVersioning().getSnapshot() == null )
                    {
                        m.getVersioning().setSnapshot( new Snapshot() );
                    }
                }
                else
                {
                    if ( ( m.getVersioning() != null ) && ( m.getVersioning().getSnapshot() != null )
                        && m.getVersioning().getSnapshot().isLocalCopy() )
                    {
                        m.getVersioning().getSnapshot().setLocalCopy( false );
                        metadata.setMetadata( m );
                        metadata.storeInLocalRepository( localRepository, repository );
                    }
                }
            }
            metadata.setMetadata( prevMetadata );
        }
    }
    private boolean loadMetadata( RepositoryMetadata repoMetadata, ArtifactRepository remoteRepository,
                                  ArtifactRepository localRepository, Map previousMetadata )
        throws RepositoryMetadataReadException
    {
        boolean setRepository = false;
        File metadataFile = new File( localRepository.getBasedir(),
                                      localRepository.pathOfLocalRepositoryMetadata( repoMetadata, remoteRepository ) );
        if ( metadataFile.exists() )
        {
            Metadata metadata = readMetadata( metadataFile );
            if ( repoMetadata.isSnapshot() && ( previousMetadata != null ) )
            {
                previousMetadata.put( remoteRepository, metadata );
            }
            if ( repoMetadata.getMetadata() != null )
            {
                setRepository = repoMetadata.getMetadata().merge( metadata );
            }
            else
            {
                repoMetadata.setMetadata( metadata );
                setRepository = true;
            }
        }
        return setRepository;
    }
    protected static Metadata readMetadata( File mappingFile )
        throws RepositoryMetadataReadException
    {
        Metadata result;
        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( mappingFile );
            MetadataXpp3Reader mappingReader = new MetadataXpp3Reader();
            result = mappingReader.read( reader, false );
        }
        catch ( FileNotFoundException e )
        {
            throw new RepositoryMetadataReadException( "Cannot read metadata from '" + mappingFile + "'", e );
        }
        catch ( IOException e )
        {
            throw new RepositoryMetadataReadException(
                "Cannot read metadata from '" + mappingFile + "': " + e.getMessage(), e );
        }
        catch ( XmlPullParserException e )
        {
            throw new RepositoryMetadataReadException(
                "Cannot read metadata from '" + mappingFile + "': " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( reader );
        }
        return result;
    }
    public void resolveAlways( RepositoryMetadata metadata, ArtifactRepository localRepository,
                               ArtifactRepository remoteRepository )
        throws RepositoryMetadataResolutionException
    {
        if ( !wagonManager.isOnline() )
        {
            throw new RepositoryMetadataResolutionException(
                "System is offline. Cannot resolve required metadata:\n" + metadata.extendedToString() );
        }
        File file;
        try
        {
            file = getArtifactMetadataFromDeploymentRepository( metadata, localRepository, remoteRepository );
        }
        catch ( TransferFailedException e )
        {
            throw new RepositoryMetadataResolutionException( metadata + " could not be retrieved from repository: "
                + remoteRepository.getId() + " due to an error: " + e.getMessage(), e );
        }
        try
        {
            if ( file.exists() )
            {
                Metadata prevMetadata = readMetadata( file );
                metadata.setMetadata( prevMetadata );
            }
        }
        catch ( RepositoryMetadataReadException e )
        {
            throw new RepositoryMetadataResolutionException( e.getMessage(), e );
        }
    }
    private File getArtifactMetadataFromDeploymentRepository( ArtifactMetadata metadata,
                                                              ArtifactRepository localRepository,
                                                              ArtifactRepository remoteRepository )
        throws TransferFailedException
    {
        File file = new File( localRepository.getBasedir(),
                              localRepository.pathOfLocalRepositoryMetadata( metadata, remoteRepository ) );
        try
        {
            wagonManager.getArtifactMetadataFromDeploymentRepository( metadata, remoteRepository, file,
                                                                      ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN );
        }
        catch ( ResourceDoesNotExistException e )
        {
            getLogger().info(
                metadata + " could not be found on repository: " + remoteRepository.getId() + ", so will be created" );
            if ( file.exists() )
            {
                file.delete();
            }
        }
        return file;
    }
    private boolean alreadyResolved( ArtifactMetadata metadata )
    {
        return cachedMetadata.contains( metadata.getKey() );
    }
    public void deploy( ArtifactMetadata metadata, ArtifactRepository localRepository,
                        ArtifactRepository deploymentRepository )
        throws RepositoryMetadataDeploymentException
    {
        if ( !wagonManager.isOnline() )
        {
            throw new RepositoryMetadataDeploymentException(
                "System is offline. Cannot deploy metadata:\n" + metadata.extendedToString() );
        }
        File file;
        if ( metadata instanceof RepositoryMetadata )
        {
            getLogger().info( "Retrieving previous metadata from " + deploymentRepository.getId() );
            try
            {
                file = getArtifactMetadataFromDeploymentRepository( metadata, localRepository, deploymentRepository );
            }
            catch ( TransferFailedException e )
            {
                throw new RepositoryMetadataDeploymentException( metadata
                    + " could not be retrieved from repository: " + deploymentRepository.getId() + " due to an error: "
                    + e.getMessage(), e );
            }
        }
        else
        {
            file = new File( localRepository.getBasedir(),
                             localRepository.pathOfLocalRepositoryMetadata( metadata, deploymentRepository ) );
        }
        try
        {
            metadata.storeInLocalRepository( localRepository, deploymentRepository );
        }
        catch ( RepositoryMetadataStoreException e )
        {
            throw new RepositoryMetadataDeploymentException( "Error installing metadata: " + e.getMessage(), e );
        }
        try
        {
            wagonManager.putArtifactMetadata( file, metadata, deploymentRepository );
        }
        catch ( TransferFailedException e )
        {
            throw new RepositoryMetadataDeploymentException( "Error while deploying metadata: " + e.getMessage(), e );
        }
    }
    public void install( ArtifactMetadata metadata, ArtifactRepository localRepository )
        throws RepositoryMetadataInstallationException
    {
        try
        {
            metadata.storeInLocalRepository( localRepository, localRepository );
        }
        catch ( RepositoryMetadataStoreException e )
        {
            throw new RepositoryMetadataInstallationException( "Error installing metadata: " + e.getMessage(), e );
        }
    }
}
