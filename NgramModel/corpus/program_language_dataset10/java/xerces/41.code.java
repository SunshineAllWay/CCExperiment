package xni;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
public class UpperCaseFilter
    extends PassThroughFilter {
    private final QName fQName = new QName();
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        super.startElement(toUpperCase(element), attributes, augs);
    } 
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        super.emptyElement(toUpperCase(element), attributes, augs);
    } 
    public void endElement(QName element, Augmentations augs)
        throws XNIException {
        super.endElement(toUpperCase(element), augs);
    } 
    protected QName toUpperCase(QName qname) {
        String prefix = qname.prefix != null
                      ? qname.prefix.toUpperCase() : null;
        String localpart = qname.localpart != null
                         ? qname.localpart.toUpperCase() : null;
        String rawname = qname.rawname != null
                       ? qname.rawname.toUpperCase() : null;
        String uri = qname.uri;
        fQName.setValues(prefix, localpart, rawname, uri);
        return fQName;
    } 
} 
