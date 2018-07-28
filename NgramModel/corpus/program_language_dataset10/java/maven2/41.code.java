package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.ManagedVersionMap;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class DefaultArtifactCollector
    implements ArtifactCollector
{
    public ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact,
                                             ArtifactRepository localRepository,
                                             List<ArtifactRepository> remoteRepositories,
                                             ArtifactMetadataSource source, ArtifactFilter filter,
                                             List<ResolutionListener> listeners )
        throws ArtifactResolutionException
    {
        return collect( artifacts, originatingArtifact, Collections.EMPTY_MAP, localRepository, remoteRepositories,
                        source, filter, listeners );
    }
    public ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact,
                                             Map managedVersions, ArtifactRepository localRepository,
                                             List<ArtifactRepository> remoteRepositories,
                                             ArtifactMetadataSource source, ArtifactFilter filter,
                                             List<ResolutionListener> listeners )
        throws ArtifactResolutionException
    {
        Map<Object, List<ResolutionNode>> resolvedArtifacts = new LinkedHashMap<Object, List<ResolutionNode>>();
        ResolutionNode root = new ResolutionNode( originatingArtifact, remoteRepositories );
        root.addDependencies( artifacts, remoteRepositories, filter );
        ManagedVersionMap versionMap = getManagedVersionsMap( originatingArtifact, managedVersions );
        recurse( originatingArtifact, root, resolvedArtifacts, versionMap, localRepository, remoteRepositories, source,
                 filter, listeners );
        Set<ResolutionNode> set = new LinkedHashSet<ResolutionNode>();
        for ( List<ResolutionNode> nodes : resolvedArtifacts.values() )
        {
            for ( ResolutionNode node : nodes )
            {
                if ( !node.equals( root ) && node.isActive() )
                {
                    Artifact artifact = node.getArtifact();
                    if ( node.filterTrail( filter ) )
                    {
                        if ( node.isChildOfRootNode() || !artifact.isOptional() )
                        {
                            artifact.setDependencyTrail( node.getDependencyTrail() );
                            set.add( node );
                        }
                    }
                }
            }
        }
        ArtifactResolutionResult result = new ArtifactResolutionResult();
        result.setArtifactResolutionNodes( set );
        return result;
    }
    private ManagedVersionMap getManagedVersionsMap( Artifact originatingArtifact, Map managedVersions )
    {
        ManagedVersionMap versionMap;
        if ( managedVersions != null && managedVersions instanceof ManagedVersionMap )
        {
            versionMap = (ManagedVersionMap) managedVersions;
        }
        else
        {
            versionMap = new ManagedVersionMap( managedVersions );
        }
        Artifact managedOriginatingArtifact = (Artifact) versionMap.get( originatingArtifact.getDependencyConflictId() );
        if ( managedOriginatingArtifact != null )
        {
            if ( managedVersions instanceof ManagedVersionMap )
            {
                versionMap = new ManagedVersionMap( managedVersions );
            }
            versionMap.remove( originatingArtifact.getDependencyConflictId() );
        }
        return versionMap;
    }
    private void recurse( Artifact originatingArtifact, ResolutionNode node,
                          Map<Object, List<ResolutionNode>> resolvedArtifacts, ManagedVersionMap managedVersions,
                          ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                          ArtifactMetadataSource source, ArtifactFilter filter, List<ResolutionListener> listeners )
        throws CyclicDependencyException, ArtifactResolutionException, OverConstrainedVersionException
    {
        fireEvent( ResolutionListener.TEST_ARTIFACT, listeners, node );
        Object key = node.getKey();
        if ( managedVersions.containsKey( key ) )
        {
            manageArtifact( node, managedVersions, listeners );
        }
        List<ResolutionNode> previousNodes = resolvedArtifacts.get( key );
        if ( previousNodes != null )
        {
            for ( ResolutionNode previous : previousNodes )
            {
                if ( previous.isActive() )
                {
                    VersionRange previousRange = previous.getArtifact().getVersionRange();
                    VersionRange currentRange = node.getArtifact().getVersionRange();
                    if ( previousRange != null && currentRange != null )
                    {
                        VersionRange newRange = previousRange.restrict( currentRange );
                        if ( newRange.isSelectedVersionKnown( previous.getArtifact() ) )
                        {
                            fireEvent( ResolutionListener.RESTRICT_RANGE, listeners, node, previous.getArtifact(),
                                       newRange );
                        }
                        previous.getArtifact().setVersionRange( newRange );
                        node.getArtifact().setVersionRange( currentRange.restrict( previousRange ) );
                        ResolutionNode[] resetNodes = {previous, node};
                        for ( int j = 0; j < 2; j++ )
                        {
                            Artifact resetArtifact = resetNodes[j].getArtifact();
                            if ( resetArtifact.getVersion() == null && resetArtifact.getVersionRange() != null )
                            {
                                List<ArtifactVersion> versions = resetArtifact.getAvailableVersions();
                                if ( versions == null )
                                {
                                    try
                                    {
                                        versions =
                                            source.retrieveAvailableVersions( resetArtifact, localRepository,
                                                                              remoteRepositories );
                                        resetArtifact.setAvailableVersions( versions );
                                    }
                                    catch ( ArtifactMetadataRetrievalException e )
                                    {
                                        resetArtifact.setDependencyTrail( node.getDependencyTrail() );
                                        throw new ArtifactResolutionException( "Unable to get dependency information: "
                                            + e.getMessage(), resetArtifact, remoteRepositories, e );
                                    }
                                }
                                ArtifactVersion selectedVersion =
                                    resetArtifact.getVersionRange().matchVersion( resetArtifact.getAvailableVersions() );
                                if ( selectedVersion != null )
                                {
                                    resetArtifact.selectVersion( selectedVersion.toString() );
                                }
                                else
                                {
                                    throw new OverConstrainedVersionException( " Unable to find a version in "
                                        + resetArtifact.getAvailableVersions() + " to match the range "
                                        + resetArtifact.getVersionRange(), resetArtifact );
                                }
                                fireEvent( ResolutionListener.SELECT_VERSION_FROM_RANGE, listeners, resetNodes[j] );
                            }
                        }
                    }
                    ResolutionNode nearest;
                    ResolutionNode farthest;
                    if ( previous.getDepth() <= node.getDepth() )
                    {
                        nearest = previous;
                        farthest = node;
                    }
                    else
                    {
                        nearest = node;
                        farthest = previous;
                    }
                    if ( checkScopeUpdate( farthest, nearest, listeners ) )
                    {
                        nearest.disable();
                        farthest.getArtifact().setVersion( nearest.getArtifact().getVersion() );
                        fireEvent( ResolutionListener.OMIT_FOR_NEARER, listeners, nearest, farthest.getArtifact() );
                    }
                    else
                    {
                        farthest.disable();
                        fireEvent( ResolutionListener.OMIT_FOR_NEARER, listeners, farthest, nearest.getArtifact() );
                    }
                }
            }
        }
        else
        {
            previousNodes = new ArrayList<ResolutionNode>();
            resolvedArtifacts.put( key, previousNodes );
        }
        previousNodes.add( node );
        if ( node.isActive() )
        {
            fireEvent( ResolutionListener.INCLUDE_ARTIFACT, listeners, node );
        }
        if ( node.isActive() && !Artifact.SCOPE_SYSTEM.equals( node.getArtifact().getScope() ) )
        {
            fireEvent( ResolutionListener.PROCESS_CHILDREN, listeners, node );
            Artifact parentArtifact = node.getArtifact();
            for ( Iterator<ResolutionNode> i = node.getChildrenIterator(); i.hasNext(); )
            {
                ResolutionNode child = i.next();
                if ( !child.isResolved() && ( !child.getArtifact().isOptional() || child.isChildOfRootNode() ) )
                {
                    Artifact artifact = child.getArtifact();
                    artifact.setDependencyTrail( node.getDependencyTrail() );
                    List<ArtifactRepository> childRemoteRepositories = child.getRemoteRepositories();
                    try
                    {
                        Object childKey;
                        do
                        {
                            childKey = child.getKey();
                            if ( managedVersions.containsKey( childKey ) )
                            {
                                manageArtifact( child, managedVersions, listeners );
                                Artifact ma = (Artifact) managedVersions.get( childKey );
                                ArtifactFilter managedExclusionFilter = ma.getDependencyFilter();
                                if ( null != managedExclusionFilter )
                                {
                                    if ( null != artifact.getDependencyFilter() )
                                    {
                                        AndArtifactFilter aaf = new AndArtifactFilter();
                                        aaf.add( artifact.getDependencyFilter() );
                                        aaf.add( managedExclusionFilter );
                                        artifact.setDependencyFilter( aaf );
                                    }
                                    else
                                    {
                                        artifact.setDependencyFilter( managedExclusionFilter );
                                    }
                                }
                            }
                            if ( artifact.getVersion() == null )
                            {
                                ArtifactVersion version;
                                if ( artifact.isSelectedVersionKnown() )
                                {
                                    version = artifact.getSelectedVersion();
                                }
                                else
                                {
                                    List<ArtifactVersion> versions = artifact.getAvailableVersions();
                                    if ( versions == null )
                                    {
                                        versions = source.retrieveAvailableVersions( artifact, localRepository,
                                                                                     childRemoteRepositories );
                                        artifact.setAvailableVersions( versions );
                                    }
                                    Collections.sort( versions );
                                    VersionRange versionRange = artifact.getVersionRange();
                                    version = versionRange.matchVersion( versions );
                                    if ( version == null )
                                    {
                                        if ( versions.isEmpty() )
                                        {
                                            throw new OverConstrainedVersionException(
                                                "No versions are present in the repository for the artifact with a range "
                                                    + versionRange, artifact, childRemoteRepositories );
                                        }
                                        throw new OverConstrainedVersionException( "Couldn't find a version in "
                                            + versions + " to match range " + versionRange, artifact,
                                            childRemoteRepositories );
                                    }
                                }
                                artifact.selectVersion( version.toString() );
                                fireEvent( ResolutionListener.SELECT_VERSION_FROM_RANGE, listeners, child );
                            }
                            Artifact relocated = source.retrieveRelocatedArtifact( artifact, localRepository, childRemoteRepositories );
                            if ( relocated != null && !artifact.equals( relocated ) )
                            {
                                relocated.setDependencyFilter( artifact.getDependencyFilter() );
                                artifact = relocated;
                                child.setArtifact( artifact );
                            }
                        }
                        while ( !childKey.equals( child.getKey() ) );
                        if ( parentArtifact != null && parentArtifact.getDependencyFilter() != null
                            && !parentArtifact.getDependencyFilter().include( artifact ) )
                        {
                            continue;
                        }
                        ResolutionGroup rGroup = source.retrieve( artifact, localRepository, childRemoteRepositories );
                        if ( rGroup == null )
                        {
                            continue;
                        }
                        child.addDependencies( rGroup.getArtifacts(), rGroup.getResolutionRepositories(), filter );
                    }
                    catch ( CyclicDependencyException e )
                    {
                        fireEvent( ResolutionListener.OMIT_FOR_CYCLE, listeners,
                                   new ResolutionNode( e.getArtifact(), childRemoteRepositories, child ) );
                    }
                    catch ( ArtifactMetadataRetrievalException e )
                    {
                        artifact.setDependencyTrail( node.getDependencyTrail() );
                        throw new ArtifactResolutionException(
                            "Unable to get dependency information: " + e.getMessage(), artifact, childRemoteRepositories,
                            e );
                    }
                    recurse( originatingArtifact, child, resolvedArtifacts, managedVersions, localRepository, childRemoteRepositories, source,
                             filter, listeners );
                }
            }
            fireEvent( ResolutionListener.FINISH_PROCESSING_CHILDREN, listeners, node );
        }
    }
    private void manageArtifact( ResolutionNode node, ManagedVersionMap managedVersions,
                                 List<ResolutionListener> listeners )
    {
        Artifact artifact = (Artifact) managedVersions.get( node.getKey() );
        if ( artifact.getVersion() != null
                        && ( node.isChildOfRootNode() ? node.getArtifact().getVersion() == null : true ) )
        {
            fireEvent( ResolutionListener.MANAGE_ARTIFACT_VERSION, listeners, node, artifact );
            node.getArtifact().setVersion( artifact.getVersion() );
        }
        if ( artifact.getScope() != null
                        && ( node.isChildOfRootNode() ? node.getArtifact().getScope() == null : true ) )
        {
            fireEvent( ResolutionListener.MANAGE_ARTIFACT_SCOPE, listeners, node, artifact );
            node.getArtifact().setScope( artifact.getScope() );
        }
    }
    boolean checkScopeUpdate( ResolutionNode farthest, ResolutionNode nearest, List<ResolutionListener> listeners )
    {
        boolean updateScope = false;
        Artifact farthestArtifact = farthest.getArtifact();
        Artifact nearestArtifact = nearest.getArtifact();
        if ( Artifact.SCOPE_RUNTIME.equals( farthestArtifact.getScope() )
            && ( Artifact.SCOPE_TEST.equals( nearestArtifact.getScope() ) || Artifact.SCOPE_PROVIDED.equals( nearestArtifact.getScope() ) ) )
        {
            updateScope = true;
        }
        if ( Artifact.SCOPE_COMPILE.equals( farthestArtifact.getScope() )
            && !Artifact.SCOPE_COMPILE.equals( nearestArtifact.getScope() ) )
        {
            updateScope = true;
        }
        if ( nearest.getDepth() < 2 && updateScope )
        {
            updateScope = false;
            fireEvent( ResolutionListener.UPDATE_SCOPE_CURRENT_POM, listeners, nearest, farthestArtifact );
        }
        if ( updateScope )
        {
            fireEvent( ResolutionListener.UPDATE_SCOPE, listeners, nearest, farthestArtifact );
            nearestArtifact.setScope( farthestArtifact.getScope() );
        }
        return updateScope;
    }
    private void fireEvent( int event, List<ResolutionListener> listeners, ResolutionNode node )
    {
        fireEvent( event, listeners, node, null );
    }
    private void fireEvent( int event, List<ResolutionListener> listeners, ResolutionNode node, Artifact replacement )
    {
        fireEvent( event, listeners, node, replacement, null );
    }
    private void fireEvent( int event, List<ResolutionListener> listeners, ResolutionNode node, Artifact replacement,
                            VersionRange newRange )
    {
        for ( ResolutionListener listener : listeners )
        {
            switch ( event )
            {
                case ResolutionListener.TEST_ARTIFACT:
                    listener.testArtifact( node.getArtifact() );
                    break;
                case ResolutionListener.PROCESS_CHILDREN:
                    listener.startProcessChildren( node.getArtifact() );
                    break;
                case ResolutionListener.FINISH_PROCESSING_CHILDREN:
                    listener.endProcessChildren( node.getArtifact() );
                    break;
                case ResolutionListener.INCLUDE_ARTIFACT:
                    listener.includeArtifact( node.getArtifact() );
                    break;
                case ResolutionListener.OMIT_FOR_NEARER:
                    listener.omitForNearer( node.getArtifact(), replacement );
                    break;
                case ResolutionListener.OMIT_FOR_CYCLE:
                    listener.omitForCycle( node.getArtifact() );
                    break;
                case ResolutionListener.UPDATE_SCOPE:
                    listener.updateScope( node.getArtifact(), replacement.getScope() );
                    break;
                case ResolutionListener.UPDATE_SCOPE_CURRENT_POM:
                    listener.updateScopeCurrentPom( node.getArtifact(), replacement.getScope() );
                    break;
                case ResolutionListener.MANAGE_ARTIFACT_VERSION:
                    if ( listener instanceof ResolutionListenerForDepMgmt )
                    {
                        ResolutionListenerForDepMgmt asImpl = (ResolutionListenerForDepMgmt) listener;
                        asImpl.manageArtifactVersion( node.getArtifact(), replacement );
                    }
                    else
                    {
                        listener.manageArtifact( node.getArtifact(), replacement );
                    }
                    break;
                case ResolutionListener.MANAGE_ARTIFACT_SCOPE:
                    if ( listener instanceof ResolutionListenerForDepMgmt )
                    {
                        ResolutionListenerForDepMgmt asImpl = (ResolutionListenerForDepMgmt) listener;
                        asImpl.manageArtifactScope( node.getArtifact(), replacement );
                    }
                    else
                    {
                        listener.manageArtifact( node.getArtifact(), replacement );
                    }
                    break;
                case ResolutionListener.SELECT_VERSION_FROM_RANGE:
                    listener.selectVersionFromRange( node.getArtifact() );
                    break;
                case ResolutionListener.RESTRICT_RANGE:
                    if ( node.getArtifact().getVersionRange().hasRestrictions()
                        || replacement.getVersionRange().hasRestrictions() )
                    {
                        listener.restrictRange( node.getArtifact(), replacement, newRange );
                    }
                    break;
                default:
                    throw new IllegalStateException( "Unknown event: " + event );
            }
        }
    }
}
