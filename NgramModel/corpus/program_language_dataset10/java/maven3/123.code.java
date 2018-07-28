package org.apache.maven.repository;
import java.util.ArrayList;
import java.util.List;
public class MetadataGraphNode
{
    MavenArtifactMetadata metadata;
    List<MetadataGraphNode> inNodes;
    List<MetadataGraphNode> exNodes;
    public MetadataGraphNode()
    {
        inNodes = new ArrayList<MetadataGraphNode>(4);
        exNodes = new ArrayList<MetadataGraphNode>(8);
    }
    public MetadataGraphNode( MavenArtifactMetadata metadata )
    {
        this();
        this.metadata = metadata;
    }
    public MetadataGraphNode addIncident( MetadataGraphNode node )
    {
        inNodes.add( node );
        return this;
    }
    public MetadataGraphNode addExident( MetadataGraphNode node )
    {
        exNodes.add( node );
        return this;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( MetadataGraphNode.class.isAssignableFrom( obj.getClass() ) )
        {
            MetadataGraphNode node2 = (MetadataGraphNode) obj;
            if ( node2.metadata == null )
            {
                return metadata == null;
            }
            return metadata == null ? false : metadata.toString().equals( node2.metadata.toString() );
        }
        else
        {
            return super.equals( obj );
        }
    }
    @Override
    public int hashCode()
    {
        if ( metadata == null )
        {
            return super.hashCode();
        }
        return metadata.toString().hashCode();
    }
}
