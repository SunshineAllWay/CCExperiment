package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.xs.util.XSGrammarPool;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XSGrammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.LSInputList;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.ls.LSInput;
public final class XSLoaderImpl implements XSLoader, DOMConfiguration {
    private final XSGrammarPool fGrammarPool = new XSGrammarMerger();
    private final XMLSchemaLoader fSchemaLoader = new XMLSchemaLoader();
    public XSLoaderImpl() {
        fSchemaLoader.setProperty(XMLSchemaLoader.XMLGRAMMAR_POOL, fGrammarPool);
    }
    public DOMConfiguration getConfig() {
        return this;
    }
    public XSModel loadURIList(StringList uriList) {
        int length = uriList.getLength();
        try {
            fGrammarPool.clear();
            for (int i = 0; i < length; ++i) {
                fSchemaLoader.loadGrammar(new XMLInputSource(null, uriList.item(i), null));
            }
            return fGrammarPool.toXSModel();
        } 
        catch (Exception e) {
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }
    public XSModel loadInputList(LSInputList is) {
        final int length = is.getLength();
        try {
            fGrammarPool.clear();
            for (int i = 0; i < length; ++i) {
                fSchemaLoader.loadGrammar(fSchemaLoader.dom2xmlInputSource(is.item(i)));
            }
            return fGrammarPool.toXSModel();
        } 
        catch (Exception e) {
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }
    public XSModel loadURI(String uri) {
        try {
            fGrammarPool.clear();
            return ((XSGrammar) fSchemaLoader.loadGrammar(new XMLInputSource(null, uri, null))).toXSModel();
        }
        catch (Exception e){
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }
    public XSModel load(LSInput is) {
        try {
            fGrammarPool.clear();
            return ((XSGrammar) fSchemaLoader.loadGrammar(fSchemaLoader.dom2xmlInputSource(is))).toXSModel();
        } 
        catch (Exception e) {
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }
    public void setParameter(String name, Object value) throws DOMException {
        fSchemaLoader.setParameter(name, value);
    }
    public Object getParameter(String name) throws DOMException {
        return fSchemaLoader.getParameter(name);
    }
    public boolean canSetParameter(String name, Object value) {
        return fSchemaLoader.canSetParameter(name, value);
    }
    public DOMStringList getParameterNames() {
        return fSchemaLoader.getParameterNames();
    }
    private static final class XSGrammarMerger extends XSGrammarPool {
        public XSGrammarMerger () {}
        public void putGrammar(Grammar grammar) {
            SchemaGrammar cachedGrammar = 
                toSchemaGrammar(super.getGrammar(grammar.getGrammarDescription()));
            if (cachedGrammar != null) {
                SchemaGrammar newGrammar = toSchemaGrammar(grammar);
                if (newGrammar != null) {
                    mergeSchemaGrammars(cachedGrammar, newGrammar);
                }
            }
            else {
                super.putGrammar(grammar);
            }
        }
        private SchemaGrammar toSchemaGrammar (Grammar grammar) {
            return (grammar instanceof SchemaGrammar) ? (SchemaGrammar) grammar : null;
        }
        private void mergeSchemaGrammars(SchemaGrammar cachedGrammar, SchemaGrammar newGrammar) {
            XSNamedMap map = newGrammar.getComponents(XSConstants.ELEMENT_DECLARATION);
            int length = map.getLength();
            for (int i = 0; i < length; ++i) {
                XSElementDecl decl = (XSElementDecl) map.item(i);
                if (cachedGrammar.getGlobalElementDecl(decl.getName()) == null) {
                    cachedGrammar.addGlobalElementDecl(decl);
                }
            }
            map = newGrammar.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
            length = map.getLength();
            for (int i = 0; i < length; ++i) {
                XSAttributeDecl decl = (XSAttributeDecl) map.item(i);
                if (cachedGrammar.getGlobalAttributeDecl(decl.getName()) == null) {
                    cachedGrammar.addGlobalAttributeDecl(decl);
                }
            }
            map = newGrammar.getComponents(XSConstants.TYPE_DEFINITION);
            length = map.getLength();
            for (int i = 0; i < length; ++i) {
                XSTypeDefinition decl = (XSTypeDefinition) map.item(i);
                if (cachedGrammar.getGlobalTypeDecl(decl.getName()) == null) {
                    cachedGrammar.addGlobalTypeDecl(decl);
                }
            }
            map = newGrammar.getComponents(XSConstants.ATTRIBUTE_GROUP);
            length = map.getLength();
            for (int i = 0; i < length; ++i) {
                XSAttributeGroupDecl decl = (XSAttributeGroupDecl) map.item(i);
                if (cachedGrammar.getGlobalAttributeGroupDecl(decl.getName()) == null) {
                    cachedGrammar.addGlobalAttributeGroupDecl(decl);
                }
            }
            map = newGrammar.getComponents(XSConstants.MODEL_GROUP);
            length = map.getLength();
            for (int i = 0; i < length; ++i) {
                XSGroupDecl decl = (XSGroupDecl) map.item(i);
                if (cachedGrammar.getGlobalGroupDecl(decl.getName()) == null) {
                    cachedGrammar.addGlobalGroupDecl(decl);
                }
            }
            map = newGrammar.getComponents(XSConstants.NOTATION_DECLARATION);
            length = map.getLength();
            for (int i = 0; i < length; ++i) {
                XSNotationDecl decl = (XSNotationDecl) map.item(i);
                if (cachedGrammar.getGlobalNotationDecl(decl.getName()) == null) {
                    cachedGrammar.addGlobalNotationDecl(decl);
                }
            }
            XSObjectList annotations = newGrammar.getAnnotations();
            length = annotations.getLength();
            for (int i = 0; i < length; ++i) {
                cachedGrammar.addAnnotation((XSAnnotationImpl) annotations.item(i));
            }
        }
        public boolean containsGrammar(XMLGrammarDescription desc) {
            return false;
        }
        public Grammar getGrammar(XMLGrammarDescription desc) {
            return null;
        }
        public Grammar retrieveGrammar(XMLGrammarDescription desc) {
            return null;
        }
        public Grammar [] retrieveInitialGrammarSet (String grammarType) {
            return new Grammar[0];
        }
    }
}
