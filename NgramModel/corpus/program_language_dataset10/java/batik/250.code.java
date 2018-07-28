package org.apache.batik.bridge.svg12;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.ErrorConstants;
import org.apache.batik.dom.AbstractAttrNS;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg12.BindableElement;
import org.apache.batik.dom.svg12.XBLEventSupport;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.apache.batik.dom.svg12.XBLOMDefinitionElement;
import org.apache.batik.dom.svg12.XBLOMImportElement;
import org.apache.batik.dom.svg12.XBLOMShadowTreeElement;
import org.apache.batik.dom.svg12.XBLOMTemplateElement;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.ShadowTreeEvent;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.dom.xbl.XBLManagerData;
import org.apache.batik.dom.xbl.XBLShadowTreeElement;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.XBLConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
public class DefaultXBLManager implements XBLManager, XBLConstants {
    protected boolean isProcessing;
    protected Document document;
    protected BridgeContext ctx;
    protected DoublyIndexedTable definitionLists = new DoublyIndexedTable();
    protected DoublyIndexedTable definitions = new DoublyIndexedTable();
    protected Map contentManagers = new HashMap();
    protected Map imports = new HashMap();
    protected DocInsertedListener docInsertedListener
        = new DocInsertedListener();
    protected DocRemovedListener docRemovedListener
        = new DocRemovedListener();
    protected DocSubtreeListener docSubtreeListener
        = new DocSubtreeListener();
    protected ImportAttrListener importAttrListener = new ImportAttrListener();
    protected RefAttrListener refAttrListener = new RefAttrListener();
    protected EventListenerList bindingListenerList = new EventListenerList();
    protected EventListenerList contentSelectionChangedListenerList
        = new EventListenerList();
    public DefaultXBLManager(Document doc, BridgeContext ctx) {
        document = doc;
        this.ctx = ctx;
        ImportRecord ir = new ImportRecord(null, null);
        imports.put(null, ir);
    }
    public void startProcessing() {
        if (isProcessing) {
            return;
        }
        NodeList nl = document.getElementsByTagNameNS(XBL_NAMESPACE_URI,
                                                      XBL_DEFINITION_TAG);
        XBLOMDefinitionElement[] defs
            = new XBLOMDefinitionElement[nl.getLength()];
        for (int i = 0; i < defs.length; i++) {
            defs[i] = (XBLOMDefinitionElement) nl.item(i);
        }
        nl = document.getElementsByTagNameNS(XBL_NAMESPACE_URI,
                                             XBL_IMPORT_TAG);
        Element[] imports
            = new Element[nl.getLength()];
        for (int i = 0; i < imports.length; i++) {
            imports[i] = (Element) nl.item(i);
        }
        AbstractDocument doc = (AbstractDocument) document;
        XBLEventSupport es = (XBLEventSupport) doc.initializeEventSupport();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             docRemovedListener, true);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             docInsertedListener, true);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMSubtreeModified",
             docSubtreeListener, true);
        for (int i = 0; i < defs.length; i++) {
            if (defs[i].getAttributeNS(null, XBL_REF_ATTRIBUTE).length() != 0) {
                addDefinitionRef(defs[i]);
            } else {
                String ns = defs[i].getElementNamespaceURI();
                String ln = defs[i].getElementLocalName();
                addDefinition(ns, ln, defs[i], null);
            }
        }
        for (int i = 0; i < imports.length; i++) {
            addImport(imports[i]);
        }
        isProcessing = true;
        bind(document.getDocumentElement());
    }
    public void stopProcessing() {
        if (!isProcessing) {
            return;
        }
        isProcessing = false;
        AbstractDocument doc = (AbstractDocument) document;
        XBLEventSupport es = (XBLEventSupport) doc.initializeEventSupport();
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             docRemovedListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             docInsertedListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMSubtreeModified",
             docSubtreeListener, true);
        int nSlots = imports.values().size();
        ImportRecord[] irs = new ImportRecord[ nSlots ];
        imports.values().toArray( irs );
        for (int i = 0; i < irs.length; i++) {
            ImportRecord ir = irs[i];
            if (ir.importElement.getLocalName().equals(XBL_DEFINITION_TAG)) {
                removeDefinitionRef(ir.importElement);
            } else {
                removeImport(ir.importElement);
            }
        }
        Object[] defRecs = definitions.getValuesArray();
        definitions.clear();
        for (int i = 0; i < defRecs.length; i++) {
            DefinitionRecord defRec = (DefinitionRecord) defRecs[i];
            TreeSet defs = (TreeSet) definitionLists.get(defRec.namespaceURI,
                                                         defRec.localName);
            if (defs != null) {
                while (!defs.isEmpty()) {
                    defRec = (DefinitionRecord) defs.first();
                    defs.remove(defRec);
                    removeDefinition(defRec);
                }
                definitionLists.put(defRec.namespaceURI, defRec.localName, null);
            }
        }
        definitionLists = new DoublyIndexedTable();
        contentManagers.clear();
    }
    public boolean isProcessing() {
        return isProcessing;
    }
    protected void addDefinitionRef(Element defRef) {
        String ref = defRef.getAttributeNS(null, XBL_REF_ATTRIBUTE);
        Element e = ctx.getReferencedElement(defRef, ref);
        if (!XBL_NAMESPACE_URI.equals(e.getNamespaceURI())
                || !XBL_DEFINITION_TAG.equals(e.getLocalName())) {
            throw new BridgeException
                (ctx, defRef, ErrorConstants.ERR_URI_BAD_TARGET,
                 new Object[] { ref });
        }
        ImportRecord ir = new ImportRecord(defRef, e);
        imports.put(defRef, ir);
        NodeEventTarget et = (NodeEventTarget) defRef;
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
             refAttrListener, false, null);
        XBLOMDefinitionElement d = (XBLOMDefinitionElement) defRef;
        String ns = d.getElementNamespaceURI();
        String ln = d.getElementLocalName();
        addDefinition(ns, ln, (XBLOMDefinitionElement) e, defRef);
    }
    protected void removeDefinitionRef(Element defRef) {
        ImportRecord ir = (ImportRecord) imports.get(defRef);
        NodeEventTarget et = (NodeEventTarget) defRef;
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
             refAttrListener, false);
        DefinitionRecord defRec
            = (DefinitionRecord) definitions.get(ir.node, defRef);
        removeDefinition(defRec);
        imports.remove(defRef);
    }
    protected void addImport(Element imp) {
        String bindings = imp.getAttributeNS(null, XBL_BINDINGS_ATTRIBUTE);
        Node n = ctx.getReferencedNode(imp, bindings);
        if (n.getNodeType() == Node.ELEMENT_NODE
                && !(XBL_NAMESPACE_URI.equals(n.getNamespaceURI())
                        && XBL_XBL_TAG.equals(n.getLocalName()))) {
            throw new BridgeException
                (ctx, imp, ErrorConstants.ERR_URI_BAD_TARGET,
                 new Object[] { n });
        }
        ImportRecord ir = new ImportRecord(imp, n);
        imports.put(imp, ir);
        NodeEventTarget et = (NodeEventTarget) imp;
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
             importAttrListener, false, null);
        et = (NodeEventTarget) n;
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
             ir.importInsertedListener, false, null);
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             ir.importRemovedListener, false, null);
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             ir.importSubtreeListener, false, null);
        addImportedDefinitions(imp, n);
    }
    protected void addImportedDefinitions(Element imp, Node n) {
        if (n instanceof XBLOMDefinitionElement) {
            XBLOMDefinitionElement def = (XBLOMDefinitionElement) n;
            String ns = def.getElementNamespaceURI();
            String ln = def.getElementLocalName();
            addDefinition(ns, ln, def, imp);
        } else {
            n = n.getFirstChild();
            while (n != null) {
                addImportedDefinitions(imp, n);
                n = n.getNextSibling();
            }
        }
    }
    protected void removeImport(Element imp) {
        ImportRecord ir = (ImportRecord) imports.get(imp);
        NodeEventTarget et = (NodeEventTarget) ir.node;
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
             ir.importInsertedListener, false);
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             ir.importRemovedListener, false);
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMSubtreeModified",
             ir.importSubtreeListener, false);
        et = (NodeEventTarget) imp;
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
             importAttrListener, false);
        Object[] defRecs = definitions.getValuesArray();
        for (int i = 0; i < defRecs.length; i++) {
            DefinitionRecord defRec = (DefinitionRecord) defRecs[i];
            if (defRec.importElement == imp) {
                removeDefinition(defRec);
            }
        }
        imports.remove(imp);
    }
    protected void addDefinition(String namespaceURI,
                                 String localName,
                                 XBLOMDefinitionElement def,
                                 Element imp) {
        ImportRecord ir = (ImportRecord) imports.get(imp);
        DefinitionRecord oldDefRec = null;
        DefinitionRecord defRec;
        TreeSet defs = (TreeSet) definitionLists.get(namespaceURI, localName);
        if (defs == null) {
            defs = new TreeSet();
            definitionLists.put(namespaceURI, localName, defs);
        } else if (defs.size() > 0) {
            oldDefRec = (DefinitionRecord) defs.first();
        }
        XBLOMTemplateElement template = null;
        for (Node n = def.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof XBLOMTemplateElement) {
                template = (XBLOMTemplateElement) n;
                break;
            }
        }
        defRec = new DefinitionRecord(namespaceURI, localName, def,
                                      template, imp);
        defs.add(defRec);
        definitions.put(def, imp, defRec);
        addDefinitionElementListeners(def, ir);
        if (defs.first() != defRec) {
            return;
        }
        if (oldDefRec != null) {
            XBLOMDefinitionElement oldDef = oldDefRec.definition;
            XBLOMTemplateElement oldTemplate = oldDefRec.template;
            if (oldTemplate != null) {
                removeTemplateElementListeners(oldTemplate, ir);
            }
            removeDefinitionElementListeners(oldDef, ir);
        }
        if (template != null) {
            addTemplateElementListeners(template, ir);
        }
        if (isProcessing) {
            rebind(namespaceURI, localName, document.getDocumentElement());
        }
    }
    protected void addDefinitionElementListeners(XBLOMDefinitionElement def,
                                                 ImportRecord ir) {
        XBLEventSupport es = (XBLEventSupport) def.initializeEventSupport();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             ir.defAttrListener, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             ir.defNodeInsertedListener, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             ir.defNodeRemovedListener, false);
    }
    protected void addTemplateElementListeners(XBLOMTemplateElement template,
                                               ImportRecord ir) {
        XBLEventSupport es
            = (XBLEventSupport) template.initializeEventSupport();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             ir.templateMutationListener, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             ir.templateMutationListener, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             ir.templateMutationListener, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMCharacterDataModified",
             ir.templateMutationListener, false);
    }
    protected void removeDefinition(DefinitionRecord defRec) {
        TreeSet defs = (TreeSet) definitionLists.get(defRec.namespaceURI,
                                                     defRec.localName);
        if (defs == null) {
            return;
        }
        Element imp = defRec.importElement;
        ImportRecord ir = (ImportRecord) imports.get(imp);
        DefinitionRecord activeDefRec = (DefinitionRecord) defs.first();
        defs.remove(defRec);
        definitions.remove(defRec.definition, imp);
        removeDefinitionElementListeners(defRec.definition, ir);
        if (defRec != activeDefRec) {
            return;
        }
        if (defRec.template != null) {
            removeTemplateElementListeners(defRec.template, ir);
        }
        rebind(defRec.namespaceURI, defRec.localName,
               document.getDocumentElement());
    }
    protected void removeDefinitionElementListeners
            (XBLOMDefinitionElement def,
             ImportRecord ir) {
        XBLEventSupport es = (XBLEventSupport) def.initializeEventSupport();
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             ir.defAttrListener, false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             ir.defNodeInsertedListener, false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             ir.defNodeRemovedListener, false);
    }
    protected void removeTemplateElementListeners
            (XBLOMTemplateElement template,
             ImportRecord ir) {
        XBLEventSupport es
            = (XBLEventSupport) template.initializeEventSupport();
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             ir.templateMutationListener, false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             ir.templateMutationListener, false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             ir.templateMutationListener, false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMCharacterDataModified",
             ir.templateMutationListener, false);
    }
    protected DefinitionRecord getActiveDefinition(String namespaceURI,
                                                   String localName) {
        TreeSet defs = (TreeSet) definitionLists.get(namespaceURI, localName);
        if (defs == null || defs.size() == 0) {
            return null;
        }
        return (DefinitionRecord) defs.first();
    }
    protected void unbind(Element e) {
        if (e instanceof BindableElement) {
            setActiveDefinition((BindableElement) e, null);
        } else {
            NodeList nl = getXblScopedChildNodes(e);
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    unbind((Element) n);
                }
            }
        }
    }
    protected void bind(Element e) {
        AbstractDocument doc = (AbstractDocument) e.getOwnerDocument();
        if (doc != document) {
            XBLManager xm = doc.getXBLManager();
            if (xm instanceof DefaultXBLManager) {
                ((DefaultXBLManager) xm).bind(e);
                return;
            }
        }
        if (e instanceof BindableElement) {
            DefinitionRecord defRec
                = getActiveDefinition(e.getNamespaceURI(),
                                      e.getLocalName());
            setActiveDefinition((BindableElement) e, defRec);
        } else {
            NodeList nl = getXblScopedChildNodes(e);
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    bind((Element) n);
                }
            }
        }
    }
    protected void rebind(String namespaceURI, String localName, Element e) {
        AbstractDocument doc = (AbstractDocument) e.getOwnerDocument();
        if (doc != document) {
            XBLManager xm = doc.getXBLManager();
            if (xm instanceof DefaultXBLManager) {
                ((DefaultXBLManager) xm).rebind(namespaceURI, localName, e);
                return;
            }
        }
        if (e instanceof BindableElement
                && namespaceURI.equals(e.getNamespaceURI())
                && localName.equals(e.getLocalName())) {
            DefinitionRecord defRec
                = getActiveDefinition(e.getNamespaceURI(),
                                      e.getLocalName());
            setActiveDefinition((BindableElement) e, defRec);
        } else {
            NodeList nl = getXblScopedChildNodes(e);
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    rebind(namespaceURI, localName, (Element) n);
                }
            }
        }
    }
    protected void setActiveDefinition(BindableElement elt,
                                       DefinitionRecord defRec) {
        XBLRecord rec = getRecord(elt);
        rec.definitionElement = defRec == null ? null : defRec.definition;
        if (defRec != null
                && defRec.definition != null
                && defRec.template != null) {
            setXblShadowTree(elt, cloneTemplate(defRec.template));
        } else {
            setXblShadowTree(elt, null);
        }
    }
    protected void setXblShadowTree(BindableElement elt,
                                    XBLOMShadowTreeElement newShadow) {
        XBLOMShadowTreeElement oldShadow
            = (XBLOMShadowTreeElement) getXblShadowTree(elt);
        if (oldShadow != null) {
            fireShadowTreeEvent(elt, XBL_UNBINDING_EVENT_TYPE, oldShadow);
            ContentManager cm = getContentManager(oldShadow);
            if (cm != null) {
                cm.dispose();
            }
            elt.setShadowTree(null);
            XBLRecord rec = getRecord(oldShadow);
            rec.boundElement = null;
            oldShadow.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                 "DOMSubtreeModified",
                 docSubtreeListener, false);
        }
        if (newShadow != null) {
            newShadow.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                 "DOMSubtreeModified",
                 docSubtreeListener, false, null);
            fireShadowTreeEvent(elt, XBL_PREBIND_EVENT_TYPE, newShadow);
            elt.setShadowTree(newShadow);
            XBLRecord rec = getRecord(newShadow);
            rec.boundElement = elt;
            AbstractDocument doc
                = (AbstractDocument) elt.getOwnerDocument();
            XBLManager xm = doc.getXBLManager();
            ContentManager cm = new ContentManager(newShadow, xm);
            setContentManager(newShadow, cm);
        }
        invalidateChildNodes(elt);
        if (newShadow != null) {
            NodeList nl = getXblScopedChildNodes(elt);
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    bind((Element) n);
                }
            }
            dispatchBindingChangedEvent(elt, newShadow);
            fireShadowTreeEvent(elt, XBL_BOUND_EVENT_TYPE, newShadow);
        } else {
            dispatchBindingChangedEvent(elt, newShadow);
        }
    }
    protected void fireShadowTreeEvent(BindableElement elt,
                                       String type,
                                       XBLShadowTreeElement e) {
        DocumentEvent de = (DocumentEvent) elt.getOwnerDocument();
        ShadowTreeEvent evt
            = (ShadowTreeEvent) de.createEvent("ShadowTreeEvent");
        evt.initShadowTreeEventNS(XBL_NAMESPACE_URI, type, true, false, e);
        elt.dispatchEvent(evt);
    }
    protected XBLOMShadowTreeElement cloneTemplate
            (XBLOMTemplateElement template) {
        XBLOMShadowTreeElement clone =
            (XBLOMShadowTreeElement)
            template.getOwnerDocument().createElementNS(XBL_NAMESPACE_URI,
                                                        XBL_SHADOW_TREE_TAG);
        NamedNodeMap attrs = template.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if (attr instanceof AbstractAttrNS) {
                clone.setAttributeNodeNS(attr);
            } else {
                clone.setAttributeNode(attr);
            }
        }
        for (Node n = template.getFirstChild();
                n != null;
                n = n.getNextSibling()) {
            clone.appendChild(n.cloneNode(true));
        }
        return clone;
    }
    public Node getXblParentNode(Node n) {
        Node contentElement = getXblContentElement(n);
        Node parent = contentElement == null
                        ? n.getParentNode()
                        : contentElement.getParentNode();
        if (parent instanceof XBLOMContentElement) {
            parent = parent.getParentNode();
        }
        if (parent instanceof XBLOMShadowTreeElement) {
            parent = getXblBoundElement(parent);
        }
        return parent;
    }
    public NodeList getXblChildNodes(Node n) {
        XBLRecord rec = getRecord(n);
        if (rec.childNodes == null) {
            rec.childNodes = new XblChildNodes(rec);
        }
        return rec.childNodes;
    }
    public NodeList getXblScopedChildNodes(Node n) {
        XBLRecord rec = getRecord(n);
        if (rec.scopedChildNodes == null) {
            rec.scopedChildNodes = new XblScopedChildNodes(rec);
        }
        return rec.scopedChildNodes;
    }
    public Node getXblFirstChild(Node n) {
        NodeList nl = getXblChildNodes(n);
        return nl.item(0);
    }
    public Node getXblLastChild(Node n) {
        NodeList nl = getXblChildNodes(n);
        return nl.item(nl.getLength() - 1);
    }
    public Node getXblPreviousSibling(Node n) {
        Node p = getXblParentNode(n);
        if (p == null || getRecord(p).childNodes == null) {
            return n.getPreviousSibling();
        }
        XBLRecord rec = getRecord(n);
        if (!rec.linksValid) {
            updateLinks(n);
        }
        return rec.previousSibling;
    }
    public Node getXblNextSibling(Node n) {
        Node p = getXblParentNode(n);
        if (p == null || getRecord(p).childNodes == null) {
            return n.getNextSibling();
        }
        XBLRecord rec = getRecord(n);
        if (!rec.linksValid) {
            updateLinks(n);
        }
        return rec.nextSibling;
    }
    public Element getXblFirstElementChild(Node n) {
        n = getXblFirstChild(n);
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
            n = getXblNextSibling(n);
        }
        return (Element) n;
    }
    public Element getXblLastElementChild(Node n) {
        n = getXblLastChild(n);
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
            n = getXblPreviousSibling(n);
        }
        return (Element) n;
    }
    public Element getXblPreviousElementSibling(Node n) {
        do {
            n = getXblPreviousSibling(n);
        } while (n != null && n.getNodeType() != Node.ELEMENT_NODE);
        return (Element) n;
    }
    public Element getXblNextElementSibling(Node n) {
        do {
            n = getXblNextSibling(n);
        } while (n != null && n.getNodeType() != Node.ELEMENT_NODE);
        return (Element) n;
    }
    public Element getXblBoundElement(Node n) {
        while (n != null && !(n instanceof XBLShadowTreeElement)) {
            XBLOMContentElement content = getXblContentElement(n);
            if (content != null) {
                n = content;
            }
            n = n.getParentNode();
        }
        if (n == null) {
            return null;
        }
        return getRecord(n).boundElement;
    }
    public Element getXblShadowTree(Node n) {
        if (n instanceof BindableElement) {
            BindableElement elt = (BindableElement) n;
            return elt.getShadowTree();
        }
        return null;
    }
    public NodeList getXblDefinitions(Node n) {
        final String namespaceURI = n.getNamespaceURI();
        final String localName = n.getLocalName();
        return new NodeList() {
            public Node item(int i) {
                TreeSet defs = (TreeSet) definitionLists.get(namespaceURI, localName);
                if (defs != null && defs.size() != 0 && i == 0) {
                    DefinitionRecord defRec = (DefinitionRecord) defs.first();
                    return defRec.definition;
                }
                return null;
            }
            public int getLength() {
                Set defs = (TreeSet) definitionLists.get(namespaceURI, localName);
                return defs != null && defs.size() != 0 ? 1 : 0;
            }
        };
    }
    protected XBLRecord getRecord(Node n) {
        XBLManagerData xmd = (XBLManagerData) n;
        XBLRecord rec = (XBLRecord) xmd.getManagerData();
        if (rec == null) {
            rec = new XBLRecord();
            rec.node = n;
            xmd.setManagerData(rec);
        }
        return rec;
    }
    protected void updateLinks(Node n) {
        XBLRecord rec = getRecord(n);
        rec.previousSibling = null;
        rec.nextSibling = null;
        rec.linksValid = true;
        Node p = getXblParentNode(n);
        if (p != null) {
            NodeList xcn = getXblChildNodes(p);
            if (xcn instanceof XblChildNodes) {
                ((XblChildNodes) xcn).update();
            }
        }
    }
    public XBLOMContentElement getXblContentElement(Node n) {
        return getRecord(n).contentElement;
    }
    public static int computeBubbleLimit(Node from, Node to) {
        ArrayList fromList = new ArrayList(10);
        ArrayList toList = new ArrayList(10);
        while (from != null) {
            fromList.add(from);
            from = ((NodeXBL) from).getXblParentNode();
        }
        while (to != null) {
            toList.add(to);
            to = ((NodeXBL) to).getXblParentNode();
        }
        int fromSize = fromList.size();
        int toSize = toList.size();
        for (int i = 0; i < fromSize && i < toSize; i++) {
            Node n1 = (Node) fromList.get(fromSize - i - 1);
            Node n2 = (Node) toList.get(toSize - i - 1);
            if (n1 != n2) {
                Node prevBoundElement = ((NodeXBL) n1).getXblBoundElement();
                while (i > 0 && prevBoundElement != fromList.get(fromSize - i - 1)) {
                    i--;
                }
                return fromSize - i - 1;
            }
        }
        return 1;
    }
    public ContentManager getContentManager(Node n) {
        Node b = getXblBoundElement(n);
        if (b != null) {
            Element s = getXblShadowTree(b);
            if (s != null) {
                ContentManager cm;
                Document doc = b.getOwnerDocument();
                if (doc != document) {
                    DefaultXBLManager xm = (DefaultXBLManager)
                        ((AbstractDocument) doc).getXBLManager();
                    cm = (ContentManager) xm.contentManagers.get(s);
                } else {
                    cm = (ContentManager) contentManagers.get(s);
                }
                return cm;
            }
        }
        return null;
    }
    void setContentManager(Element shadow, ContentManager cm) {
        if (cm == null) {
            contentManagers.remove(shadow);
        } else {
            contentManagers.put(shadow, cm);
        }
    }
    public void invalidateChildNodes(Node n) {
        XBLRecord rec = getRecord(n);
        if (rec.childNodes != null) {
            rec.childNodes.invalidate();
        }
        if (rec.scopedChildNodes != null) {
            rec.scopedChildNodes.invalidate();
        }
    }
    public void addContentSelectionChangedListener
            (ContentSelectionChangedListener l) {
        contentSelectionChangedListenerList.add
            (ContentSelectionChangedListener.class, l);
    }
    public void removeContentSelectionChangedListener
            (ContentSelectionChangedListener l) {
        contentSelectionChangedListenerList.remove
            (ContentSelectionChangedListener.class, l);
    }
    protected Object[] getContentSelectionChangedListeners() {
        return contentSelectionChangedListenerList.getListenerList();
    }
    void shadowTreeSelectedContentChanged(Set deselected, Set selected) {
        Iterator i = deselected.iterator();
        while (i.hasNext()) {
            Node n = (Node) i.next();
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                unbind((Element) n);
            }
        }
        i = selected.iterator();
        while (i.hasNext()) {
            Node n = (Node) i.next();
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                bind((Element) n);
            }
        }
    }
    public void addBindingListener(BindingListener l) {
        bindingListenerList.add(BindingListener.class, l);
    }
    public void removeBindingListener(BindingListener l) {
        bindingListenerList.remove(BindingListener.class, l);
    }
    protected void dispatchBindingChangedEvent(Element bindableElement,
                                               Element shadowTree) {
        Object[] ls = bindingListenerList.getListenerList();
        for (int i = ls.length - 2; i >= 0; i -= 2) {
            BindingListener l = (BindingListener) ls[i + 1];
            l.bindingChanged(bindableElement, shadowTree);
        }
    }
    protected boolean isActiveDefinition(XBLOMDefinitionElement def,
                                         Element imp) {
        DefinitionRecord defRec = (DefinitionRecord) definitions.get(def, imp);
        if (defRec == null) {
            return false;
        }
        return defRec == getActiveDefinition(defRec.namespaceURI,
                                             defRec.localName);
    }
    protected class DefinitionRecord implements Comparable {
        public String namespaceURI;
        public String localName;
        public XBLOMDefinitionElement definition;
        public XBLOMTemplateElement template;
        public Element importElement;
        public DefinitionRecord(String ns,
                                String ln,
                                XBLOMDefinitionElement def,
                                XBLOMTemplateElement t,
                                Element imp) {
            namespaceURI = ns;
            localName = ln;
            definition = def;
            template = t;
            importElement = imp;
        }
        public boolean equals(Object other) {
            return compareTo(other) == 0;
        }
        public int compareTo(Object other) {
            DefinitionRecord rec = (DefinitionRecord) other;
            AbstractNode n1, n2;
            if (importElement == null) {
                n1 = definition;
                if (rec.importElement == null) {
                    n2 = rec.definition;
                } else {
                    n2 = (AbstractNode) rec.importElement;
                }
            } else if (rec.importElement == null) {
                n1 = (AbstractNode) importElement;
                n2 = rec.definition;
            } else if (definition.getOwnerDocument()
                        == rec.definition.getOwnerDocument()) {
                n1 = definition;
                n2 = rec.definition;
            } else {
                n1 = (AbstractNode) importElement;
                n2 = (AbstractNode) rec.importElement;
            }
            short comp = n1.compareDocumentPosition(n2);
            if ((comp & AbstractNode.DOCUMENT_POSITION_PRECEDING) != 0) {
                return -1;
            }
            if ((comp & AbstractNode.DOCUMENT_POSITION_FOLLOWING) != 0) {
                return 1;
            }
            return 0;
        }
    }
    protected class ImportRecord {
        public Element importElement;
        public Node node;
        public DefNodeInsertedListener defNodeInsertedListener;
        public DefNodeRemovedListener defNodeRemovedListener;
        public DefAttrListener defAttrListener;
        public ImportInsertedListener importInsertedListener;
        public ImportRemovedListener importRemovedListener;
        public ImportSubtreeListener importSubtreeListener;
        public TemplateMutationListener templateMutationListener;
        public ImportRecord(Element imp, Node n) {
            importElement = imp;
            node = n;
            defNodeInsertedListener = new DefNodeInsertedListener(imp);
            defNodeRemovedListener = new DefNodeRemovedListener(imp);
            defAttrListener = new DefAttrListener(imp);
            importInsertedListener = new ImportInsertedListener(imp);
            importRemovedListener = new ImportRemovedListener();
            importSubtreeListener
                = new ImportSubtreeListener(imp, importRemovedListener);
            templateMutationListener = new TemplateMutationListener(imp);
        }
    }
    protected class ImportInsertedListener implements EventListener {
        protected Element importElement;
        public ImportInsertedListener(Element importElement) {
            this.importElement = importElement;
        }
        public void handleEvent(Event evt) {
            EventTarget target = evt.getTarget();
            if (target instanceof XBLOMDefinitionElement) {
                XBLOMDefinitionElement def = (XBLOMDefinitionElement) target;
                addDefinition(def.getElementNamespaceURI(),
                              def.getElementLocalName(),
                              def,
                              importElement);
            }
        }
    }
    protected class ImportRemovedListener implements EventListener {
        protected LinkedList toBeRemoved = new LinkedList();
        public void handleEvent(Event evt) {
            toBeRemoved.add(evt.getTarget());
        }
    }
    protected class ImportSubtreeListener implements EventListener {
        protected Element importElement;
        protected ImportRemovedListener importRemovedListener;
        public ImportSubtreeListener(Element imp,
                                     ImportRemovedListener irl) {
            importElement = imp;
            importRemovedListener = irl;
        }
        public void handleEvent(Event evt) {
            Object[] defs = importRemovedListener.toBeRemoved.toArray();
            importRemovedListener.toBeRemoved.clear();
            for (int i = 0; i < defs.length; i++) {
                XBLOMDefinitionElement def = (XBLOMDefinitionElement) defs[i];
                DefinitionRecord defRec
                    = (DefinitionRecord) definitions.get(def, importElement);
                removeDefinition(defRec);
            }
        }
    }
    protected class DocInsertedListener implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget target = evt.getTarget();
            if (target instanceof XBLOMDefinitionElement) {
                if (getXblBoundElement((Node) target) == null) {       
                    XBLOMDefinitionElement def
                        = (XBLOMDefinitionElement) target;
                    if (def.getAttributeNS(null, XBL_REF_ATTRIBUTE).length()
                            == 0) {
                        addDefinition(def.getElementNamespaceURI(),
                                      def.getElementLocalName(),
                                      def,
                                      null);
                    } else {
                        addDefinitionRef(def);
                    }
                }
            } else if (target instanceof XBLOMImportElement) {
                if (getXblBoundElement((Node) target) == null) {      
                    addImport((Element) target);
                }
            } else {
                evt = XBLEventSupport.getUltimateOriginalEvent(evt);
                target = evt.getTarget();
                Node parent = getXblParentNode((Node) target);
                if (parent != null) {
                    invalidateChildNodes(parent);
                }
                if (target instanceof BindableElement) {
                    for (Node n = ((Node) target).getParentNode();
                            n != null;
                            n = n.getParentNode()) {
                        if (n instanceof BindableElement
                                && getRecord(n).definitionElement != null) {
                            return;
                        }
                    }
                    bind((Element) target);
                }
            }
        }
    }
    protected class DocRemovedListener implements EventListener {
        protected LinkedList defsToBeRemoved = new LinkedList();
        protected LinkedList importsToBeRemoved = new LinkedList();
        protected LinkedList nodesToBeInvalidated = new LinkedList();
        public void handleEvent(Event evt) {
            EventTarget target = evt.getTarget();
            if (target instanceof XBLOMDefinitionElement) {
                if (getXblBoundElement((Node) target) == null) {
                    defsToBeRemoved.add(target);
                }
            } else if (target instanceof XBLOMImportElement) {
                if (getXblBoundElement((Node) target) == null) {
                    importsToBeRemoved.add(target);
                }
            }
            Node parent = getXblParentNode((Node) target);
            if (parent != null) {
                nodesToBeInvalidated.add(parent);
            }
        }
    }
    protected class DocSubtreeListener implements EventListener {
        public void handleEvent(Event evt) {
            Object[] defs = docRemovedListener.defsToBeRemoved.toArray();
            docRemovedListener.defsToBeRemoved.clear();
            for (int i = 0; i < defs.length; i++) {
                XBLOMDefinitionElement def = (XBLOMDefinitionElement) defs[i];
                if (def.getAttributeNS(null, XBL_REF_ATTRIBUTE).length() == 0) {
                    DefinitionRecord defRec
                        = (DefinitionRecord) definitions.get(def, null);
                    removeDefinition(defRec);
                } else {
                    removeDefinitionRef(def);
                }
            }
            Object[] imps = docRemovedListener.importsToBeRemoved.toArray();
            docRemovedListener.importsToBeRemoved.clear();
            for (int i = 0; i < imps.length; i++) {
                removeImport((Element) imps[i]);
            }
            Object[] nodes = docRemovedListener.nodesToBeInvalidated.toArray();
            docRemovedListener.nodesToBeInvalidated.clear();
            for (int i = 0; i < nodes.length; i++) {
                invalidateChildNodes((Node) nodes[i]);
            }
        }
    }
    protected class TemplateMutationListener implements EventListener {
        protected Element importElement;
        public TemplateMutationListener(Element imp) {
            importElement = imp;
        }
        public void handleEvent(Event evt) {
            Node n = (Node) evt.getTarget();
            while (n != null && !(n instanceof XBLOMDefinitionElement)) {
                n = n.getParentNode();
            }
            DefinitionRecord defRec
                = (DefinitionRecord) definitions.get(n, importElement);
            if (defRec == null) {
                return;
            }
            rebind(defRec.namespaceURI, defRec.localName,
                   document.getDocumentElement());
        }
    }
    protected class DefAttrListener implements EventListener {
        protected Element importElement;
        public DefAttrListener(Element imp) {
            importElement = imp;
        }
        public void handleEvent(Event evt) {
            EventTarget target = evt.getTarget();
            if (!(target instanceof XBLOMDefinitionElement)) {
                return;
            }
            XBLOMDefinitionElement def = (XBLOMDefinitionElement) target;
            if (!isActiveDefinition(def, importElement)) {
                return;
            }
            MutationEvent mevt = (MutationEvent) evt;
            String attrName = mevt.getAttrName();
            if (attrName.equals(XBL_ELEMENT_ATTRIBUTE)) {
                DefinitionRecord defRec =
                    (DefinitionRecord) definitions.get(def, importElement);
                removeDefinition(defRec);
                addDefinition(def.getElementNamespaceURI(),
                              def.getElementLocalName(),
                              def,
                              importElement);
            } else if (attrName.equals(XBL_REF_ATTRIBUTE)) {
                if (mevt.getNewValue().length() != 0) {
                    DefinitionRecord defRec =
                        (DefinitionRecord) definitions.get(def, importElement);
                    removeDefinition(defRec);
                    addDefinitionRef(def);
                }
            }
        }
    }
    protected class DefNodeInsertedListener implements EventListener {
        protected Element importElement;
        public DefNodeInsertedListener(Element imp) {
            importElement = imp;
        }
        public void handleEvent(Event evt) {
            MutationEvent mevt = (MutationEvent) evt;
            Node parent = mevt.getRelatedNode();
            if (!(parent instanceof XBLOMDefinitionElement)) {
                return;
            }
            EventTarget target = evt.getTarget();
            if (!(target instanceof XBLOMTemplateElement)) {
                return;
            }
            XBLOMTemplateElement template = (XBLOMTemplateElement) target;
            DefinitionRecord defRec
                = (DefinitionRecord) definitions.get(parent, importElement);
            if (defRec == null) {
                return;
            }
            ImportRecord ir = (ImportRecord) imports.get(importElement);
            if (defRec.template != null) {
                for (Node n = parent.getFirstChild();
                        n != null;
                        n = n.getNextSibling()) {
                    if (n == template) {
                        removeTemplateElementListeners(defRec.template, ir);
                        defRec.template = template;
                        break;
                    } else if (n == defRec.template) {
                        return;
                    }
                }
            } else {
                defRec.template = template;
            }
            addTemplateElementListeners(template, ir);
            rebind(defRec.namespaceURI, defRec.localName,
                   document.getDocumentElement());
        }
    }
    protected class DefNodeRemovedListener implements EventListener {
        protected Element importElement;
        public DefNodeRemovedListener(Element imp) {
            importElement = imp;
        }
        public void handleEvent(Event evt) {
            MutationEvent mevt = (MutationEvent) evt;
            Node parent = mevt.getRelatedNode();
            if (!(parent instanceof XBLOMDefinitionElement)) {
                return;
            }
            EventTarget target = evt.getTarget();
            if (!(target instanceof XBLOMTemplateElement)) {
                return;
            }
            XBLOMTemplateElement template = (XBLOMTemplateElement) target;
            DefinitionRecord defRec
                = (DefinitionRecord) definitions.get(parent, importElement);
            if (defRec == null || defRec.template != template) {
                return;
            }
            ImportRecord ir = (ImportRecord) imports.get(importElement);
            removeTemplateElementListeners(template, ir);
            defRec.template = null;
            for (Node n = template.getNextSibling();
                    n != null;
                    n = n.getNextSibling()) {
                if (n instanceof XBLOMTemplateElement) {
                    defRec.template = (XBLOMTemplateElement) n;
                    break;
                }
            }
            addTemplateElementListeners(defRec.template, ir);
            rebind(defRec.namespaceURI, defRec.localName,
                   document.getDocumentElement());
        }
    }
    protected class ImportAttrListener implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget target = evt.getTarget();
            if (target != evt.getCurrentTarget()) {
                return;
            }
            MutationEvent mevt = (MutationEvent) evt;
            if (mevt.getAttrName().equals(XBL_BINDINGS_ATTRIBUTE)) {
                Element imp = (Element) target;
                removeImport(imp);
                addImport(imp);
            }
        }
    }
    protected class RefAttrListener implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget target = evt.getTarget();
            if (target != evt.getCurrentTarget()) {
                return;
            }
            MutationEvent mevt = (MutationEvent) evt;
            if (mevt.getAttrName().equals(XBL_REF_ATTRIBUTE)) {
                Element defRef = (Element) target;
                removeDefinitionRef(defRef);
                if (mevt.getNewValue().length() == 0) {
                    XBLOMDefinitionElement def
                        = (XBLOMDefinitionElement) defRef;
                    String ns = def.getElementNamespaceURI();
                    String ln = def.getElementLocalName();
                    addDefinition(ns, ln,
                                  (XBLOMDefinitionElement) defRef, null);
                } else {
                    addDefinitionRef(defRef);
                }
            }
        }
    }
    protected class XBLRecord {
        public Node node;
        public XblChildNodes childNodes;
        public XblScopedChildNodes scopedChildNodes;
        public XBLOMContentElement contentElement;
        public XBLOMDefinitionElement definitionElement;
        public BindableElement boundElement;
        public boolean linksValid;
        public Node nextSibling;
        public Node previousSibling;
    }
    protected class XblChildNodes implements NodeList {
        protected XBLRecord record;
        protected List nodes;
        protected int size;
        public XblChildNodes(XBLRecord rec) {
            record = rec;
            nodes = new ArrayList();
            size = -1;
        }
        protected void update() {
            size = 0;
            Node shadowTree = getXblShadowTree(record.node);
            Node last = null;
            Node m = shadowTree == null ? record.node.getFirstChild()
                                        : shadowTree.getFirstChild();
            while (m != null) {
                last = collectXblChildNodes(m, last);
                m = m.getNextSibling();
            }
            if (last != null) {
                XBLRecord rec = getRecord(last);
                rec.nextSibling = null;
                rec.linksValid = true;
            }
        }
        protected Node collectXblChildNodes(Node n, Node prev) {
            boolean isChild = false;
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (!XBL_NAMESPACE_URI.equals(n.getNamespaceURI())) {
                    isChild = true;
                } else if (n instanceof XBLOMContentElement) {
                    ContentManager cm = getContentManager(n);
                    if (cm != null) {
                        NodeList selected =
                            cm.getSelectedContent((XBLOMContentElement) n);
                        for (int i = 0; i < selected.getLength(); i++) {
                            prev = collectXblChildNodes(selected.item(i),
                                                        prev);
                        }
                    }
                }
            } else {
                isChild = true;
            }
            if (isChild) {
                nodes.add(n);
                size++;
                if (prev != null) {
                    XBLRecord rec = getRecord(prev);
                    rec.nextSibling = n;
                    rec.linksValid = true;
                }
                XBLRecord rec = getRecord(n);
                rec.previousSibling = prev;
                rec.linksValid = true;
                prev = n;
            }
            return prev;
        }
        public void invalidate() {
            for (int i = 0; i < size; i++) {
                XBLRecord rec = getRecord((Node) nodes.get(i));
                rec.previousSibling = null;
                rec.nextSibling = null;
                rec.linksValid = false;
            }
            nodes.clear();
            size = -1;
        }
        public Node getFirstNode() {
            if (size == -1) {
                update();
            }
            return size == 0 ? null : (Node) nodes.get(0);
        }
        public Node getLastNode() {
            if (size == -1) {
                update();
            }
            return size == 0 ? null : (Node) nodes.get( nodes.size() -1 );
        }
        public Node item(int index) {
            if (size == -1) {
                update();
            }
            if (index < 0 || index >= size) {
                return null;
            }
            return (Node) nodes.get(index);
        }
        public int getLength() {
            if (size == -1) {
                update();
            }
            return size;
        }
    }
    protected class XblScopedChildNodes extends XblChildNodes {
        public XblScopedChildNodes(XBLRecord rec) {
            super(rec);
        }
        protected void update() {
            size = 0;
            Node shadowTree = getXblShadowTree(record.node);
            Node n = shadowTree == null ? record.node.getFirstChild()
                                        : shadowTree.getFirstChild();
            while (n != null) {
                collectXblScopedChildNodes(n);
                n = n.getNextSibling();
            }
        }
        protected void collectXblScopedChildNodes(Node n) {
            boolean isChild = false;
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (!n.getNamespaceURI().equals(XBL_NAMESPACE_URI)) {
                    isChild = true;
                } else if (n instanceof XBLOMContentElement) {
                    ContentManager cm = getContentManager(n);
                    if (cm != null) {
                        NodeList selected =
                            cm.getSelectedContent((XBLOMContentElement) n);
                        for (int i = 0; i < selected.getLength(); i++) {
                            collectXblScopedChildNodes(selected.item(i));
                        }
                    }
                }
            } else {
                isChild = true;
            }
            if (isChild) {
                nodes.add(n);
                size++;
            }
        }
    }
}
