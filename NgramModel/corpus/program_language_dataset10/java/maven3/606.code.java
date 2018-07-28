package org.apache.maven.project.harness;
import java.util.Locale;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
public class Xpp3DomPointerFactory
    implements NodePointerFactory
{
    public int getOrder()
    {
        return 200;
    }
    public NodePointer createNodePointer( QName name, Object object, Locale locale )
    {
        if ( object instanceof Xpp3Dom )
        {
            return new Xpp3DomNodePointer( (Xpp3Dom) object );
        }
        return null;
    }
    public NodePointer createNodePointer( NodePointer parent, QName name, Object object )
    {
        if ( object instanceof Xpp3Dom )
        {
            return new Xpp3DomNodePointer( parent, (Xpp3Dom) object );
        }
        return null;
    }
}
