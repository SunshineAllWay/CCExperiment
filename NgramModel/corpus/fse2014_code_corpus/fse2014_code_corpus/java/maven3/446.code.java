package org.apache.maven.plugin.internal;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
class PluginDependencyResolutionListener
    implements ResolutionListener
{
    private ArtifactFilter coreFilter;
    private LinkedList<Artifact> coreArtifacts = new LinkedList<Artifact>();
    private Artifact wagonProvider;
    private Map<Artifact, Object> bannedArtifacts = new IdentityHashMap<Artifact, Object>();
    public PluginDependencyResolutionListener( ArtifactFilter coreFilter )
    {
        this.coreFilter = coreFilter;
    }
    public void removeBannedDependencies( Collection<Artifact> artifacts )
    {
        if ( !bannedArtifacts.isEmpty() && artifacts != null )
        {
            for ( Iterator<Artifact> it = artifacts.iterator(); it.hasNext(); )
            {
                Artifact artifact = it.next();
                if ( bannedArtifacts.containsKey( artifact ) )
                {
                    it.remove();
                }
            }
        }
    }
    public void startProcessChildren( Artifact artifact )
    {
        if ( wagonProvider == null )
        {
            if ( isLegacyCoreArtifact( artifact ) )
            {
                coreArtifacts.addFirst( artifact );
            }
            else if ( !coreArtifacts.isEmpty() && isWagonProvider( artifact ) )
            {
                wagonProvider = artifact;
                bannedArtifacts.put( artifact, null );
            }
        }
    }
    private boolean isLegacyCoreArtifact( Artifact artifact )
    {
        String version = artifact.getVersion();
        return version != null && version.startsWith( "2." ) && !coreFilter.include( artifact );
    }
    public void endProcessChildren( Artifact artifact )
    {
        if ( wagonProvider == artifact )
        {
            wagonProvider = null;
        }
        else if ( coreArtifacts.peek() == artifact )
        {
            coreArtifacts.removeFirst();
        }
    }
    public void includeArtifact( Artifact artifact )
    {
        if ( wagonProvider != null )
        {
            bannedArtifacts.put( artifact, null );
        }
    }
    private boolean isWagonProvider( Artifact artifact )
    {
        if ( "org.apache.maven.wagon".equals( artifact.getGroupId() ) )
        {
            return artifact.getArtifactId().startsWith( "wagon-" );
        }
        return false;
    }
    public void manageArtifact( Artifact artifact, Artifact replacement )
    {
    }
    public void omitForCycle( Artifact artifact )
    {
    }
    public void omitForNearer( Artifact omitted, Artifact kept )
    {
    }
    public void restrictRange( Artifact artifact, Artifact replacement, VersionRange newRange )
    {
    }
    public void selectVersionFromRange( Artifact artifact )
    {
    }
    public void testArtifact( Artifact node )
    {
    }
    public void updateScope( Artifact artifact, String scope )
    {
    }
    public void updateScopeCurrentPom( Artifact artifact, String ignoredScope )
    {
    }
}
