package org.apache.maven.repository.metadata;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.apache.maven.artifact.ArtifactScopeEnum;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
@Component( role = GraphConflictResolver.class )
public class DefaultGraphConflictResolver
    implements GraphConflictResolver
{
    @Requirement( role = GraphConflictResolutionPolicy.class )
    protected GraphConflictResolutionPolicy policy;
    public MetadataGraph resolveConflicts( MetadataGraph graph, ArtifactScopeEnum scope )
        throws GraphConflictResolutionException
    {
        if ( policy == null )
        {
            throw new GraphConflictResolutionException( "no GraphConflictResolutionPolicy injected" );
        }
        if ( graph == null )
        {
            return null;
        }
        final MetadataGraphVertex entry = graph.getEntry();
        if ( entry == null )
        {
            return null;
        }
        if ( graph.isEmpty() )
        {
            throw new GraphConflictResolutionException( "graph with an entry, but not vertices do not exist" );
        }
        if ( graph.isEmptyEdges() )
        {
            return null; 
        }
        final TreeSet<MetadataGraphVertex> vertices = graph.getVertices();
        try
        {
            if ( vertices.size() == 1 )
            {
                return new MetadataGraph( entry );
            }
            final ArtifactScopeEnum requestedScope = ArtifactScopeEnum.checkScope( scope );
            MetadataGraph res = new MetadataGraph( vertices.size() );
            res.setVersionedVertices( false );
            res.setScopedVertices( false );
            MetadataGraphVertex resEntry = res.addVertex( entry.getMd() );
            res.setEntry( resEntry );
            res.setScope( requestedScope );
            for ( MetadataGraphVertex v : vertices )
            {
                final List<MetadataGraphEdge> ins = graph.getIncidentEdges( v );
                final MetadataGraphEdge edge = cleanEdges( v, ins, requestedScope );
                if ( edge == null )
                { 
                    if ( entry.equals( v ) )
                    { 
                        res.getEntry().getMd().setWhy( "This is a graph entry point. No links." );
                    }
                    else
                    {
                    }
                }
                else
                {
                    ArtifactMetadata md = v.getMd();
                    ArtifactMetadata newMd =
                        new ArtifactMetadata( md.getGroupId(), md.getArtifactId(), edge.getVersion(), md.getType(),
                                              md.getScopeAsEnum(), md.getClassifier(), edge.getArtifactUri(),
                                              edge.getSource() == null ? "" : edge.getSource().getMd().toString(),
                                              edge.isResolved(), edge.getTarget() == null ? null
                                                              : edge.getTarget().getMd().getError() );
                    MetadataGraphVertex newV = res.addVertex( newMd );
                    MetadataGraphVertex sourceV = res.addVertex( edge.getSource().getMd() );
                    res.addEdge( sourceV, newV, edge );
                }
            }
            MetadataGraph linkedRes = findLinkedSubgraph( res );
            return linkedRes;
        }
        catch ( MetadataResolutionException e )
        {
            throw new GraphConflictResolutionException( e );
        }
    }
    private MetadataGraph findLinkedSubgraph( MetadataGraph g )
    {
        if ( g.getVertices().size() == 1 )
        {
            return g;
        }
        List<MetadataGraphVertex> visited = new ArrayList<MetadataGraphVertex>( g.getVertices().size() );
        visit( g.getEntry(), visited, g );
        List<MetadataGraphVertex> dropList = new ArrayList<MetadataGraphVertex>( g.getVertices().size() );
        for ( MetadataGraphVertex v : g.getVertices() )
        {
            if ( !visited.contains( v ) )
            {
                dropList.add( v );
            }
        }
        if ( dropList.size() < 1 )
        {
            return g;
        }
        TreeSet<MetadataGraphVertex> vertices = g.getVertices();
        for ( MetadataGraphVertex v : dropList )
        {
            vertices.remove( v );
        }
        return g;
    }
    private void visit( MetadataGraphVertex from, List<MetadataGraphVertex> visited, MetadataGraph graph )
    {
        if ( visited.contains( from ) )
        {
            return;
        }
        visited.add( from );
        List<MetadataGraphEdge> exitList = graph.getExcidentEdges( from );
        if ( exitList != null && exitList.size() > 0 )
        {
            for ( MetadataGraphEdge e : graph.getExcidentEdges( from ) )
            {
                visit( e.getTarget(), visited, graph );
            }
        }
    }
    private MetadataGraphEdge cleanEdges( MetadataGraphVertex v, List<MetadataGraphEdge> edges,
                                          ArtifactScopeEnum scope )
    {
        if ( edges == null || edges.isEmpty() )
        {
            return null;
        }
        if ( edges.size() == 1 )
        {
            MetadataGraphEdge e = edges.get( 0 );
            if ( scope.encloses( e.getScope() ) )
            {
                return e;
            }
            return null;
        }
        MetadataGraphEdge res = null;
        for ( MetadataGraphEdge e : edges )
        {
            if ( !scope.encloses( e.getScope() ) )
            {
                continue;
            }
            if ( res == null )
            {
                res = e;
            }
            else
            {
                res = policy.apply( e, res );
            }
        }
        return res;
    }
}
