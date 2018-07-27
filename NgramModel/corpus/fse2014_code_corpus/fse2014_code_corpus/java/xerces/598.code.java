package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import org.apache.xerces.stax.DefaultNamespaceContext;
public final class StartElementImpl extends ElementImpl implements StartElement {
    private static final Comparator QNAME_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            QName name1 = (QName) o1;
            QName name2 = (QName) o2;
            return name1.toString().compareTo(name2.toString());
        }};
    private final Map fAttributes;
    private final NamespaceContext fNamespaceContext;
    public StartElementImpl(final QName name, final Iterator attributes, final Iterator namespaces, final NamespaceContext namespaceContext, final Location location) {
        super(name, true, namespaces, location);
        if (attributes != null && attributes.hasNext()) {
            fAttributes = new TreeMap(QNAME_COMPARATOR);
            do {
                Attribute attr = (Attribute) attributes.next();
                fAttributes.put(attr.getName(), attr);
            }
            while (attributes.hasNext());
        }
        else {
            fAttributes = Collections.EMPTY_MAP;
        }
        fNamespaceContext = (namespaceContext != null) ? namespaceContext : DefaultNamespaceContext.getInstance();
    }
    public Iterator getAttributes() {
        return createImmutableIterator(fAttributes.values().iterator());
    }
    public Attribute getAttributeByName(final QName name) {
        return (Attribute) fAttributes.get(name);
    }
    public NamespaceContext getNamespaceContext() {
        return fNamespaceContext;
    }
    public String getNamespaceURI(final String prefix) {
        return fNamespaceContext.getNamespaceURI(prefix);
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write('<');
            QName name = getName();
            String prefix = name.getPrefix();
            if (prefix != null && prefix.length() > 0) {
                writer.write(prefix);
                writer.write(':');
            }
            writer.write(name.getLocalPart());
            Iterator nsIter = getNamespaces();
            while (nsIter.hasNext()) {
                Namespace ns = (Namespace) nsIter.next();
                writer.write(' ');
                ns.writeAsEncodedUnicode(writer);
            }
            Iterator attrIter = getAttributes();
            while (attrIter.hasNext()) {
                Attribute attr = (Attribute) attrIter.next();
                writer.write(' ');
                attr.writeAsEncodedUnicode(writer);
            }
            writer.write('>');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
