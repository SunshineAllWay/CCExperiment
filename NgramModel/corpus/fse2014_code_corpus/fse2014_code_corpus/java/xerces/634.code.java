package org.apache.xerces.util;
import java.io.IOException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.readers.OASISXMLCatalogReader;
import org.apache.xml.resolver.readers.SAXCatalogReader;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
public class XMLCatalogResolver 
    implements XMLEntityResolver, EntityResolver2, LSResourceResolver {
    private CatalogManager fResolverCatalogManager = null;
    private Catalog fCatalog = null;
    private String [] fCatalogsList = null;
    private boolean fCatalogsChanged = true;
    private boolean fPreferPublic = true;
    private boolean fUseLiteralSystemId = true;
    public XMLCatalogResolver () {
    	this(null, true);
    }
    public XMLCatalogResolver (String [] catalogs) {
    	this(catalogs, true);
    }
    public XMLCatalogResolver (String [] catalogs, boolean preferPublic) {
        init(catalogs, preferPublic);
    }
    public final synchronized String [] getCatalogList () {
        return (fCatalogsList != null) 
            ? (String[]) fCatalogsList.clone() : null;
    }
    public final synchronized void setCatalogList (String [] catalogs) {
        fCatalogsChanged = true;
        fCatalogsList = (catalogs != null)
            ? (String[]) catalogs.clone() : null;
    }
    public final synchronized void clear () {
        fCatalog = null;
    }
    public final boolean getPreferPublic () {
        return fPreferPublic;
    }
    public final void setPreferPublic (boolean preferPublic) {
        fPreferPublic = preferPublic;
        fResolverCatalogManager.setPreferPublic(preferPublic);
    }
    public final boolean getUseLiteralSystemId () {
        return fUseLiteralSystemId;
    }
    public final void setUseLiteralSystemId (boolean useLiteralSystemId) {
        fUseLiteralSystemId = useLiteralSystemId;
    }
    public InputSource resolveEntity(String publicId, String systemId)
    	 throws SAXException, IOException {
        String resolvedId = null;
        if (publicId != null && systemId != null) {
            resolvedId = resolvePublic(publicId, systemId);
        }
        else if (systemId != null) {
            resolvedId = resolveSystem(systemId);
        }
        if (resolvedId != null) {
            InputSource source = new InputSource(resolvedId);
            source.setPublicId(publicId);
            return source;
        }  
        return null;
    }
     public InputSource resolveEntity(String name, String publicId, 
         String baseURI, String systemId) throws SAXException, IOException { 
         String resolvedId = null;
         if (!getUseLiteralSystemId() && baseURI != null) {
             try {
                 URI uri = new URI(new URI(baseURI), systemId);
                 systemId = uri.toString();
             }
             catch (URI.MalformedURIException ex) {}
         }
         if (publicId != null && systemId != null) {
             resolvedId = resolvePublic(publicId, systemId);
         }
         else if (systemId != null) {
             resolvedId = resolveSystem(systemId);
         }
         if (resolvedId != null) {
             InputSource source = new InputSource(resolvedId);
             source.setPublicId(publicId);
             return source;
         }  
         return null;
    }
     public InputSource getExternalSubset(String name, String baseURI)
         throws SAXException, IOException {
         return null;
     }
    public LSInput resolveResource(String type, String namespaceURI,
        String publicId, String systemId, String baseURI) {
        String resolvedId = null;
        try {
            if (namespaceURI != null) {
                resolvedId = resolveURI(namespaceURI);
            }
            if (!getUseLiteralSystemId() && baseURI != null) {
                try {
                    URI uri = new URI(new URI(baseURI), systemId);
                    systemId = uri.toString();
                }
                catch (URI.MalformedURIException ex) {}
            }
            if (resolvedId == null) {
                if (publicId != null && systemId != null) {
                    resolvedId = resolvePublic(publicId, systemId);
                }
                else if (systemId != null) {
                    resolvedId = resolveSystem(systemId);
                }
            }
        }
        catch (IOException ex) {}
        if (resolvedId != null) {
            return new DOMInputImpl(publicId, resolvedId, baseURI);
        }  
        return null;
    }
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
        throws XNIException, IOException {
        String resolvedId = resolveIdentifier(resourceIdentifier);
        if (resolvedId != null) {
            return new XMLInputSource(resourceIdentifier.getPublicId(), 
                                      resolvedId, 
                                      resourceIdentifier.getBaseSystemId());
        }
        return null;
    }
    public String resolveIdentifier(XMLResourceIdentifier resourceIdentifier)
        throws IOException, XNIException {
        String resolvedId = null;
        String namespace = resourceIdentifier.getNamespace();
        if (namespace != null) {
            resolvedId = resolveURI(namespace);
        }
        if (resolvedId == null) {
            String publicId = resourceIdentifier.getPublicId();
            String systemId = getUseLiteralSystemId() 
                ? resourceIdentifier.getLiteralSystemId()
                : resourceIdentifier.getExpandedSystemId();
            if (publicId != null && systemId != null) {
                resolvedId = resolvePublic(publicId, systemId);
            }
            else if (systemId != null) {
                resolvedId = resolveSystem(systemId);
            }
        }
        return resolvedId;
    }
    public final synchronized String resolveSystem (String systemId) 
        throws IOException {
        if (fCatalogsChanged) {
            parseCatalogs();
            fCatalogsChanged = false;
        }
        return (fCatalog != null) 
            ? fCatalog.resolveSystem(systemId) : null;
    }
    public final synchronized String resolvePublic (String publicId, String systemId) 
        throws IOException {
        if (fCatalogsChanged) {
            parseCatalogs();
            fCatalogsChanged = false;
        }
        return (fCatalog != null) 
            ? fCatalog.resolvePublic(publicId, systemId) : null;
    }
    public final synchronized String resolveURI (String uri) 
        throws IOException {
        if (fCatalogsChanged) {
            parseCatalogs();
            fCatalogsChanged = false;
        }
        return (fCatalog != null)
            ? fCatalog.resolveURI(uri) : null;
    }
    private void init (String [] catalogs, boolean preferPublic) {
        fCatalogsList = (catalogs != null) ? (String[]) catalogs.clone() : null;
        fPreferPublic = preferPublic;
        fResolverCatalogManager = new CatalogManager();
        fResolverCatalogManager.setAllowOasisXMLCatalogPI(false);
        fResolverCatalogManager.setCatalogClassName("org.apache.xml.resolver.Catalog");
        fResolverCatalogManager.setCatalogFiles("");
        fResolverCatalogManager.setIgnoreMissingProperties(true);
        fResolverCatalogManager.setPreferPublic(fPreferPublic);
        fResolverCatalogManager.setRelativeCatalogs(false);
        fResolverCatalogManager.setUseStaticCatalog(false);
        fResolverCatalogManager.setVerbosity(0);
    }
    private void parseCatalogs () throws IOException {
        if (fCatalogsList != null) {
            fCatalog = new Catalog(fResolverCatalogManager);
            attachReaderToCatalog(fCatalog);
            for (int i = 0; i < fCatalogsList.length; ++i) {
                String catalog = fCatalogsList[i];
                if (catalog != null && catalog.length() > 0) {
                    fCatalog.parseCatalog(catalog);
                }
            }
        }
        else {
            fCatalog = null;
        }
    }
    private void attachReaderToCatalog (Catalog catalog) {
        SAXParserFactory spf = new SAXParserFactoryImpl();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        SAXCatalogReader saxReader = new SAXCatalogReader(spf);
        saxReader.setCatalogParser(OASISXMLCatalogReader.namespaceName, "catalog", 
            "org.apache.xml.resolver.readers.OASISXMLCatalogReader");
        catalog.addReader("application/xml", saxReader);
    }
}
