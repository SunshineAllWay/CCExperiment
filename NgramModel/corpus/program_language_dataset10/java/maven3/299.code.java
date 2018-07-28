package org.apache.maven.artifact.resolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryCache;
import org.apache.maven.artifact.repository.RepositoryRequest;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
public class ArtifactResolutionRequest
    implements RepositoryRequest
{
    private Artifact artifact;
    private Set<Artifact> artifactDependencies;
    private ArtifactRepository localRepository;
    private List<ArtifactRepository> remoteRepositories;
    private ArtifactFilter collectionFilter;
    private ArtifactFilter resolutionFilter;
    private List<ResolutionListener> listeners = new ArrayList<ResolutionListener>();
    private Map managedVersionMap;
    private boolean resolveRoot = true;
    private boolean resolveTransitively = false;
    private boolean offline;
    private boolean forceUpdate;
    private List<Server> servers;
    private List<Mirror> mirrors;
    private List<Proxy> proxies;
    public ArtifactResolutionRequest()
    {
    }
    public ArtifactResolutionRequest( RepositoryRequest request )
    {
        setLocalRepository( request.getLocalRepository() );
        setRemoteRepositories( request.getRemoteRepositories() );
        setOffline( request.isOffline() );
        setForceUpdate( request.isForceUpdate() );
    }
    public Artifact getArtifact()
    {
        return artifact;
    }
    public ArtifactResolutionRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
        return this;
    }
    public ArtifactResolutionRequest setArtifactDependencies( Set<Artifact> artifactDependencies )
    {
        this.artifactDependencies = artifactDependencies;
        return this;
    }
    public Set<Artifact> getArtifactDependencies()
    {
        return artifactDependencies;
    }
    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }
    public ArtifactResolutionRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
        return this;
    }
    public List<ArtifactRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }
    public ArtifactResolutionRequest setRemoteRepositories( List<ArtifactRepository> remoteRepositories )
    {
        this.remoteRepositories = remoteRepositories;
        return this;
    }
    public ArtifactFilter getCollectionFilter()
    {
        return collectionFilter;
    }
    public ArtifactResolutionRequest setCollectionFilter( ArtifactFilter filter )
    {
        this.collectionFilter = filter;
        return this;
    }
    public ArtifactFilter getResolutionFilter()
    {
        return resolutionFilter;
    }
    public ArtifactResolutionRequest setResolutionFilter( ArtifactFilter filter )
    {
        this.resolutionFilter = filter;
        return this;
    }
    public List<ResolutionListener> getListeners()
    {
        return listeners;
    }
    public ArtifactResolutionRequest setListeners( List<ResolutionListener> listeners )
    {        
        this.listeners = listeners;
        return this;
    }
    public ArtifactResolutionRequest addListener( ResolutionListener listener )
    {
        listeners.add( listener );
        return this;
    }
    public Map getManagedVersionMap()
    {
        return managedVersionMap;
    }
    public ArtifactResolutionRequest setManagedVersionMap( Map managedVersionMap )
    {
        this.managedVersionMap = managedVersionMap;
        return this;
    }
    public ArtifactResolutionRequest setResolveRoot( boolean resolveRoot )
    {
        this.resolveRoot = resolveRoot;
        return this;
    }
    public boolean isResolveRoot()
    {
        return resolveRoot;
    }        
    public ArtifactResolutionRequest setResolveTransitively( boolean resolveDependencies )
    {
        this.resolveTransitively = resolveDependencies;
        return this;
    }
    public boolean isResolveTransitively()
    {
        return resolveTransitively;
    }        
    public String toString()
    {
        StringBuilder sb = new StringBuilder()
                .append( "REQUEST: " ).append(  "\n" )
                .append( "artifact: " ).append( artifact ).append(  "\n" )
                .append( artifactDependencies ).append(  "\n" )
                .append( "localRepository: " ).append(  localRepository ).append(  "\n" )
                .append( "remoteRepositories: " ).append(  remoteRepositories ).append(  "\n" );
        return sb.toString();
    }
    public RepositoryCache getCache()
    {
        return null;
    }
    public ArtifactResolutionRequest setCache( RepositoryCache cache )
    {
        return this;
    }
    public boolean isOffline()
    {
        return offline;
    }
    public ArtifactResolutionRequest setOffline( boolean offline )
    {
        this.offline = offline;
        return this;
    }
    public boolean isForceUpdate()
    {
        return forceUpdate;
    }
    public ArtifactResolutionRequest setForceUpdate( boolean forceUpdate )
    {
        this.forceUpdate = forceUpdate;
        return this;
    }
    public ArtifactResolutionRequest setServers( List<Server> servers )
    {
        this.servers = servers;
        return this;
    }
    public List<Server> getServers()
    {
        if ( servers == null )
        {
            servers = new ArrayList<Server>();
        }
        return servers;
    }
    public ArtifactResolutionRequest setMirrors( List<Mirror> mirrors )
    {
        this.mirrors = mirrors;
        return this;
    }
    public List<Mirror> getMirrors()
    {
        if ( mirrors == null )
        {
            mirrors = new ArrayList<Mirror>();
        }
        return mirrors;
    }
    public ArtifactResolutionRequest setProxies( List<Proxy> proxies )
    {
        this.proxies = proxies;
        return this;
    }
    public List<Proxy> getProxies()
    {
        if ( proxies == null )
        {
            proxies = new ArrayList<Proxy>();
        }
        return proxies;
    }
}
