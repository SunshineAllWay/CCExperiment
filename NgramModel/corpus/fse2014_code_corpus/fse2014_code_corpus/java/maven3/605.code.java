package org.apache.maven.project.harness;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
class Xpp3DomNodePointer
    extends NodePointer
{
    private Xpp3Dom node;
    public Xpp3DomNodePointer( Xpp3Dom node )
    {
        super( null );
        this.node = node;
    }
    public Xpp3DomNodePointer( NodePointer parent, Xpp3Dom node )
    {
        super( parent );
        this.node = node;
    }
    @Override
    public int compareChildNodePointers( NodePointer pointer1, NodePointer pointer2 )
    {
        Xpp3Dom node1 = (Xpp3Dom) pointer1.getBaseValue();
        Xpp3Dom node2 = (Xpp3Dom) pointer2.getBaseValue();
        if ( node1 == node2 )
        {
            return 0;
        }
        for ( int i = 0; i < node.getChildCount(); i++ )
        {
            Xpp3Dom child = node.getChild( i );
            if ( child == node1 )
            {
                return -1;
            }
            if ( child == node2 )
            {
                return 1;
            }
        }
        return 0;
    }
    @Override
    public Object getValue()
    {
        return getValue(node);
    }
    private static Object getValue( Xpp3Dom node )
    {
        if ( node.getValue() != null )
        {
            return node.getValue().trim();
        }
        else
        {
            List<Object> children = new ArrayList<Object>();
            for ( int i = 0; i < node.getChildCount(); i++ )
            {
                children.add( getValue( node.getChild( i ) ) );
            }
            return children;
        }
    }
    @Override
    public Object getBaseValue()
    {
        return node;
    }
    @Override
    public Object getImmediateNode()
    {
        return node;
    }
    @Override
    public int getLength()
    {
        return 1;
    }
    @Override
    public QName getName()
    {
        return new QName( null, node.getName() );
    }
    @Override
    public boolean isCollection()
    {
        return false;
    }
    @Override
    public boolean isLeaf()
    {
        return node.getChildCount() <= 0;
    }
    @Override
    public void setValue( Object value )
    {
        throw new UnsupportedOperationException();
    }
    @Override
    public NodeIterator childIterator( NodeTest test, boolean reverse, NodePointer startWith )
    {
        return new Xpp3DomNodeIterator( this, test, reverse, startWith );
    }
    @Override
    public NodeIterator attributeIterator( QName qname )
    {
        return new Xpp3DomAttributeIterator( this, qname );
    }
}
