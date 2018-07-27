package org.apache.maven.repository.metadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.maven.artifact.ArtifactScopeEnum;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
@Component( role = ClasspathTransformation.class )
public class DefaultClasspathTransformation
    implements ClasspathTransformation
{
    @Requirement
    GraphConflictResolver conflictResolver;
    public ClasspathContainer transform( MetadataGraph dirtyGraph, ArtifactScopeEnum scope, boolean resolve )
        throws MetadataGraphTransformationException
    {
        try
        {
            if ( dirtyGraph == null || dirtyGraph.isEmpty() )
            {
                return null;
            }
            MetadataGraph cleanGraph = conflictResolver.resolveConflicts( dirtyGraph, scope );
            if ( cleanGraph == null || cleanGraph.isEmpty() )
            {
                return null;
            }
            ClasspathContainer cpc = new ClasspathContainer( scope );
            if ( cleanGraph.isEmptyEdges() )
            {
                ArtifactMetadata amd = cleanGraph.getEntry().getMd();
                cpc.add( amd );
            }
            else
            {
                ClasspathGraphVisitor v = new ClasspathGraphVisitor( cleanGraph, cpc );
                MetadataGraphVertex entry = cleanGraph.getEntry();
                ArtifactMetadata md = entry.getMd();
                v.visit( entry ); 
            }
            return cpc;
        }
        catch ( GraphConflictResolutionException e )
        {
            throw new MetadataGraphTransformationException( e );
        }
    }
    private class ClasspathGraphVisitor
    {
        MetadataGraph graph;
        ClasspathContainer cpc;
        List<MetadataGraphVertex> visited;
        protected ClasspathGraphVisitor( MetadataGraph cleanGraph, ClasspathContainer cpc )
        {
            this.cpc = cpc;
            this.graph = cleanGraph;
            visited = new ArrayList<MetadataGraphVertex>( cleanGraph.getVertices().size() );
        }
        protected void visit( MetadataGraphVertex node ) 
        {
            ArtifactMetadata md = node.getMd();
            if ( visited.contains( node ) )
            {
                return;
            }
            cpc.add( md );
            List<MetadataGraphEdge> exits = graph.getExcidentEdges( node );
            if ( exits != null && exits.size() > 0 )
            {
                MetadataGraphEdge[] sortedExits = exits.toArray( new MetadataGraphEdge[exits.size()] );
                Arrays.sort( sortedExits
                        ,
                        new Comparator<MetadataGraphEdge>()
                        {
                            public int compare( MetadataGraphEdge e1
                                            , MetadataGraphEdge e2
                                            )
                            {
                                if ( e1.getDepth() == e2.getDepth() )
                                {
                                    if ( e2.getPomOrder() == e1.getPomOrder() )
                                    {
                                        return e1.getTarget().toString().compareTo( e2.getTarget().toString() );
                                    }
                                    return e2.getPomOrder() - e1.getPomOrder();
                                }
                                return e2.getDepth() - e1.getDepth();
                            }
                        }
                );
                for ( MetadataGraphEdge e : sortedExits )
                {
                    MetadataGraphVertex targetNode = e.getTarget();
                    targetNode.getMd().setArtifactScope( e.getScope() );
                    targetNode.getMd().setWhy( e.getSource().getMd().toString() );
                    visit( targetNode );
                }
            }
        }
    }
}
