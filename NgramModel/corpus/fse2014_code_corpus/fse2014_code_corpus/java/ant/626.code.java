package org.apache.tools.ant.types;
import java.lang.reflect.Method;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
public class XMLCatalog extends DataType
    implements Cloneable, EntityResolver, URIResolver {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private Vector elements = new Vector();
    private Path classpath;
    private Path catalogPath;
    public static final String APACHE_RESOLVER
        = "org.apache.tools.ant.types.resolver.ApacheCatalogResolver";
    public static final String CATALOG_RESOLVER
        = "org.apache.xml.resolver.tools.CatalogResolver";
    public XMLCatalog() {
        setChecked(false);
    }
    private Vector getElements() {
        return getRef().elements;
    }
    private Path getClasspath() {
        return getRef().classpath;
    }
    public Path createClasspath() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        setChecked(false);
        return this.classpath.createPath();
    }
    public void setClasspath(Path classpath) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
        setChecked(false);
    }
    public void setClasspathRef(Reference r) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        createClasspath().setRefid(r);
        setChecked(false);
    }
    public Path createCatalogPath() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (this.catalogPath == null) {
            this.catalogPath = new Path(getProject());
        }
        setChecked(false);
        return this.catalogPath.createPath();
    }
    public void setCatalogPathRef(Reference r) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        createCatalogPath().setRefid(r);
        setChecked(false);
    }
    public Path getCatalogPath() {
        return getRef().catalogPath;
    }
    public void addDTD(ResourceLocation dtd) throws BuildException {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        getElements().addElement(dtd);
        setChecked(false);
    }
    public void addEntity(ResourceLocation entity) throws BuildException {
        addDTD(entity);
    }
    public void addConfiguredXMLCatalog(XMLCatalog catalog) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        Vector newElements = catalog.getElements();
        Vector ourElements = getElements();
        Enumeration e = newElements.elements();
        while (e.hasMoreElements()) {
            ourElements.addElement(e.nextElement());
        }
        Path nestedClasspath = catalog.getClasspath();
        createClasspath().append(nestedClasspath);
        Path nestedCatalogPath = catalog.getCatalogPath();
        createCatalogPath().append(nestedCatalogPath);
        setChecked(false);
    }
    public void setRefid(Reference r) throws BuildException {
        if (!elements.isEmpty()) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException {
        if (isReference()) {
            return getRef().resolveEntity(publicId, systemId);
        }
        dieOnCircularReference();
        log("resolveEntity: '" + publicId + "': '" + systemId + "'",
            Project.MSG_DEBUG);
        InputSource inputSource =
            getCatalogResolver().resolveEntity(publicId, systemId);
        if (inputSource == null) {
            log("No matching catalog entry found, parser will use: '"
                + systemId + "'", Project.MSG_DEBUG);
        }
        return inputSource;
    }
    public Source resolve(String href, String base)
        throws TransformerException {
        if (isReference()) {
            return getRef().resolve(href, base);
        }
        dieOnCircularReference();
        SAXSource source = null;
        String uri = removeFragment(href);
        log("resolve: '" + uri + "' with base: '" + base + "'", Project.MSG_DEBUG);
        source = (SAXSource) getCatalogResolver().resolve(uri, base);
        if (source == null) {
            log("No matching catalog entry found, parser will use: '"
                + href + "'", Project.MSG_DEBUG);
            source = new SAXSource();
            URL baseURL = null;
            try {
                if (base == null) {
                    baseURL = FILE_UTILS.getFileURL(getProject().getBaseDir());
                } else {
                    baseURL = new URL(base);
                }
                URL url = (uri.length() == 0 ? baseURL : new URL(baseURL, uri));
                source.setInputSource(new InputSource(url.toString()));
            } catch (MalformedURLException ex) {
                source.setInputSource(new InputSource(uri));
            }
        }
        setEntityResolver(source);
        return source;
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            if (classpath != null) {
                pushAndInvokeCircularReferenceCheck(classpath, stk, p);
            }
            if (catalogPath != null) {
                pushAndInvokeCircularReferenceCheck(catalogPath, stk, p);
            }
            setChecked(true);
        }
    }
    private XMLCatalog getRef() {
        if (!isReference()) {
            return this;
        }
        return (XMLCatalog) getCheckedRef(XMLCatalog.class, "xmlcatalog");
    }
    private CatalogResolver catalogResolver = null;
    private CatalogResolver getCatalogResolver() {
        if (catalogResolver == null) {
            AntClassLoader loader = null;
            loader = getProject().createClassLoader(Path.systemClasspath);
            try {
                Class clazz = Class.forName(APACHE_RESOLVER, true, loader);
                ClassLoader apacheResolverLoader = clazz.getClassLoader();
                Class baseResolverClass
                    = Class.forName(CATALOG_RESOLVER, true, apacheResolverLoader);
                ClassLoader baseResolverLoader
                    = baseResolverClass.getClassLoader();
                clazz = Class.forName(APACHE_RESOLVER, true, baseResolverLoader);
                Object obj  = clazz.newInstance();
                catalogResolver = new ExternalResolver(clazz, obj);
            } catch (Throwable ex) {
                catalogResolver = new InternalResolver();
                if (getCatalogPath() != null
                    && getCatalogPath().list().length != 0) {
                        log("Warning: XML resolver not found; external catalogs"
                            + " will be ignored", Project.MSG_WARN);
                    }
                log("Failed to load Apache resolver: " + ex, Project.MSG_DEBUG);
            }
        }
        return catalogResolver;
    }
    private void setEntityResolver(SAXSource source) throws TransformerException {
        XMLReader reader = source.getXMLReader();
        if (reader == null) {
            SAXParserFactory spFactory = SAXParserFactory.newInstance();
            spFactory.setNamespaceAware(true);
            try {
                reader = spFactory.newSAXParser().getXMLReader();
            } catch (ParserConfigurationException ex) {
                throw new TransformerException(ex);
            } catch (SAXException ex) {
                throw new TransformerException(ex);
            }
        }
        reader.setEntityResolver(this);
        source.setXMLReader(reader);
    }
    private ResourceLocation findMatchingEntry(String publicId) {
        Enumeration e = getElements().elements();
        ResourceLocation element = null;
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof ResourceLocation) {
                element = (ResourceLocation) o;
                if (element.getPublicId().equals(publicId)) {
                    return element;
                }
            }
        }
        return null;
    }
    private String removeFragment(String uri) {
        String result = uri;
        int hashPos = uri.indexOf("#");
        if (hashPos >= 0) {
            result = uri.substring(0, hashPos);
        }
        return result;
    }
    private InputSource filesystemLookup(ResourceLocation matchingEntry) {
        String uri = matchingEntry.getLocation();
        uri = uri.replace(File.separatorChar, '/');
        URL baseURL = null;
        if (matchingEntry.getBase() != null) {
            baseURL = matchingEntry.getBase();
        } else {
            try {
                baseURL = FILE_UTILS.getFileURL(getProject().getBaseDir());
            } catch (MalformedURLException ex) {
                throw new BuildException("Project basedir cannot be converted to a URL");
            }
        }
        InputSource source = null;
        URL url = null;
        try {
            url = new URL(baseURL, uri);
        } catch (MalformedURLException ex) {
            File testFile = new File(uri);
            if (testFile.exists() && testFile.canRead()) {
                log("uri : '"
                    + uri + "' matches a readable file", Project.MSG_DEBUG);
                try {
                    url = FILE_UTILS.getFileURL(testFile);
                } catch (MalformedURLException ex1) {
                    throw new BuildException(
                        "could not find an URL for :" + testFile.getAbsolutePath());
                }
            } else {
                log("uri : '"
                    + uri + "' does not match a readable file", Project.MSG_DEBUG);
            }
        }
        if (url != null && url.getProtocol().equals("file")) {
            String fileName = FILE_UTILS.fromURI(url.toString());
            if (fileName != null) {
                log("fileName " + fileName, Project.MSG_DEBUG);
                File resFile = new File(fileName);
                if (resFile.exists() && resFile.canRead()) {
                    try {
                        source = new InputSource(new FileInputStream(resFile));
                        String sysid = JAXPUtils.getSystemId(resFile);
                        source.setSystemId(sysid);
                        log("catalog entry matched a readable file: '"
                            + sysid + "'", Project.MSG_DEBUG);
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return source;
    }
    private InputSource classpathLookup(ResourceLocation matchingEntry) {
        InputSource source = null;
        AntClassLoader loader = null;
        Path cp = classpath;
        if (cp != null) {
            cp = classpath.concatSystemClasspath("ignore");
        } else {
            cp = (new Path(getProject())).concatSystemClasspath("last");
        }
        loader = getProject().createClassLoader(cp);
        InputStream is
            = loader.getResourceAsStream(matchingEntry.getLocation());
        if (is != null) {
            source = new InputSource(is);
            URL entryURL = loader.getResource(matchingEntry.getLocation());
            String sysid = entryURL.toExternalForm();
            source.setSystemId(sysid);
            log("catalog entry matched a resource in the classpath: '"
                + sysid + "'", Project.MSG_DEBUG);
        }
        return source;
    }
    private InputSource urlLookup(ResourceLocation matchingEntry) {
        String uri = matchingEntry.getLocation();
        URL baseURL = null;
        if (matchingEntry.getBase() != null) {
            baseURL = matchingEntry.getBase();
        } else {
            try {
                baseURL = FILE_UTILS.getFileURL(getProject().getBaseDir());
            } catch (MalformedURLException ex) {
                throw new BuildException("Project basedir cannot be converted to a URL");
            }
        }
        InputSource source = null;
        URL url = null;
        try {
            url = new URL(baseURL, uri);
        } catch (MalformedURLException ex) {
        }
        if (url != null) {
            try {
                InputStream is = url.openStream();
                if (is != null) {
                    source = new InputSource(is);
                    String sysid = url.toExternalForm();
                    source.setSystemId(sysid);
                    log("catalog entry matched as a URL: '"
                        + sysid + "'", Project.MSG_DEBUG);
                }
            } catch (IOException ex) {
            }
        }
        return source;
    }
    private interface CatalogResolver extends URIResolver, EntityResolver {
        InputSource resolveEntity(String publicId, String systemId);
        Source resolve(String href, String base) throws TransformerException;
    }
    private class InternalResolver implements CatalogResolver {
        public InternalResolver() {
            log("Apache resolver library not found, internal resolver will be used",
                Project.MSG_VERBOSE);
        }
        public InputSource resolveEntity(String publicId,
                                         String systemId) {
            InputSource result = null;
            ResourceLocation matchingEntry = findMatchingEntry(publicId);
            if (matchingEntry != null) {
                log("Matching catalog entry found for publicId: '"
                    + matchingEntry.getPublicId() + "' location: '"
                    + matchingEntry.getLocation() + "'",
                    Project.MSG_DEBUG);
                result = filesystemLookup(matchingEntry);
                if (result == null) {
                    result = classpathLookup(matchingEntry);
                }
                if (result == null) {
                    result = urlLookup(matchingEntry);
                }
            }
            return result;
        }
        public Source resolve(String href, String base)
            throws TransformerException {
            SAXSource result = null;
            InputSource source = null;
            ResourceLocation matchingEntry = findMatchingEntry(href);
            if (matchingEntry != null) {
                log("Matching catalog entry found for uri: '"
                    + matchingEntry.getPublicId() + "' location: '"
                    + matchingEntry.getLocation() + "'",
                    Project.MSG_DEBUG);
                ResourceLocation entryCopy = matchingEntry;
                if (base != null) {
                    try {
                        URL baseURL = new URL(base);
                        entryCopy = new ResourceLocation();
                        entryCopy.setBase(baseURL);
                    } catch (MalformedURLException ex) {
                    }
                }
                entryCopy.setPublicId(matchingEntry.getPublicId());
                entryCopy.setLocation(matchingEntry.getLocation());
                source = filesystemLookup(entryCopy);
                if (source == null) {
                    source = classpathLookup(entryCopy);
                }
                if (source == null) {
                    source = urlLookup(entryCopy);
                }
                if (source != null) {
                    result = new SAXSource(source);
                }
            }
            return result;
        }
    }
    private class ExternalResolver implements CatalogResolver {
        private Method setXMLCatalog = null;
        private Method parseCatalog = null;
        private Method resolveEntity = null;
        private Method resolve = null;
        private Object resolverImpl = null;
        private boolean externalCatalogsProcessed = false;
        public ExternalResolver(Class resolverImplClass,
                              Object resolverImpl) {
            this.resolverImpl = resolverImpl;
            try {
                setXMLCatalog =
                    resolverImplClass.getMethod("setXMLCatalog",
                                                new Class[] {XMLCatalog.class});
                parseCatalog =
                    resolverImplClass.getMethod("parseCatalog",
                                                new Class[] {String.class});
                resolveEntity =
                    resolverImplClass.getMethod("resolveEntity",
                                                new Class[] {String.class, String.class});
                resolve =
                    resolverImplClass.getMethod("resolve",
                                                new Class[] {String.class, String.class});
            } catch (NoSuchMethodException ex) {
                throw new BuildException(ex);
            }
            log("Apache resolver library found, xml-commons resolver will be used",
                Project.MSG_VERBOSE);
        }
        public InputSource resolveEntity(String publicId,
                                         String systemId) {
            InputSource result = null;
            processExternalCatalogs();
            ResourceLocation matchingEntry = findMatchingEntry(publicId);
            if (matchingEntry != null) {
                log("Matching catalog entry found for publicId: '"
                    + matchingEntry.getPublicId() + "' location: '"
                    + matchingEntry.getLocation() + "'",
                    Project.MSG_DEBUG);
                result = filesystemLookup(matchingEntry);
                if (result == null) {
                    result = classpathLookup(matchingEntry);
                }
                if (result == null) {
                    try {
                        result =
                            (InputSource) resolveEntity.invoke(resolverImpl,
                                                              new Object[] {publicId, systemId});
                    } catch (Exception ex) {
                        throw new BuildException(ex);
                    }
                }
            } else {
                try {
                    result =
                        (InputSource) resolveEntity.invoke(resolverImpl,
                                                          new Object[] {publicId, systemId});
                } catch (Exception ex) {
                    throw new BuildException(ex);
                }
            }
            return result;
        }
        public Source resolve(String href, String base)
            throws TransformerException {
            SAXSource result = null;
            InputSource source = null;
            processExternalCatalogs();
            ResourceLocation matchingEntry = findMatchingEntry(href);
            if (matchingEntry != null) {
                log("Matching catalog entry found for uri: '"
                    + matchingEntry.getPublicId() + "' location: '"
                    + matchingEntry.getLocation() + "'",
                    Project.MSG_DEBUG);
                ResourceLocation entryCopy = matchingEntry;
                if (base != null) {
                    try {
                        URL baseURL = new URL(base);
                        entryCopy = new ResourceLocation();
                        entryCopy.setBase(baseURL);
                    } catch (MalformedURLException ex) {
                    }
                }
                entryCopy.setPublicId(matchingEntry.getPublicId());
                entryCopy.setLocation(matchingEntry.getLocation());
                source = filesystemLookup(entryCopy);
                if (source == null) {
                    source = classpathLookup(entryCopy);
                }
                if (source != null) {
                    result = new SAXSource(source);
                } else {
                    try {
                        result =
                            (SAXSource) resolve.invoke(resolverImpl,
                                                      new Object[] {href, base});
                    } catch (Exception ex) {
                        throw new BuildException(ex);
                    }
                }
            } else {
                try {
                    result =
                        (SAXSource) resolve.invoke(resolverImpl,
                                                  new Object[] {href, base});
                } catch (Exception ex) {
                    throw new BuildException(ex);
                }
            }
            return result;
        }
        private void processExternalCatalogs() {
            if (!externalCatalogsProcessed) {
                try {
                    setXMLCatalog.invoke(resolverImpl,
                                         new Object[] {XMLCatalog.this});
                } catch (Exception ex) {
                    throw new BuildException(ex);
                }
                Path catPath = getCatalogPath();
                if (catPath != null) {
                    log("Using catalogpath '" + getCatalogPath() + "'",
                        Project.MSG_DEBUG);
                    String[] catPathList = getCatalogPath().list();
                    for (int i = 0; i < catPathList.length; i++) {
                        File catFile = new File(catPathList[i]);
                        log("Parsing " + catFile, Project.MSG_DEBUG);
                        try {
                            parseCatalog.invoke(resolverImpl,
                                    new Object[] {catFile.getPath()});
                        } catch (Exception ex) {
                            throw new BuildException(ex);
                        }
                    }
                }
            }
            externalCatalogsProcessed = true;
        }
    }
} 
