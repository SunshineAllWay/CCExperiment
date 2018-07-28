package org.apache.batik.dom;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.css.parser.ExtendedParserWrapper;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.Service;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.css.DOMImplementationCSS;
import org.w3c.dom.css.ViewCSS;
public abstract class ExtensibleDOMImplementation
    extends AbstractDOMImplementation
    implements DOMImplementationCSS,
               StyleSheetFactory {
    protected DoublyIndexedTable customFactories;
    protected List customValueManagers;
    protected List customShorthandManagers;
    public ExtensibleDOMImplementation() {
        Iterator iter = getDomExtensions().iterator();
        while(iter.hasNext()) {
            DomExtension de = (DomExtension)iter.next();
            de.registerTags(this);
        }
    }
    public void registerCustomElementFactory(String namespaceURI,
                                             String localName,
                                             ElementFactory factory) {
        if (customFactories == null) {
            customFactories = new DoublyIndexedTable();
        }
        customFactories.put(namespaceURI, localName, factory);
    }
    public void registerCustomCSSValueManager(ValueManager vm) {
        if (customValueManagers == null) {
            customValueManagers = new LinkedList();
        }
        customValueManagers.add(vm);
    }
    public void registerCustomCSSShorthandManager(ShorthandManager sm) {
        if (customShorthandManagers == null) {
            customShorthandManagers = new LinkedList();
        }
        customShorthandManagers.add(sm);
    }
    public CSSEngine createCSSEngine(AbstractStylableDocument doc,
                                     CSSContext ctx) {
        String pn = XMLResourceDescriptor.getCSSParserClassName();
        Parser p;
        try {
            p = (Parser)Class.forName(pn).newInstance();
        } catch (ClassNotFoundException e) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                                   formatMessage("css.parser.class",
                                                 new Object[] { pn }));
        } catch (InstantiationException e) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                                   formatMessage("css.parser.creation",
                                                 new Object[] { pn }));
        } catch (IllegalAccessException e) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                                   formatMessage("css.parser.access",
                                                 new Object[] { pn }));
        }
        ExtendedParser ep = ExtendedParserWrapper.wrap(p);
        ValueManager[] vms;
        if (customValueManagers == null) {
            vms = new ValueManager[0];
        } else {
            vms = new ValueManager[customValueManagers.size()];
            Iterator it = customValueManagers.iterator();
            int i = 0;
            while (it.hasNext()) {
                vms[i++] = (ValueManager)it.next();
            }
        }
        ShorthandManager[] sms;
        if (customShorthandManagers == null) {
            sms = new ShorthandManager[0];
        } else {
            sms = new ShorthandManager[customShorthandManagers.size()];
            Iterator it = customShorthandManagers.iterator();
            int i = 0;
            while (it.hasNext()) {
                sms[i++] = (ShorthandManager)it.next();
            }
        }
        CSSEngine result = createCSSEngine(doc, ctx, ep, vms, sms);
        doc.setCSSEngine(result);
        return result;
    }
    public abstract CSSEngine createCSSEngine(AbstractStylableDocument doc,
                                              CSSContext               ctx,
                                              ExtendedParser           ep,
                                              ValueManager     []      vms,
                                              ShorthandManager []      sms);
    public abstract ViewCSS createViewCSS(AbstractStylableDocument doc);
    public Element createElementNS(AbstractDocument document,
                                   String           namespaceURI,
                                   String           qualifiedName) {
        if (namespaceURI != null && namespaceURI.length() == 0) {
            namespaceURI = null;
        }
        if (namespaceURI == null)
            return new GenericElement(qualifiedName.intern(), document);
        if (customFactories != null) {
            String name = DOMUtilities.getLocalName(qualifiedName);
            ElementFactory cef;
            cef = (ElementFactory)customFactories.get(namespaceURI, name);
            if (cef != null) {
                return cef.create(DOMUtilities.getPrefix(qualifiedName),
                                  document);
            }
        }
        return new GenericElementNS(namespaceURI.intern(),
                                    qualifiedName.intern(),
                                    document);
    }
    public DocumentType createDocumentType(String qualifiedName,
                                           String publicId,
                                           String systemId) {
        if (qualifiedName == null) {
            qualifiedName = "";
        }
        int test = XMLUtilities.testXMLQName(qualifiedName);
        if ((test & XMLUtilities.IS_XML_10_NAME) == 0) {
            throw new DOMException
                (DOMException.INVALID_CHARACTER_ERR,
                 formatMessage("xml.name",
                               new Object[] { qualifiedName }));
        }
        if ((test & XMLUtilities.IS_XML_10_QNAME) == 0) {
            throw new DOMException
                (DOMException.INVALID_CHARACTER_ERR,
                 formatMessage("invalid.qname",
                               new Object[] { qualifiedName }));
        }
        return new GenericDocumentType(qualifiedName, publicId, systemId);
    }
    public interface ElementFactory {
        Element create(String prefix, Document doc);
    }
    protected static List extensions = null;
    protected static synchronized List getDomExtensions() {
        if (extensions != null)
            return extensions;
        extensions = new LinkedList();
        Iterator iter = Service.providers(DomExtension.class);
        while (iter.hasNext()) {
            DomExtension de = (DomExtension)iter.next();
            float priority  = de.getPriority();
            ListIterator li = extensions.listIterator();
            for (;;) {
                if (!li.hasNext()) {
                    li.add(de);
                    break;
                }
                DomExtension lde = (DomExtension)li.next();
                if (lde.getPriority() > priority) {
                    li.previous();
                    li.add(de);
                    break;
                }
            }
        }
        return extensions;
    }
}
