package org.apache.maven.artifact.manager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.observers.ChecksumObserver;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
public class DefaultWagonManager
    extends AbstractLogEnabled
    implements WagonManager, Contextualizable, Initializable
{
    private static final String WILDCARD = "*";
    private static final String EXTERNAL_WILDCARD = "external:*";
    private static final String MAVEN_ARTIFACT_PROPERTIES = "META-INF/maven/org.apache.maven/maven-artifact/pom.properties";
    private static final String WAGON_PROVIDER_CONFIGURATION = "wagonProvider";
    private static int anonymousMirrorIdSeed = 0;
    private PlexusContainer container;
    private Map proxies = new HashMap();
    private Map authenticationInfoMap = new HashMap();
    private Map serverPermissionsMap = new HashMap();
    private Map mirrors = new LinkedHashMap();
    private Map<String, XmlPlexusConfiguration> serverConfigurationMap = new HashMap<String, XmlPlexusConfiguration>();
    private Map<String, String> serverWagonProviderMap = new HashMap<String, String>();
    private TransferListener downloadMonitor;
    private boolean online = true;
    private ArtifactRepositoryFactory repositoryFactory;
    private boolean interactive = true;
    private Map<String, PlexusContainer> availableWagons = new HashMap<String, PlexusContainer>();
    private RepositoryPermissions defaultRepositoryPermissions;
    private String httpUserAgent;
    private WagonProviderMapping providerMapping = new DefaultWagonProviderMapping();
    public Wagon getWagon( Repository repository )
        throws UnsupportedProtocolException, WagonConfigurationException
    {
        String protocol = repository.getProtocol();
        if ( protocol == null )
        {
            throw new UnsupportedProtocolException( "The repository " + repository + " does not specify a protocol" );
        }
        Wagon wagon = getWagon( protocol, repository.getId() );
        configureWagon( wagon, repository.getId(), protocol );
        return wagon;
    }
    public Wagon getWagon( String protocol )
        throws UnsupportedProtocolException
    {
        return getWagon( protocol, null );
    }
    private Wagon getWagon( String protocol, String repositoryId )
        throws UnsupportedProtocolException
    {
        String hint = getWagonHint( protocol, repositoryId );
        PlexusContainer container = getWagonContainer( hint );
        Wagon wagon;
        try
        {
            wagon = (Wagon) container.lookup( Wagon.ROLE, hint );
        }
        catch ( ComponentLookupException e1 )
        {
            throw new UnsupportedProtocolException(
                "Cannot find wagon which supports the requested protocol: " + protocol, e1 );
        }
        wagon.setInteractive( interactive );
        return wagon;
    }
    private String getWagonHint( String protocol, String repositoryId )
    {
        String impl = null;
        if ( repositoryId != null && serverWagonProviderMap.containsKey( repositoryId ) )
        {
            impl = serverWagonProviderMap.get( repositoryId );
            getLogger().debug( "Using Wagon implementation " + impl + " from settings for server " + repositoryId );
        }
        else
        {
            impl = providerMapping.getWagonProvider( protocol );
            if ( impl != null )
            {
                getLogger().debug( "Using Wagon implementation " + impl + " from default mapping for protocol " + protocol );
            }
        }
        String hint;
        if ( impl != null )
        {
            hint = protocol + "-" + impl;
            PlexusContainer container = getWagonContainer( hint );
            if ( container == null || !container.hasComponent( Wagon.ROLE, hint ) )
            {
                getLogger().debug(
                                   "Cannot find wagon for protocol-provider hint: '" + hint
                                       + "', configured for repository: '" + repositoryId + "'. Using protocol hint: '"
                                       + protocol + "' instead." );
                hint = protocol;
            }
        }
        else
        {
            hint = protocol;
        }
        return hint;
    }
    private PlexusContainer getWagonContainer( String hint )
    {
        PlexusContainer container = this.container;
        if ( availableWagons.containsKey( hint ) )
        {
            container = availableWagons.get( hint );
        }
        return container;
    }
    public void putArtifact( File source,
                             Artifact artifact,
                             ArtifactRepository deploymentRepository )
        throws TransferFailedException
    {
        putRemoteFile( deploymentRepository, source, deploymentRepository.pathOf( artifact ), downloadMonitor );
    }
    public void putArtifactMetadata( File source,
                                     ArtifactMetadata artifactMetadata,
                                     ArtifactRepository repository )
        throws TransferFailedException
    {
        getLogger().info( "Uploading " + artifactMetadata );
        putRemoteFile( repository, source, repository.pathOfRemoteRepositoryMetadata( artifactMetadata ), null );
    }
    private void putRemoteFile( ArtifactRepository repository,
                                File source,
                                String remotePath,
                                TransferListener downloadMonitor )
        throws TransferFailedException
    {
        failIfNotOnline();
        String protocol = repository.getProtocol();
        Wagon wagon;
        try
        {
            wagon = getWagon( protocol, repository.getId() );
            configureWagon( wagon, repository );
        }
        catch ( UnsupportedProtocolException e )
        {
            throw new TransferFailedException( "Unsupported Protocol: '" + protocol + "': " + e.getMessage(), e );
        }
        if ( downloadMonitor != null )
        {
            wagon.addTransferListener( downloadMonitor );
        }
        Map checksums = new HashMap( 2 );
        Map sums = new HashMap( 2 );
        try
        {
            ChecksumObserver checksumObserver = new ChecksumObserver( "MD5" );
            wagon.addTransferListener( checksumObserver );
            checksums.put( "md5", checksumObserver );
            checksumObserver = new ChecksumObserver( "SHA-1" );
            wagon.addTransferListener( checksumObserver );
            checksums.put( "sha1", checksumObserver );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new TransferFailedException( "Unable to add checksum methods: " + e.getMessage(), e );
        }
        try
        {
            Repository artifactRepository = new Repository( repository.getId(), repository.getUrl() );
            if ( serverPermissionsMap.containsKey( repository.getId() ) )
            {
                RepositoryPermissions perms = (RepositoryPermissions) serverPermissionsMap.get( repository.getId() );
                getLogger().debug(
                    "adding permissions to wagon connection: " + perms.getFileMode() + " " + perms.getDirectoryMode() );
                artifactRepository.setPermissions( perms );
            }
            else
            {
                if ( defaultRepositoryPermissions != null )
                {
                    artifactRepository.setPermissions( defaultRepositoryPermissions );
                }
                else
                {
                    getLogger().debug( "not adding permissions to wagon connection" );
                }
            }
            wagon.connect( artifactRepository, getAuthenticationInfo( repository.getId() ), new ProxyInfoProvider()
            {
                public ProxyInfo getProxyInfo( String protocol )
                {
                    return getProxy( protocol );
                }
            } );
            wagon.put( source, remotePath );
            wagon.removeTransferListener( downloadMonitor );
            for ( Iterator i = checksums.keySet().iterator(); i.hasNext(); )
            {
                String extension = (String) i.next();
                ChecksumObserver observer = (ChecksumObserver) checksums.get( extension );
                sums.put( extension, observer.getActualChecksum() );
            }
            for ( Iterator i = checksums.keySet().iterator(); i.hasNext(); )
            {
                String extension = (String) i.next();
                File temp = File.createTempFile( "maven-artifact", null );
                temp.deleteOnExit();
                FileUtils.fileWrite( temp.getAbsolutePath(), "UTF-8", (String) sums.get( extension ) );
                wagon.put( temp, remotePath + "." + extension );
            }
        }
        catch ( ConnectionException e )
        {
            throw new TransferFailedException( "Connection failed: " + e.getMessage(), e );
        }
        catch ( AuthenticationException e )
        {
            throw new TransferFailedException( "Authentication failed: " + e.getMessage(), e );
        }
        catch ( AuthorizationException e )
        {
            throw new TransferFailedException( "Authorization failed: " + e.getMessage(), e );
        }
        catch ( ResourceDoesNotExistException e )
        {
            throw new TransferFailedException( "Resource to deploy not found: " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new TransferFailedException( "Error creating temporary file for deployment: " + e.getMessage(), e );
        }
        finally
        {
            disconnectWagon( wagon );
            releaseWagon( protocol, wagon, repository.getId() );
        }
    }
    public void getArtifact( Artifact artifact,
                             List remoteRepositories )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        boolean successful = false;
        for ( Iterator iter = remoteRepositories.iterator(); iter.hasNext() && !successful; )
        {
            ArtifactRepository repository = (ArtifactRepository) iter.next();
            try
            {
                getArtifact( artifact, repository );
                successful = artifact.isResolved();
            }
            catch ( ResourceDoesNotExistException e )
            {
                getLogger().info( "Unable to find resource '" + artifact.getId() + "' in repository "
                    + repository.getId() + " (" + repository.getUrl() + ")" );
            }
            catch ( TransferFailedException e )
            {
                getLogger().warn( "Unable to get resource '" + artifact.getId() + "' from repository "
                    + repository.getId() + " (" + repository.getUrl() + "): " + e.getMessage() );
            }
        }
        if ( !successful && !artifact.getFile().exists() )
        {
            throw new ResourceDoesNotExistException( "Unable to download the artifact from any repository" );
        }
    }
    public void getArtifact( Artifact artifact,
                             ArtifactRepository repository )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        String remotePath = repository.pathOf( artifact );
        ArtifactRepositoryPolicy policy = artifact.isSnapshot() ? repository.getSnapshots() : repository.getReleases();
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
            getLogger().debug( "Trying repository " + repository.getId() );
            getRemoteFile( getMirrorRepository( repository ), artifact.getFile(), remotePath, downloadMonitor,
                                   policy.getChecksumPolicy(), false );
            getLogger().debug( "  Artifact resolved" );
            artifact.setResolved( true );
        }
    }
    public void getArtifactMetadata( ArtifactMetadata metadata,
                                     ArtifactRepository repository,
                                     File destination,
                                     String checksumPolicy )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        String remotePath = repository.pathOfRemoteRepositoryMetadata( metadata );
        getRemoteFile( getMirrorRepository( repository ), destination, remotePath, null, checksumPolicy, true );
    }
    public void getArtifactMetadataFromDeploymentRepository( ArtifactMetadata metadata, ArtifactRepository repository,
                                                             File destination, String checksumPolicy )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        String remotePath = repository.pathOfRemoteRepositoryMetadata( metadata );
        getRemoteFile( repository, destination, remotePath, null, checksumPolicy, true );
    }
    private void getRemoteFile( ArtifactRepository repository,
                                File destination,
                                String remotePath,
                                TransferListener downloadMonitor,
                                String checksumPolicy,
                                boolean force )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        failIfNotOnline();
        String protocol = repository.getProtocol();
        Wagon wagon;
        try
        {
            wagon = getWagon( protocol, repository.getId() );
            configureWagon( wagon, repository );
        }
        catch ( UnsupportedProtocolException e )
        {
            throw new TransferFailedException( "Unsupported Protocol: '" + protocol + "': " + e.getMessage(), e );
        }
        if ( downloadMonitor != null )
        {
            wagon.addTransferListener( downloadMonitor );
        }
        File temp = new File( destination + ".tmp" );
        temp.deleteOnExit();
        boolean downloaded = false;
        try
        {
            getLogger().debug( "Connecting to repository: \'" + repository.getId() + "\' with url: \'" + repository.getUrl() + "\'." );
            wagon.connect( new Repository( repository.getId(), repository.getUrl() ),
                           getAuthenticationInfo( repository.getId() ), new ProxyInfoProvider()
            {
                public ProxyInfo getProxyInfo( String protocol )
                {
                    return getProxy( protocol );
                }
            } );
            boolean firstRun = true;
            boolean retry = true;
            while ( firstRun || retry )
            {
                retry = false;
                ChecksumObserver md5ChecksumObserver = null;
                ChecksumObserver sha1ChecksumObserver = null;
                try
                {
                    md5ChecksumObserver = new ChecksumObserver( "MD5" );
                    wagon.addTransferListener( md5ChecksumObserver );
                    sha1ChecksumObserver = new ChecksumObserver( "SHA-1" );
                    wagon.addTransferListener( sha1ChecksumObserver );
                    if ( destination.exists() && !force )
                    {
                        try
                        {
                            downloaded = wagon.getIfNewer( remotePath, temp, destination.lastModified() );
                            if ( !downloaded )
                            {
                                destination.setLastModified( System.currentTimeMillis() );
                            }
                        }
                        catch ( UnsupportedOperationException e )
                        {
                            wagon.get( remotePath, temp );
                            downloaded = true;
                        }
                    }
                    else
                    {
                        wagon.get( remotePath, temp );
                        downloaded = true;
                    }
                }
                catch ( NoSuchAlgorithmException e )
                {
                    throw new TransferFailedException( "Unable to add checksum methods: " + e.getMessage(), e );
                }
                finally
                {
                    if ( md5ChecksumObserver != null )
                    {
                        wagon.removeTransferListener( md5ChecksumObserver );
                    }
                    if ( sha1ChecksumObserver != null )
                    {
                        wagon.removeTransferListener( sha1ChecksumObserver );
                    }
                }
                if ( downloaded )
                {
                    if ( downloadMonitor != null )
                    {
                        wagon.removeTransferListener( downloadMonitor );
                    }
                    try
                    {
                        verifyChecksum( sha1ChecksumObserver, destination, temp, remotePath, ".sha1", wagon );
                    }
                    catch ( ChecksumFailedException e )
                    {
                        if ( firstRun )
                        {
                            getLogger().warn( "*** CHECKSUM FAILED - " + e.getMessage() + " - RETRYING" );
                            retry = true;
                        }
                        else
                        {
                            handleChecksumFailure( checksumPolicy, e.getMessage(), e.getCause() );
                        }
                    }
                    catch ( ResourceDoesNotExistException sha1TryException )
                    {
                        getLogger().debug( "SHA1 not found, trying MD5", sha1TryException );
                        try
                        {
                            verifyChecksum( md5ChecksumObserver, destination, temp, remotePath, ".md5", wagon );
                        }
                        catch ( ChecksumFailedException e )
                        {
                            if ( firstRun )
                            {
                                retry = true;
                            }
                            else
                            {
                                handleChecksumFailure( checksumPolicy, e.getMessage(), e.getCause() );
                            }
                        }
                        catch ( ResourceDoesNotExistException md5TryException )
                        {
                            handleChecksumFailure( checksumPolicy, "Error retrieving checksum file for " + remotePath,
                                md5TryException );
                        }
                    }
                    if ( downloadMonitor != null )
                    {
                        wagon.addTransferListener( downloadMonitor );
                    }
                }
                firstRun = false;
            }
        }
        catch ( ConnectionException e )
        {
            throw new TransferFailedException( "Connection failed: " + e.getMessage(), e );
        }
        catch ( AuthenticationException e )
        {
            throw new TransferFailedException( "Authentication failed: " + e.getMessage(), e );
        }
        catch ( AuthorizationException e )
        {
            throw new TransferFailedException( "Authorization failed: " + e.getMessage(), e );
        }
        finally
        {
            disconnectWagon( wagon );
            releaseWagon( protocol, wagon, repository.getId() );
        }
        if ( downloaded )
        {
            if ( !temp.exists() )
            {
                throw new ResourceDoesNotExistException( "Downloaded file does not exist: " + temp );
            }
            if ( !temp.renameTo( destination ) )
            {
                try
                {
                    FileUtils.copyFile( temp, destination );
                    temp.delete();
                }
                catch ( IOException e )
                {
                    throw new TransferFailedException(
                        "Error copying temporary file to the final destination: " + e.getMessage(), e );
                }
            }
        }
    }
    public ArtifactRepository getMirrorRepository( ArtifactRepository repository )
    {
        ArtifactRepository mirror = getMirror( repository );
        if ( mirror != null )
        {
            String id = mirror.getId();
            if ( id == null )
            {
                id = repository.getId();
            }
            getLogger().debug( "Using mirror: " + mirror.getUrl() + " (id: " + id + ")" );
            repository = repositoryFactory.createArtifactRepository( id, mirror.getUrl(),
                                                                     repository.getLayout(), repository.getSnapshots(),
                                                                     repository.getReleases() );
        }
        return repository;
    }
    private void failIfNotOnline()
        throws TransferFailedException
    {
        if ( !isOnline() )
        {
            throw new TransferFailedException( "System is offline." );
        }
    }
    private void handleChecksumFailure( String checksumPolicy,
                                        String message,
                                        Throwable cause )
        throws ChecksumFailedException
    {
        if ( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( checksumPolicy ) )
        {
            throw new ChecksumFailedException( message, cause );
        }
        else if ( !ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( checksumPolicy ) )
        {
            getLogger().warn( "*** CHECKSUM FAILED - " + message + " - IGNORING" );
        }
    }
    private void verifyChecksum( ChecksumObserver checksumObserver,
                                 File destination,
                                 File tempDestination,
                                 String remotePath,
                                 String checksumFileExtension,
                                 Wagon wagon )
        throws ResourceDoesNotExistException, TransferFailedException, AuthorizationException
    {
        try
        {
            String actualChecksum = checksumObserver.getActualChecksum();
            File tempChecksumFile = new File( tempDestination + checksumFileExtension + ".tmp" );
            tempChecksumFile.deleteOnExit();
            wagon.get( remotePath + checksumFileExtension, tempChecksumFile );
            String expectedChecksum = FileUtils.fileRead( tempChecksumFile, "UTF-8" );
            expectedChecksum = expectedChecksum.trim();
            if ( expectedChecksum.regionMatches( true, 0, "MD", 0, 2 )
                || expectedChecksum.regionMatches( true, 0, "SHA", 0, 3 ) )
            {
                int lastSpacePos = expectedChecksum.lastIndexOf( ' ' );
                expectedChecksum = expectedChecksum.substring( lastSpacePos + 1 );
            }
            else
            {
                int spacePos = expectedChecksum.indexOf( ' ' );
                if ( spacePos != -1 )
                {
                    expectedChecksum = expectedChecksum.substring( 0, spacePos );
                }
            }
            if ( expectedChecksum.equalsIgnoreCase( actualChecksum ) )
            {
                File checksumFile = new File( destination + checksumFileExtension );
                if ( checksumFile.exists() )
                {
                    checksumFile.delete();
                }
                FileUtils.copyFile( tempChecksumFile, checksumFile );
            }
            else
            {
                throw new ChecksumFailedException( "Checksum failed on download: local = '" + actualChecksum
                    + "'; remote = '" + expectedChecksum + "'" );
            }
        }
        catch ( IOException e )
        {
            throw new ChecksumFailedException( "Invalid checksum file", e );
        }
    }
    private void disconnectWagon( Wagon wagon )
    {
        try
        {
            wagon.disconnect();
        }
        catch ( ConnectionException e )
        {
            getLogger().error( "Problem disconnecting from wagon - ignoring: " + e.getMessage() );
        }
    }
    private void releaseWagon( String protocol,
                               Wagon wagon, String repositoryId )
    {
        String hint = getWagonHint( protocol, repositoryId );
        PlexusContainer container = getWagonContainer( hint );
        try
        {
            container.release( wagon );
        }
        catch ( ComponentLifecycleException e )
        {
            getLogger().error( "Problem releasing wagon - ignoring: " + e.getMessage() );
        }
    }
    public ProxyInfo getProxy( String protocol )
    {
        ProxyInfo info = (ProxyInfo) proxies.get( protocol );
        if ( info != null )
        {
            getLogger().debug( "Using Proxy: " + info.getHost() );
        }
        return info;
    }
    public AuthenticationInfo getAuthenticationInfo( String id )
    {
        return (AuthenticationInfo) authenticationInfoMap.get( id );
    }
    public ArtifactRepository getMirror( ArtifactRepository originalRepository )
    {
        ArtifactRepository selectedMirror = (ArtifactRepository) mirrors.get( originalRepository.getId() );
        if ( null == selectedMirror )
        {
            Set keySet = mirrors.keySet();
            if ( keySet != null )
            {
                Iterator iter = keySet.iterator();
                while ( iter.hasNext() )
                {
                    String pattern = (String) iter.next();
                    if ( matchPattern( originalRepository, pattern ) )
                    {
                        selectedMirror = (ArtifactRepository) mirrors.get( pattern );
                        break;
                    }
                }
            }
        }
        return selectedMirror;
    }
    public boolean matchPattern( ArtifactRepository originalRepository, String pattern )
    {
        boolean result = false;
        String originalId = originalRepository.getId();
        if ( WILDCARD.equals( pattern ) || pattern.equals( originalId ) )
        {
            result = true;
        }
        else
        {
            String[] repos = pattern.split( "," );
            for ( int i = 0; i < repos.length; i++ )
            {
                String repo = repos[i];
                if ( repo.length() > 1 && repo.startsWith( "!" ) )
                {
                    if ( originalId.equals( repo.substring( 1 ) ) )
                    {
                        result = false;
                        break;
                    }
                }
                else if ( originalId.equals( repo ) )
                {
                    result = true;
                    break;
                }
                else if ( EXTERNAL_WILDCARD.equals( repo ) && isExternalRepo( originalRepository ) )
                {
                    result = true;
                }
                else if ( WILDCARD.equals( repo ) )
                {
                    result = true;
                }
            }
        }
        return result;
    }
    public boolean isExternalRepo( ArtifactRepository originalRepository )
    {
        try
        {
            URL url = new URL( originalRepository.getUrl() );
            return !( url.getHost().equals( "localhost" ) || url.getHost().equals( "127.0.0.1" ) || url.getProtocol().equals(
                                                                                                                              "file" ) );
        }
        catch ( MalformedURLException e )
        {
            return false;
        }
    }
    public void addProxy( String protocol,
                          String host,
                          int port,
                          String username,
                          String password,
                          String nonProxyHosts )
    {
        ProxyInfo proxyInfo = new ProxyInfo();
        proxyInfo.setHost( host );
        proxyInfo.setType( protocol );
        proxyInfo.setPort( port );
        proxyInfo.setNonProxyHosts( nonProxyHosts );
        proxyInfo.setUserName( username );
        proxyInfo.setPassword( password );
        proxies.put( protocol, proxyInfo );
    }
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
    public void setDownloadMonitor( TransferListener downloadMonitor )
    {
        this.downloadMonitor = downloadMonitor;
    }
    public void addAuthenticationInfo( String repositoryId,
                                       String username,
                                       String password,
                                       String privateKey,
                                       String passphrase )
    {
        AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setUserName( username );
        authInfo.setPassword( password );
        authInfo.setPrivateKey( privateKey );
        authInfo.setPassphrase( passphrase );
        authenticationInfoMap.put( repositoryId, authInfo );
    }
    public void addPermissionInfo( String repositoryId,
                                   String filePermissions,
                                   String directoryPermissions )
    {
        RepositoryPermissions permissions = new RepositoryPermissions();
        boolean addPermissions = false;
        if ( filePermissions != null )
        {
            permissions.setFileMode( filePermissions );
            addPermissions = true;
        }
        if ( directoryPermissions != null )
        {
            permissions.setDirectoryMode( directoryPermissions );
            addPermissions = true;
        }
        if ( addPermissions )
        {
            serverPermissionsMap.put( repositoryId, permissions );
        }
    }
    public void addMirror( String id,
                           String mirrorOf,
                           String url )
    {
        if ( id == null )
        {
            id = "mirror-" + anonymousMirrorIdSeed++;
            getLogger().warn( "You are using a mirror that doesn't declare an <id/> element. Using \'" + id + "\' instead:\nId: " + id + "\nmirrorOf: " + mirrorOf + "\nurl: " + url + "\n" );
        }
        ArtifactRepository mirror = new DefaultArtifactRepository( id, url, null );
        if ( !mirrors.containsKey( mirrorOf ) )
        {
            mirrors.put( mirrorOf, mirror );
        }
    }
    public void setOnline( boolean online )
    {
        this.online = online;
    }
    public boolean isOnline()
    {
        return online;
    }
    public void setInteractive( boolean interactive )
    {
        this.interactive = interactive;
    }
    @SuppressWarnings( "unchecked" )
    public void registerWagons( Collection wagons,
                                PlexusContainer extensionContainer )
    {
        for ( Iterator<String> i = wagons.iterator(); i.hasNext(); )
        {
            availableWagons.put( i.next(), extensionContainer );
        }
    }
    private void configureWagon( Wagon wagon,
                                 ArtifactRepository repository )
        throws WagonConfigurationException
    {
        configureWagon( wagon, repository.getId(), repository.getProtocol() );
    }
    private void configureWagon( Wagon wagon, String repositoryId, String protocol )
        throws WagonConfigurationException
    {
        PlexusConfiguration config = (PlexusConfiguration) serverConfigurationMap.get( repositoryId );
        if ( protocol.startsWith( "http" ) || protocol.startsWith( "dav" ) )
        {
            config = updateUserAgentForHttp( wagon, config );
        }
        if ( config != null )
        {
            ComponentConfigurator componentConfigurator = null;
            try
            {
                componentConfigurator = (ComponentConfigurator) container.lookup( ComponentConfigurator.ROLE, "wagon" );
                componentConfigurator.configureComponent( wagon, config, container.getContainerRealm() );
            }
            catch ( final ComponentLookupException e )
            {
                throw new WagonConfigurationException( repositoryId,
                                                       "Unable to lookup wagon configurator. Wagon configuration cannot be applied.",
                                                       e );
            }
            catch ( ComponentConfigurationException e )
            {
                throw new WagonConfigurationException( repositoryId, "Unable to apply wagon configuration.", e );
            }
            finally
            {
                if ( componentConfigurator != null )
                {
                    try
                    {
                        container.release( componentConfigurator );
                    }
                    catch ( ComponentLifecycleException e )
                    {
                        getLogger().error( "Problem releasing configurator - ignoring: " + e.getMessage() );
                    }
                }
            }
        }
    }
    private PlexusConfiguration updateUserAgentForHttp( Wagon wagon, PlexusConfiguration config )
    {
        if ( config == null )
        {
            config = new XmlPlexusConfiguration( "configuration" );
        }
        if ( httpUserAgent != null )
        {
            try
            {
                wagon.getClass().getMethod( "setHttpHeaders", new Class[]{ Properties.class } );
                PlexusConfiguration headerConfig = config.getChild( "httpHeaders", true );
                PlexusConfiguration[] children = headerConfig.getChildren( "property" );
                boolean found = false;
                getLogger().debug( "Checking for pre-existing User-Agent configuration." );
                for ( int i = 0; i < children.length; i++ )
                {
                    PlexusConfiguration c = children[i].getChild( "name", false );
                    if ( c != null && "User-Agent".equals( c.getValue( null ) ) )
                    {
                        found = true;
                        break;
                    }
                }
                if ( !found )
                {
                    getLogger().debug( "Adding User-Agent configuration." );
                    XmlPlexusConfiguration propertyConfig = new XmlPlexusConfiguration( "property" );
                    headerConfig.addChild( propertyConfig );
                    XmlPlexusConfiguration nameConfig = new XmlPlexusConfiguration( "name" );
                    nameConfig.setValue( "User-Agent" );
                    propertyConfig.addChild( nameConfig );
                    XmlPlexusConfiguration versionConfig = new XmlPlexusConfiguration( "value" );
                    versionConfig.setValue( httpUserAgent );
                    propertyConfig.addChild( versionConfig );
                }
                else
                {
                    getLogger().debug( "User-Agent configuration found." );
                }
            }
            catch ( SecurityException e )
            {
                getLogger().debug( "setHttpHeaders method not accessible on wagon: " + wagon + "; skipping User-Agent configuration." );
            }
            catch ( NoSuchMethodException e )
            {
                getLogger().debug( "setHttpHeaders method not found on wagon: " + wagon + "; skipping User-Agent configuration." );
            }
        }
        return config;
    }
    public void addConfiguration( String repositoryId,
                                  Xpp3Dom configuration )
    {
        if ( repositoryId == null || configuration == null )
        {
            throw new IllegalArgumentException( "arguments can't be null" );
        }
        final XmlPlexusConfiguration xmlConf = new XmlPlexusConfiguration( configuration );
        for ( int i = 0; i < configuration.getChildCount(); i++ )
        {
            Xpp3Dom domChild = configuration.getChild( i );
            if ( WAGON_PROVIDER_CONFIGURATION.equals( domChild.getName() ) )
            {
                serverWagonProviderMap.put( repositoryId, domChild.getValue() );
                configuration.removeChild( i );
                break;
            }
            i++;
        }
        serverConfigurationMap.put( repositoryId, xmlConf );
    }
    public void setDefaultRepositoryPermissions( RepositoryPermissions defaultRepositoryPermissions )
    {
        this.defaultRepositoryPermissions = defaultRepositoryPermissions;
    }
    public void initialize()
        throws InitializationException
    {
        if ( httpUserAgent == null )
        {
            InputStream resourceAsStream = null;
            try
            {
                Properties properties = new Properties();
                resourceAsStream = getClass().getClassLoader().getResourceAsStream( MAVEN_ARTIFACT_PROPERTIES );
                if ( resourceAsStream != null )
                {
                    try
                    {
                        properties.load( resourceAsStream );
                        httpUserAgent =
                            "maven-artifact/" + properties.getProperty( "version" ) + " (Java "
                                + System.getProperty( "java.version" ) + "; " + System.getProperty( "os.name" ) + " "
                                + System.getProperty( "os.version" ) + ")";
                    }
                    catch ( IOException e )
                    {
                        getLogger().warn(
                                          "Failed to load Maven artifact properties from:\n" + MAVEN_ARTIFACT_PROPERTIES
                                              + "\n\nUser-Agent HTTP header may be incorrect for artifact resolution." );
                    }
                }
            }
            finally
            {
                IOUtil.close( resourceAsStream );
            }
        }
    }
    public void setHttpUserAgent( String userAgent )
    {
        this.httpUserAgent = userAgent;
    }
    public String getHttpUserAgent()
    {
        return httpUserAgent;
    }
}
