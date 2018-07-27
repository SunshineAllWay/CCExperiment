package org.apache.maven.repository.internal;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.ConfigurationProperties;
import org.sonatype.aether.RepositoryCache;
import org.sonatype.aether.RepositoryEvent.EventType;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.util.artifact.SubArtifact;
import org.sonatype.aether.util.listener.DefaultRepositoryEvent;
import org.sonatype.aether.util.metadata.DefaultMetadata;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.MetadataResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.CacheUtils;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.ArtifactRepository;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.resolution.VersionResult;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;
@Component( role = VersionResolver.class )
public class DefaultVersionResolver
    implements VersionResolver, Service
{
    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";
    private static final String RELEASE = "RELEASE";
    private static final String LATEST = "LATEST";
    private static final String SNAPSHOT = "SNAPSHOT";
    @Requirement
    private Logger logger = NullLogger.INSTANCE;
    @Requirement
    private MetadataResolver metadataResolver;
    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
        setMetadataResolver( locator.getService( MetadataResolver.class ) );
    }
    public DefaultVersionResolver setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }
    public DefaultVersionResolver setMetadataResolver( MetadataResolver metadataResolver )
    {
        if ( metadataResolver == null )
        {
            throw new IllegalArgumentException( "metadata resolver has not been specified" );
        }
        this.metadataResolver = metadataResolver;
        return this;
    }
    public VersionResult resolveVersion( RepositorySystemSession session, VersionRequest request )
        throws VersionResolutionException
    {
        Artifact artifact = request.getArtifact();
        String version = artifact.getVersion();
        VersionResult result = new VersionResult( request );
        Key cacheKey = null;
        RepositoryCache cache = session.getCache();
        if ( cache != null && !ConfigurationProperties.get( session, "aether.versionResolver.noCache", false ) )
        {
            cacheKey = new Key( session, request );
            Object obj = cache.get( session, cacheKey );
            if ( obj instanceof Record )
            {
                Record record = (Record) obj;
                result.setVersion( record.version );
                result.setRepository( CacheUtils.getRepository( session, request.getRepositories(), record.repoClass,
                                                                record.repoId ) );
                return result;
            }
        }
        Metadata metadata;
        if ( RELEASE.equals( version ) )
        {
            metadata =
                new DefaultMetadata( artifact.getGroupId(), artifact.getArtifactId(), MAVEN_METADATA_XML,
                                     Metadata.Nature.RELEASE );
        }
        else if ( LATEST.equals( version ) )
        {
            metadata =
                new DefaultMetadata( artifact.getGroupId(), artifact.getArtifactId(), MAVEN_METADATA_XML,
                                     Metadata.Nature.RELEASE_OR_SNAPSHOT );
        }
        else if ( version.endsWith( SNAPSHOT ) )
        {
            WorkspaceReader workspace = session.getWorkspaceReader();
            if ( workspace != null && workspace.findVersions( artifact ).contains( version ) )
            {
                metadata = null;
                result.setRepository( workspace.getRepository() );
            }
            else
            {
                metadata =
                    new DefaultMetadata( artifact.getGroupId(), artifact.getArtifactId(), version, MAVEN_METADATA_XML,
                                         Metadata.Nature.SNAPSHOT );
            }
        }
        else
        {
            metadata = null;
        }
        if ( metadata == null )
        {
            result.setVersion( version );
        }
        else
        {
            List<MetadataRequest> metadataRequests = new ArrayList<MetadataRequest>( request.getRepositories().size() );
            metadataRequests.add( new MetadataRequest( metadata, null, request.getRequestContext() ) );
            for ( RemoteRepository repository : request.getRepositories() )
            {
                MetadataRequest metadataRequest =
                    new MetadataRequest( metadata, repository, request.getRequestContext() );
                metadataRequest.setDeleteLocalCopyIfMissing( true );
                metadataRequest.setFavorLocalRepository( true );
                metadataRequests.add( metadataRequest );
            }
            List<MetadataResult> metadataResults = metadataResolver.resolveMetadata( session, metadataRequests );
            Map<String, VersionInfo> infos = new HashMap<String, VersionInfo>();
            for ( MetadataResult metadataResult : metadataResults )
            {
                result.addException( metadataResult.getException() );
                ArtifactRepository repository = metadataResult.getRequest().getRepository();
                if ( repository == null )
                {
                    repository = session.getLocalRepository();
                }
                Versioning versioning = readVersions( session, metadataResult.getMetadata(), repository, result );
                merge( artifact, infos, versioning, repository );
            }
            if ( RELEASE.equals( version ) )
            {
                resolve( result, infos, RELEASE );
            }
            else if ( LATEST.equals( version ) )
            {
                if ( !resolve( result, infos, LATEST ) )
                {
                    resolve( result, infos, RELEASE );
                }
                if ( result.getVersion() != null && result.getVersion().endsWith( SNAPSHOT ) )
                {
                    VersionRequest subRequest = new VersionRequest();
                    subRequest.setArtifact( artifact.setVersion( result.getVersion() ) );
                    if ( result.getRepository() instanceof RemoteRepository )
                    {
                        subRequest.setRepositories( Collections.singletonList( (RemoteRepository) result.getRepository() ) );
                    }
                    else
                    {
                        subRequest.setRepositories( request.getRepositories() );
                    }
                    VersionResult subResult = resolveVersion( session, subRequest );
                    result.setVersion( subResult.getVersion() );
                    result.setRepository( subResult.getRepository() );
                    for ( Exception exception : subResult.getExceptions() )
                    {
                        result.addException( exception );
                    }
                }
            }
            else
            {
                String key = SNAPSHOT + getKey( artifact.getClassifier(), artifact.getExtension() );
                merge( infos, SNAPSHOT, key );
                if ( !resolve( result, infos, key ) )
                {
                    result.setVersion( version );
                }
            }
            if ( StringUtils.isEmpty( result.getVersion() ) )
            {
                throw new VersionResolutionException( result );
            }
        }
        if ( cacheKey != null && metadata != null && isSafelyCacheable( session, artifact ) )
        {
            cache.put( session, cacheKey, new Record( result.getVersion(), result.getRepository() ) );
        }
        return result;
    }
    private boolean resolve( VersionResult result, Map<String, VersionInfo> infos, String key )
    {
        VersionInfo info = infos.get( key );
        if ( info != null )
        {
            result.setVersion( info.version );
            result.setRepository( info.repository );
        }
        return info != null;
    }
    private Versioning readVersions( RepositorySystemSession session, Metadata metadata, ArtifactRepository repository,
                                     VersionResult result )
    {
        Versioning versioning = null;
        FileInputStream fis = null;
        try
        {
            if ( metadata != null && metadata.getFile() != null && metadata.getFile().exists() )
            {
                fis = new FileInputStream( metadata.getFile() );
                org.apache.maven.artifact.repository.metadata.Metadata m = new MetadataXpp3Reader().read( fis, false );
                versioning = m.getVersioning();
                if ( versioning != null && repository instanceof LocalRepository )
                {
                    if ( versioning.getSnapshot() != null && versioning.getSnapshot().getBuildNumber() > 0 )
                    {
                        Versioning repaired = new Versioning();
                        repaired.setLastUpdated( versioning.getLastUpdated() );
                        Snapshot snapshot = new Snapshot();
                        snapshot.setLocalCopy( true );
                        repaired.setSnapshot( snapshot );
                        versioning = repaired;
                        throw new IOException( "Snapshot information corrupted with remote repository data"
                            + ", please verify that no remote repository uses the id '" + repository.getId() + "'" );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            invalidMetadata( session, metadata, repository, e );
            result.addException( e );
        }
        finally
        {
            IOUtil.close( fis );
        }
        return ( versioning != null ) ? versioning : new Versioning();
    }
    private void invalidMetadata( RepositorySystemSession session, Metadata metadata, ArtifactRepository repository,
                                  Exception exception )
    {
        RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            DefaultRepositoryEvent event = new DefaultRepositoryEvent( EventType.METADATA_INVALID, session );
            event.setMetadata( metadata );
            event.setException( exception );
            event.setRepository( repository );
            listener.metadataInvalid( event );
        }
    }
    private void merge( Artifact artifact, Map<String, VersionInfo> infos, Versioning versioning,
                        ArtifactRepository repository )
    {
        if ( StringUtils.isNotEmpty( versioning.getRelease() ) )
        {
            merge( RELEASE, infos, versioning.getLastUpdated(), versioning.getRelease(), repository );
        }
        if ( StringUtils.isNotEmpty( versioning.getLatest() ) )
        {
            merge( LATEST, infos, versioning.getLastUpdated(), versioning.getLatest(), repository );
        }
        for ( SnapshotVersion sv : versioning.getSnapshotVersions() )
        {
            if ( StringUtils.isNotEmpty( sv.getVersion() ) )
            {
                String key = getKey( sv.getClassifier(), sv.getExtension() );
                merge( SNAPSHOT + key, infos, sv.getUpdated(), sv.getVersion(), repository );
            }
        }
        Snapshot snapshot = versioning.getSnapshot();
        if ( snapshot != null && versioning.getSnapshotVersions().isEmpty() )
        {
            String version = artifact.getVersion();
            if ( snapshot.getTimestamp() != null && snapshot.getBuildNumber() > 0 )
            {
                String qualifier = snapshot.getTimestamp() + '-' + snapshot.getBuildNumber();
                version = version.substring( 0, version.length() - SNAPSHOT.length() ) + qualifier;
            }
            merge( SNAPSHOT, infos, versioning.getLastUpdated(), version, repository );
        }
    }
    private void merge( String key, Map<String, VersionInfo> infos, String timestamp, String version,
                        ArtifactRepository repository )
    {
        VersionInfo info = infos.get( key );
        if ( info == null )
        {
            info = new VersionInfo( timestamp, version, repository );
            infos.put( key, info );
        }
        else if ( info.isOutdated( timestamp ) )
        {
            info.version = version;
            info.repository = repository;
        }
    }
    private void merge( Map<String, VersionInfo> infos, String srcKey, String dstKey )
    {
        VersionInfo srcInfo = infos.get( srcKey );
        VersionInfo dstInfo = infos.get( dstKey );
        if ( dstInfo == null
            || ( srcInfo != null && dstInfo.isOutdated( srcInfo.timestamp ) && srcInfo.repository != dstInfo.repository ) )
        {
            infos.put( dstKey, srcInfo );
        }
    }
    private String getKey( String classifier, String extension )
    {
        return StringUtils.clean( classifier ) + ':' + StringUtils.clean( extension );
    }
    private boolean isSafelyCacheable( RepositorySystemSession session, Artifact artifact )
    {
        WorkspaceReader workspace = session.getWorkspaceReader();
        if ( workspace == null )
        {
            return true;
        }
        Artifact pomArtifact = artifact;
        if ( pomArtifact.getClassifier().length() > 0 || !"pom".equals( pomArtifact.getExtension() ) )
        {
            pomArtifact = new SubArtifact( artifact, "", "pom" );
        }
        return workspace.findArtifact( pomArtifact ) == null;
    }
    private static class VersionInfo
    {
        String timestamp;
        String version;
        ArtifactRepository repository;
        public VersionInfo( String timestamp, String version, ArtifactRepository repository )
        {
            this.timestamp = ( timestamp != null ) ? timestamp : "";
            this.version = version;
            this.repository = repository;
        }
        public boolean isOutdated( String timestamp )
        {
            return timestamp != null && timestamp.compareTo( this.timestamp ) > 0;
        }
    }
    private static class Key
    {
        private final String groupId;
        private final String artifactId;
        private final String classifier;
        private final String extension;
        private final String version;
        private final String context;
        private final File localRepo;
        private final WorkspaceRepository workspace;
        private final List<RemoteRepository> repositories;
        private final int hashCode;
        public Key( RepositorySystemSession session, VersionRequest request )
        {
            Artifact artifact = request.getArtifact();
            groupId = artifact.getGroupId();
            artifactId = artifact.getArtifactId();
            classifier = artifact.getClassifier();
            extension = artifact.getExtension();
            version = artifact.getVersion();
            localRepo = session.getLocalRepository().getBasedir();
            workspace = CacheUtils.getWorkspace( session );
            repositories = new ArrayList<RemoteRepository>( request.getRepositories().size() );
            boolean repoMan = false;
            for ( RemoteRepository repository : request.getRepositories() )
            {
                if ( repository.isRepositoryManager() )
                {
                    repoMan = true;
                    repositories.addAll( repository.getMirroredRepositories() );
                }
                else
                {
                    repositories.add( repository );
                }
            }
            context = repoMan ? request.getRequestContext() : "";
            int hash = 17;
            hash = hash * 31 + groupId.hashCode();
            hash = hash * 31 + artifactId.hashCode();
            hash = hash * 31 + classifier.hashCode();
            hash = hash * 31 + extension.hashCode();
            hash = hash * 31 + version.hashCode();
            hash = hash * 31 + localRepo.hashCode();
            hash = hash * 31 + CacheUtils.repositoriesHashCode( repositories );
            hashCode = hash;
        }
        @Override
        public boolean equals( Object obj )
        {
            if ( obj == this )
            {
                return true;
            }
            else if ( obj == null || !getClass().equals( obj.getClass() ) )
            {
                return false;
            }
            Key that = (Key) obj;
            return artifactId.equals( that.artifactId ) && groupId.equals( that.groupId )
                && classifier.equals( that.classifier ) && extension.equals( that.extension )
                && version.equals( that.version ) && context.equals( that.context )
                && localRepo.equals( that.localRepo ) && CacheUtils.eq( workspace, that.workspace )
                && CacheUtils.repositoriesEquals( repositories, that.repositories );
        }
        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }
    private static class Record
    {
        final String version;
        final String repoId;
        final Class<?> repoClass;
        public Record( String version, ArtifactRepository repository )
        {
            this.version = version;
            if ( repository != null )
            {
                repoId = repository.getId();
                repoClass = repository.getClass();
            }
            else
            {
                repoId = null;
                repoClass = null;
            }
        }
    }
}
