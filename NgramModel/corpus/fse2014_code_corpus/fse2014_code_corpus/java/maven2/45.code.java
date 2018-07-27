package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
public class ResolutionNode
{
    private Artifact artifact;
    private List<ResolutionNode> children;
    private final List<Object> parents;
    private final int depth;
    private final ResolutionNode parent;
    private final List<ArtifactRepository> remoteRepositories;
    private boolean active = true;
    private List<Artifact> trail;
    public ResolutionNode( Artifact artifact, List<ArtifactRepository> remoteRepositories )
    {
        this.artifact = artifact;
        this.remoteRepositories = remoteRepositories;
        depth = 0;
        parents = Collections.emptyList();
        parent = null;
    }
    public ResolutionNode( Artifact artifact, List<ArtifactRepository> remoteRepositories, ResolutionNode parent )
    {
        this.artifact = artifact;
        this.remoteRepositories = remoteRepositories;
        depth = parent.depth + 1;
        parents = new ArrayList<Object>();
        parents.addAll( parent.parents );
        parents.add( parent.getKey() );
        this.parent = parent;
    }
    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }
    public Artifact getArtifact()
    {
        return artifact;
    }
    public Object getKey()
    {
        return artifact.getDependencyConflictId();
    }
    public void addDependencies( Set<Artifact> artifacts, List<ArtifactRepository> remoteRepositories,
                                 ArtifactFilter filter )
        throws CyclicDependencyException, OverConstrainedVersionException
    {
        if ( !artifacts.isEmpty() )
        {
            children = new ArrayList<ResolutionNode>( artifacts.size() );
            for ( Artifact a : artifacts )
            {
                if ( parents.contains( a.getDependencyConflictId() ) )
                {
                    a.setDependencyTrail( getDependencyTrail() );
                    throw new CyclicDependencyException( "A dependency has introduced a cycle", a );
                }
                children.add( new ResolutionNode( a, remoteRepositories, this ) );
            }
        }
        else
        {
            children = Collections.emptyList();
        }
        trail = null;
    }
    public List<String> getDependencyTrail()
        throws OverConstrainedVersionException
    {
        List<Artifact> trial = getTrail();
        List<String> ret = new ArrayList<String>( trial.size() );
        for ( Artifact artifact : trial )
        {
            ret.add( artifact.getId() );
        }
        return ret;
    }
    private List<Artifact> getTrail()
        throws OverConstrainedVersionException
    {
        if ( trail == null )
        {
            List<Artifact> ids = new LinkedList<Artifact>();
            ResolutionNode node = this;
            while ( node != null )
            {
                Artifact artifact = node.getArtifact();
                if ( artifact.getVersion() == null )
                {
                    ArtifactVersion selected = artifact.getSelectedVersion();
                    if ( selected != null )
                    {
                        artifact.selectVersion( selected.toString() );
                    }
                    else
                    {
                        throw new OverConstrainedVersionException( "Unable to get a selected Version for "
                            + artifact.getArtifactId(), artifact );
                    }
                }
                ids.add( 0, artifact );
                node = node.parent;
            }
            trail = ids;
        }
        return trail;
    }
    public boolean isResolved()
    {
        return children != null;
    }
    public boolean isChildOfRootNode()
    {
        return parent != null && parent.parent == null;
    }
    public Iterator<ResolutionNode> getChildrenIterator()
    {
        return children.iterator();
    }
    public int getDepth()
    {
        return depth;
    }
    public List<ArtifactRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }
    public boolean isActive()
    {
        return active;
    }
    public void enable()
    {
        active = true;
        if ( children != null )
        {
            for ( ResolutionNode node : children )
            {
                node.enable();
            }
        }
    }
    public void disable()
    {
        active = false;
        if ( children != null )
        {
            for ( ResolutionNode node : children )
            {
                node.disable();
            }
        }
    }
    public boolean filterTrail( ArtifactFilter filter )
        throws OverConstrainedVersionException
    {
        if ( filter != null )
        {
            for ( Artifact artifact : getTrail() )
            {
                if ( !filter.include( artifact ) )
                {
                    return false;
                }
            }
        }
        return true;
    }
    public String toString()
    {
        return artifact.toString() + " (" + depth + "; " + ( active ? "enabled" : "disabled" ) + ")";
    }
}
