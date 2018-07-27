package org.apache.maven.project.harness;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
class Xpp3DomAttributeIterator
    implements NodeIterator
{
    private NodePointer parent;
    private Xpp3Dom node;
    private List<Map.Entry<String, String>> attributes;
    private Map.Entry<String, String> attribute;
    private int position;
    public Xpp3DomAttributeIterator( NodePointer parent, QName qname )
    {
        this.parent = parent;
        this.node = (Xpp3Dom) parent.getNode();
        Map<String, String> map = new LinkedHashMap<String, String>();
        for ( String name : this.node.getAttributeNames() )
        {
            if ( name.equals( qname.getName() ) || "*".equals( qname.getName() ) )
            {
                String value = this.node.getAttribute( name );
                map.put( name, value );
            }
        }
        this.attributes = new ArrayList<Map.Entry<String, String>>( map.entrySet() );
    }
    public NodePointer getNodePointer()
    {
        if ( position == 0 )
        {
            setPosition( 1 );
        }
        return ( attribute == null ) ? null : new Xpp3DomAttributePointer( parent, attribute );
    }
    public int getPosition()
    {
        return position;
    }
    public boolean setPosition( int position )
    {
        this.position = position;
        attribute = ( position > 0 && position <= attributes.size() ) ? attributes.get( position - 1 ) : null;
        return attribute != null;
    }
}
