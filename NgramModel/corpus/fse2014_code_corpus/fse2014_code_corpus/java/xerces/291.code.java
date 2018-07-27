package org.apache.xerces.impl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.xerces.impl.io.ASCIIReader;
import org.apache.xerces.impl.io.Latin1Reader;
import org.apache.xerces.impl.io.UCSReader;
import org.apache.xerces.impl.io.UTF16Reader;
import org.apache.xerces.impl.io.UTF8Reader;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.EncodingMap;
import org.apache.xerces.util.HTTPInputSource;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLEntityDescriptionImpl;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
public class XMLEntityManager
    implements XMLComponent, XMLEntityResolver {
    public static final int DEFAULT_BUFFER_SIZE = 2048; 
    public static final int DEFAULT_XMLDECL_BUFFER_SIZE = 64;
    public static final int DEFAULT_INTERNAL_BUFFER_SIZE = 512;
    protected static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    protected static final String EXTERNAL_GENERAL_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;
    protected static final String EXTERNAL_PARAMETER_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS =
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF =
    Constants.XERCES_FEATURE_PREFIX +Constants.WARN_ON_DUPLICATE_ENTITYDEF_FEATURE;
    protected static final String STANDARD_URI_CONFORMANT =
    Constants.XERCES_FEATURE_PREFIX +Constants.STANDARD_URI_CONFORMANT_FEATURE;
	protected static final String PARSER_SETTINGS = 
		Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;	
    protected static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String BUFFER_SIZE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.BUFFER_SIZE_PROPERTY;
    protected static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private static final String[] RECOGNIZED_FEATURES = {
        VALIDATION,
        EXTERNAL_GENERAL_ENTITIES,
        EXTERNAL_PARAMETER_ENTITIES,
        ALLOW_JAVA_ENCODINGS,
        WARN_ON_DUPLICATE_ENTITYDEF,
        STANDARD_URI_CONFORMANT
    };
    private static final Boolean[] FEATURE_DEFAULTS = {
        null,
        Boolean.TRUE,
        Boolean.TRUE,
        Boolean.FALSE,
        Boolean.FALSE,
        Boolean.FALSE
    };
    private static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,
        ERROR_REPORTER,
        ENTITY_RESOLVER,
        VALIDATION_MANAGER,
        BUFFER_SIZE,
        SECURITY_MANAGER,
    };
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
        null,
        null,
        null,
        new Integer(DEFAULT_BUFFER_SIZE),
        null,
    };
    private static final String XMLEntity = "[xml]".intern();
    private static final String DTDEntity = "[dtd]".intern();
    private static final boolean DEBUG_BUFFER = false;
    private static final boolean DEBUG_ENTITIES = false;
    private static final boolean DEBUG_ENCODINGS = false;
    private static final boolean DEBUG_RESOLVER = false;
    protected boolean fValidation;
    protected boolean fExternalGeneralEntities = true;
    protected boolean fExternalParameterEntities = true;
    protected boolean fAllowJavaEncodings;
    protected boolean fWarnDuplicateEntityDef;
    protected boolean fStrictURI;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityResolver fEntityResolver;
    protected ValidationManager fValidationManager;
    protected int fBufferSize = DEFAULT_BUFFER_SIZE;
    protected SecurityManager fSecurityManager = null;
    protected boolean fStandalone;
    protected boolean fHasPEReferences;
    protected boolean fInExternalSubset = false;
    protected XMLEntityHandler fEntityHandler;
    protected XMLEntityScanner fEntityScanner;
    protected XMLEntityScanner fXML10EntityScanner;
    protected XMLEntityScanner fXML11EntityScanner;
    protected int fEntityExpansionLimit = 0;
    protected int fEntityExpansionCount = 0;
    protected final Hashtable fEntities = new Hashtable();
    protected final Stack fEntityStack = new Stack();
    protected ScannedEntity fCurrentEntity;
    protected Hashtable fDeclaredEntities;
    private final XMLResourceIdentifierImpl fResourceIdentifier = new XMLResourceIdentifierImpl();
    private final Augmentations fEntityAugs = new AugmentationsImpl();
    private final ByteBufferPool fSmallByteBufferPool = new ByteBufferPool(fBufferSize);
    private final ByteBufferPool fLargeByteBufferPool = new ByteBufferPool(fBufferSize << 1);
    private byte[] fTempByteBuffer = null;
    private final CharacterBufferPool fCharacterBufferPool = new CharacterBufferPool(fBufferSize, DEFAULT_INTERNAL_BUFFER_SIZE);
    public XMLEntityManager() {
        this(null);
    } 
    public XMLEntityManager(XMLEntityManager entityManager) {
        fDeclaredEntities = entityManager != null
                          ? entityManager.getDeclaredEntities() : null;
        setScannerVersion(Constants.XML_VERSION_1_0);
    } 
    public void setStandalone(boolean standalone) {
        fStandalone = standalone;
    } 
    public boolean isStandalone() {
        return fStandalone;
    } 
    final void notifyHasPEReferences() {
        fHasPEReferences = true;
    } 
    final boolean hasPEReferences() {
        return fHasPEReferences;
    } 
    public void setEntityHandler(XMLEntityHandler entityHandler) {
        fEntityHandler = entityHandler;
    } 
    public XMLResourceIdentifier getCurrentResourceIdentifier() {
        return fResourceIdentifier;
    }
    public ScannedEntity getCurrentEntity() {
        return fCurrentEntity;
    }
    public void addInternalEntity(String name, String text) {
        if (!fEntities.containsKey(name)) {
            Entity entity = new InternalEntity(name, text, fInExternalSubset);
            fEntities.put(name, entity);
        }
        else{
            if(fWarnDuplicateEntityDef){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                             "MSG_DUPLICATE_ENTITY_DEFINITION",
                                             new Object[]{ name },
                                             XMLErrorReporter.SEVERITY_WARNING );
            }
        }
    } 
    public void addExternalEntity(String name,
                                  String publicId, String literalSystemId,
                                  String baseSystemId) throws IOException {
        if (!fEntities.containsKey(name)) {
            if (baseSystemId == null) {
                int size = fEntityStack.size();
                if (size == 0 && fCurrentEntity != null && fCurrentEntity.entityLocation != null) {
                    baseSystemId = fCurrentEntity.entityLocation.getExpandedSystemId();
                }
                for (int i = size - 1; i >= 0 ; i--) {
                    ScannedEntity externalEntity =
                        (ScannedEntity)fEntityStack.elementAt(i);
                    if (externalEntity.entityLocation != null && externalEntity.entityLocation.getExpandedSystemId() != null) {
                        baseSystemId = externalEntity.entityLocation.getExpandedSystemId();
                        break;
                    }
                }
            }
            Entity entity = new ExternalEntity(name,
                new XMLEntityDescriptionImpl(name, publicId, literalSystemId, baseSystemId, 
                expandSystemId(literalSystemId, baseSystemId, false)), null, fInExternalSubset);
            fEntities.put(name, entity);
        }
        else{
            if(fWarnDuplicateEntityDef){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                             "MSG_DUPLICATE_ENTITY_DEFINITION",
                                             new Object[]{ name },
                                             XMLErrorReporter.SEVERITY_WARNING );
            }
        }
    } 
    public boolean isExternalEntity(String entityName) {
        Entity entity = (Entity)fEntities.get(entityName);
        if (entity == null) {
            return false;
        }
        return entity.isExternal();
    }
    public boolean isEntityDeclInExternalSubset(String entityName) {
        Entity entity = (Entity)fEntities.get(entityName);
        if (entity == null) {
            return false;
        }
        return entity.isEntityDeclInExternalSubset();
    }
    public void addUnparsedEntity(String name,
                                  String publicId, String systemId,
                                  String baseSystemId, String notation) {
        if (!fEntities.containsKey(name)) {
            Entity entity = new ExternalEntity(name, 
                new XMLEntityDescriptionImpl(name, publicId, systemId, baseSystemId, null), 
                notation, fInExternalSubset);
            fEntities.put(name, entity);
        }
        else{
            if(fWarnDuplicateEntityDef){
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                             "MSG_DUPLICATE_ENTITY_DEFINITION",
                                             new Object[]{ name },
                                             XMLErrorReporter.SEVERITY_WARNING );
            }
        }
    } 
    public boolean isUnparsedEntity(String entityName) {
        Entity entity = (Entity)fEntities.get(entityName);
        if (entity == null) {
            return false;
        }
        return entity.isUnparsed();
    }
    public boolean isDeclaredEntity(String entityName) {
        Entity entity = (Entity)fEntities.get(entityName);
        return entity != null;
    }
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws IOException, XNIException {
        if(resourceIdentifier == null ) return null;
        String publicId = resourceIdentifier.getPublicId();
        String literalSystemId = resourceIdentifier.getLiteralSystemId();
        String baseSystemId = resourceIdentifier.getBaseSystemId();
        String expandedSystemId = resourceIdentifier.getExpandedSystemId();
        boolean needExpand = (expandedSystemId == null);
        if (baseSystemId == null && fCurrentEntity != null && fCurrentEntity.entityLocation != null) {
            baseSystemId = fCurrentEntity.entityLocation.getExpandedSystemId();
            if (baseSystemId != null)
                needExpand = true;
        }
        XMLInputSource xmlInputSource = null;
        if (fEntityResolver != null) {
            if (needExpand) {
                expandedSystemId = expandSystemId(literalSystemId, baseSystemId, false);
            }
            resourceIdentifier.setBaseSystemId(baseSystemId);
            resourceIdentifier.setExpandedSystemId(expandedSystemId);
            xmlInputSource = fEntityResolver.resolveEntity(resourceIdentifier);
        }
        if (xmlInputSource == null) {
            xmlInputSource = new XMLInputSource(publicId, literalSystemId, baseSystemId);
        }
        if (DEBUG_RESOLVER) {
            System.err.println("XMLEntityManager.resolveEntity(" + publicId + ")");
            System.err.println(" = " + xmlInputSource);
        }
        return xmlInputSource;
    } 
    public void startEntity(String entityName, boolean literal)
        throws IOException, XNIException {
        Entity entity = (Entity)fEntities.get(entityName);
        if (entity == null) {
            if (fEntityHandler != null) {
                String encoding = null;
                fResourceIdentifier.clear();
                fEntityAugs.removeAllItems();
                fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                fEntityHandler.startEntity(entityName, fResourceIdentifier, encoding, fEntityAugs);
                fEntityAugs.removeAllItems();
                fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                fEntityHandler.endEntity(entityName, fEntityAugs);
            }
            return;
        }
        boolean external = entity.isExternal();
        if (external && (fValidationManager == null || !fValidationManager.isCachedDTD())) {
            boolean unparsed = entity.isUnparsed();
            boolean parameter = entityName.startsWith("%");
            boolean general = !parameter;
            if (unparsed || (general && !fExternalGeneralEntities) ||
                (parameter && !fExternalParameterEntities)) {
                if (fEntityHandler != null) {
                    fResourceIdentifier.clear();
                    final String encoding = null;
                    ExternalEntity externalEntity = (ExternalEntity)entity;
                    String extLitSysId = (externalEntity.entityLocation != null ? externalEntity.entityLocation.getLiteralSystemId() : null);
                    String extBaseSysId = (externalEntity.entityLocation != null ? externalEntity.entityLocation.getBaseSystemId() : null);
                    String expandedSystemId = expandSystemId(extLitSysId, extBaseSysId, false);
                    fResourceIdentifier.setValues(
                            (externalEntity.entityLocation != null ? externalEntity.entityLocation.getPublicId() : null),
                            extLitSysId, extBaseSysId, expandedSystemId);
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                    fEntityHandler.startEntity(entityName, fResourceIdentifier, encoding, fEntityAugs);
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                    fEntityHandler.endEntity(entityName, fEntityAugs);
                }
                return;
            }
        }
        int size = fEntityStack.size();
        for (int i = size; i >= 0; i--) {
            Entity activeEntity = i == size
                                ? fCurrentEntity
                                : (Entity)fEntityStack.elementAt(i);
            if (activeEntity.name == entityName) {
                StringBuffer path = new StringBuffer(entityName);
                for (int j = i + 1; j < size; j++) {
                    activeEntity = (Entity)fEntityStack.elementAt(j);
                    path.append(" -> ");
                    path.append(activeEntity.name);
                }
                path.append(" -> ");
                path.append(fCurrentEntity.name);
                path.append(" -> ");
                path.append(entityName);
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                           "RecursiveReference",
                                           new Object[] { entityName, path.toString() },
                                           XMLErrorReporter.SEVERITY_FATAL_ERROR);
                if (fEntityHandler != null) {
                    fResourceIdentifier.clear();
                    final String encoding = null;
                    if (external) {
                        ExternalEntity externalEntity = (ExternalEntity)entity;
                        String extLitSysId = (externalEntity.entityLocation != null ? externalEntity.entityLocation.getLiteralSystemId() : null);
                        String extBaseSysId = (externalEntity.entityLocation != null ? externalEntity.entityLocation.getBaseSystemId() : null);
                        String expandedSystemId = expandSystemId(extLitSysId, extBaseSysId, false);
                        fResourceIdentifier.setValues(
                                (externalEntity.entityLocation != null ? externalEntity.entityLocation.getPublicId() : null),
                                extLitSysId, extBaseSysId, expandedSystemId);
                    }
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                    fEntityHandler.startEntity(entityName, fResourceIdentifier, encoding, fEntityAugs);
                    fEntityAugs.removeAllItems();
                    fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                    fEntityHandler.endEntity(entityName, fEntityAugs);
                }
                return;
            }
        }
        XMLInputSource xmlInputSource = null;
        if (external) {
            ExternalEntity externalEntity = (ExternalEntity)entity;
            xmlInputSource = resolveEntity(externalEntity.entityLocation);
        }
        else {
            InternalEntity internalEntity = (InternalEntity)entity;
            Reader reader = new StringReader(internalEntity.text);
            xmlInputSource = new XMLInputSource(null, null, null, reader, null);
        }
        startEntity(entityName, xmlInputSource, literal, external);
    } 
    public void startDocumentEntity(XMLInputSource xmlInputSource)
        throws IOException, XNIException {
        startEntity(XMLEntity, xmlInputSource, false, true);
    } 
    public void startDTDEntity(XMLInputSource xmlInputSource)
        throws IOException, XNIException {
        startEntity(DTDEntity, xmlInputSource, false, true);
    } 
    public void startExternalSubset() {
        fInExternalSubset = true;
    }
    public void endExternalSubset() {
        fInExternalSubset = false;
    }
    public void startEntity(String name,
                            XMLInputSource xmlInputSource,
                            boolean literal, boolean isExternal)
        throws IOException, XNIException {
        String encoding = setupCurrentEntity(name, xmlInputSource, literal, isExternal);
        if( fSecurityManager != null && fEntityExpansionCount++ > fEntityExpansionLimit ){
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                             "EntityExpansionLimitExceeded",
                                             new Object[]{new Integer(fEntityExpansionLimit) },
                                             XMLErrorReporter.SEVERITY_FATAL_ERROR );
            fEntityExpansionCount = 0;
        }
        if (fEntityHandler != null) {
            fEntityHandler.startEntity(name, fResourceIdentifier, encoding, null);
        }
    } 
    public String setupCurrentEntity(String name, XMLInputSource xmlInputSource,
                boolean literal, boolean isExternal)
            throws IOException, XNIException {
        final String publicId = xmlInputSource.getPublicId();
        String literalSystemId = xmlInputSource.getSystemId();
        String baseSystemId = xmlInputSource.getBaseSystemId();
        String encoding = xmlInputSource.getEncoding();
        final boolean encodingExternallySpecified = (encoding != null);
        Boolean isBigEndian = null;
        fTempByteBuffer = null;
        InputStream stream = null;
        Reader reader = xmlInputSource.getCharacterStream();
        String expandedSystemId = expandSystemId(literalSystemId, baseSystemId, fStrictURI);
        if (baseSystemId == null) {
            baseSystemId = expandedSystemId;
        }
        if (reader == null) {
            stream = xmlInputSource.getByteStream();
            if (stream == null) {
                URL location = new URL(expandedSystemId);
                URLConnection connect = location.openConnection();
                if (!(connect instanceof HttpURLConnection)) {
                    stream = connect.getInputStream();
                }
                else {
                    boolean followRedirects = true;
                    if (xmlInputSource instanceof HTTPInputSource) {
                        final HttpURLConnection urlConnection = (HttpURLConnection) connect;
                        final HTTPInputSource httpInputSource = (HTTPInputSource) xmlInputSource;
                        Iterator propIter = httpInputSource.getHTTPRequestProperties();
                        while (propIter.hasNext()) {
                            Map.Entry entry = (Map.Entry) propIter.next();
                            urlConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                        }
                        followRedirects = httpInputSource.getFollowHTTPRedirects();
                        if (!followRedirects) {
                            urlConnection.setInstanceFollowRedirects(followRedirects);
                        }
                    }
                    stream = connect.getInputStream();
                    if (followRedirects) {
                        String redirect = connect.getURL().toString();
                        if (!redirect.equals(expandedSystemId)) {
                            literalSystemId = redirect;
                            expandedSystemId = redirect;
                        }
                    }
                }
            }
            RewindableInputStream rewindableStream = new RewindableInputStream(stream);
            stream = rewindableStream;
            if (encoding == null) {
                final byte[] b4 = new byte[4];
                int count = 0;
                for (; count<4; count++ ) {
                    b4[count] = (byte)rewindableStream.readAndBuffer();
                }
                if (count == 4) {
                    EncodingInfo info = getEncodingInfo(b4, count);
                    encoding = info.encoding;
                    isBigEndian = info.isBigEndian;
                    stream.reset();
                    if (info.hasBOM) {
                        if (encoding == "UTF-8") {
                            stream.skip(3);
                        }
                        else if (encoding == "UTF-16") {
                            stream.skip(2);
                        }
                    }
                    reader = createReader(stream, encoding, isBigEndian);
                }
                else {
                    reader = createReader(stream, encoding, isBigEndian);
                }
            }
            else {
                encoding = encoding.toUpperCase(Locale.ENGLISH);
                if (encoding.equals("UTF-8")) {
                    final int[] b3 = new int[3];
                    int count = 0;
                    for (; count < 3; ++count) {
                        b3[count] = rewindableStream.readAndBuffer();
                        if (b3[count] == -1)
                            break;
                    }
                    if (count == 3) {
                        if (b3[0] != 0xEF || b3[1] != 0xBB || b3[2] != 0xBF) {
                            stream.reset();
                        }
                    }
                    else {
                        stream.reset();
                    }
                    reader = createReader(stream, "UTF-8", isBigEndian);
                }
                else if (encoding.equals("UTF-16")) {
                    final int[] b4 = new int[4];
                    int count = 0;
                    for (; count < 4; ++count) {
                        b4[count] = rewindableStream.readAndBuffer();
                        if (b4[count] == -1)
                            break;
                    }
                    stream.reset();
                    if (count >= 2) {
                        final int b0 = b4[0];
                        final int b1 = b4[1];
                        if (b0 == 0xFE && b1 == 0xFF) {
                            isBigEndian = Boolean.TRUE;
                            stream.skip(2);
                        }
                        else if (b0 == 0xFF && b1 == 0xFE) {
                            isBigEndian = Boolean.FALSE;
                            stream.skip(2);
                        }
                        else if (count == 4) {
                            final int b2 = b4[2];
                            final int b3 = b4[3];
                            if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x3F) {
                                isBigEndian = Boolean.TRUE;
                            }
                            if (b0 == 0x3C && b1 == 0x00 && b2 == 0x3F && b3 == 0x00) {
                                isBigEndian = Boolean.FALSE;
                            }
                        }
                    }
                    reader = createReader(stream, "UTF-16", isBigEndian);
                }
                else if (encoding.equals("ISO-10646-UCS-4")) {
                    final int[] b4 = new int[4];
                    int count = 0;
                    for (; count < 4; ++count) {
                        b4[count] = rewindableStream.readAndBuffer();
                        if (b4[count] == -1)
                            break;
                    }
                    stream.reset();
                    if (count == 4) {
                        if (b4[0] == 0x00 && b4[1] == 0x00 && b4[2] == 0x00 && b4[3] == 0x3C) {
                            isBigEndian = Boolean.TRUE;
                        }
                        else if (b4[0] == 0x3C && b4[1] == 0x00 && b4[2] == 0x00 && b4[3] == 0x00) {
                            isBigEndian = Boolean.FALSE;
                        }
                    }
                    reader = createReader(stream, encoding, isBigEndian);
                }
                else if (encoding.equals("ISO-10646-UCS-2")) {
                    final int[] b4 = new int[4];
                    int count = 0;
                    for (; count < 4; ++count) {
                        b4[count] = rewindableStream.readAndBuffer();
                        if (b4[count] == -1)
                            break;
                    }
                    stream.reset();
                    if (count == 4) {
                        if (b4[0] == 0x00 && b4[1] == 0x3C && b4[2] == 0x00 && b4[3] == 0x3F) {
                            isBigEndian = Boolean.TRUE;
                        }
                        else if (b4[0] == 0x3C && b4[1] == 0x00 && b4[2] == 0x3F && b4[3] == 0x00) {
                            isBigEndian = Boolean.FALSE;
                        }
                    }
                    reader = createReader(stream, encoding, isBigEndian);
                }
                else {
                    reader = createReader(stream, encoding, isBigEndian);
                }
            }
            if (DEBUG_ENCODINGS) {
                System.out.println("$$$ no longer wrapping reader in OneCharReader");
            }
        }
        fReaderStack.push(reader);
        if (fCurrentEntity != null) {
            fEntityStack.push(fCurrentEntity);
        }
        fCurrentEntity = new ScannedEntity(name,
                new XMLResourceIdentifierImpl(publicId, literalSystemId, baseSystemId, expandedSystemId),
                stream, reader, fTempByteBuffer, encoding, literal, false, isExternal);
		fCurrentEntity.setEncodingExternallySpecified(encodingExternallySpecified);
        fEntityScanner.setCurrentEntity(fCurrentEntity);
        fResourceIdentifier.setValues(publicId, literalSystemId, baseSystemId, expandedSystemId);
        return encoding;
    } 
    public void setScannerVersion(short version) {
        if(version == Constants.XML_VERSION_1_0) {
            if(fXML10EntityScanner == null) {
                fXML10EntityScanner = new XMLEntityScanner();
            }
			fXML10EntityScanner.reset(fSymbolTable, this, fErrorReporter);
            fEntityScanner = fXML10EntityScanner;
            fEntityScanner.setCurrentEntity(fCurrentEntity);
        } else {
            if(fXML11EntityScanner == null) {
                fXML11EntityScanner = new XML11EntityScanner();
            }
			fXML11EntityScanner.reset(fSymbolTable, this, fErrorReporter);
            fEntityScanner = fXML11EntityScanner;
            fEntityScanner.setCurrentEntity(fCurrentEntity);
        }
    } 
    public XMLEntityScanner getEntityScanner() {
        if(fEntityScanner == null) {
            if(fXML10EntityScanner == null) {
                fXML10EntityScanner = new XMLEntityScanner();
            }
            fXML10EntityScanner.reset(fSymbolTable, this, fErrorReporter);
            fEntityScanner = fXML10EntityScanner;
        }
        return fEntityScanner;
    } 
    protected Stack fReaderStack = new Stack();
    public void closeReaders() {
        for (int i = fReaderStack.size()-1; i >= 0; i--) {
            try {
                ((Reader)fReaderStack.pop()).close();
            } catch (IOException e) {
            }
        }
    }
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {
		boolean parser_settings;
		try {
				parser_settings = componentManager.getFeature(PARSER_SETTINGS);
		} catch (XMLConfigurationException e) {
				parser_settings = true;
		}
		if (!parser_settings) {
			reset();
			return;
		}
        try {
            fValidation = componentManager.getFeature(VALIDATION);
        }
        catch (XMLConfigurationException e) {
            fValidation = false;
        }
        try {
            fExternalGeneralEntities = componentManager.getFeature(EXTERNAL_GENERAL_ENTITIES);
        }
        catch (XMLConfigurationException e) {
            fExternalGeneralEntities = true;
        }
        try {
            fExternalParameterEntities = componentManager.getFeature(EXTERNAL_PARAMETER_ENTITIES);
        }
        catch (XMLConfigurationException e) {
            fExternalParameterEntities = true;
        }
        try {
            fAllowJavaEncodings = componentManager.getFeature(ALLOW_JAVA_ENCODINGS);
        }
        catch (XMLConfigurationException e) {
            fAllowJavaEncodings = false;
        }
        try {
            fWarnDuplicateEntityDef = componentManager.getFeature(WARN_ON_DUPLICATE_ENTITYDEF);
        }
        catch (XMLConfigurationException e) {
            fWarnDuplicateEntityDef = false;
        }
        try {
            fStrictURI = componentManager.getFeature(STANDARD_URI_CONFORMANT);
        }
        catch (XMLConfigurationException e) {
            fStrictURI = false;
        }
        fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        try {
            fEntityResolver = (XMLEntityResolver)componentManager.getProperty(ENTITY_RESOLVER);
        }
        catch (XMLConfigurationException e) {
            fEntityResolver = null;
        }
        try {
            fValidationManager = (ValidationManager)componentManager.getProperty(VALIDATION_MANAGER);
        }
        catch (XMLConfigurationException e) {
            fValidationManager = null;
        }
        try {
            fSecurityManager = (SecurityManager)componentManager.getProperty(SECURITY_MANAGER);
        }
        catch (XMLConfigurationException e) {
            fSecurityManager = null;
        }
        reset();
    } 
    public void reset() {
        fEntityExpansionLimit = (fSecurityManager != null)?fSecurityManager.getEntityExpansionLimit():0;
        fStandalone = false;
        fHasPEReferences = false;
        fEntities.clear();
        fEntityStack.removeAllElements();
        fEntityExpansionCount = 0;
        fCurrentEntity = null;
        if(fXML10EntityScanner != null){ 
            fXML10EntityScanner.reset(fSymbolTable, this, fErrorReporter);
        }
        if(fXML11EntityScanner != null) {
            fXML11EntityScanner.reset(fSymbolTable, this, fErrorReporter);
        }
        if (DEBUG_ENTITIES) {
            addInternalEntity("text", "Hello, World.");
            addInternalEntity("empty-element", "<foo/>");
            addInternalEntity("balanced-element", "<foo></foo>");
            addInternalEntity("balanced-element-with-text", "<foo>Hello, World</foo>");
            addInternalEntity("balanced-element-with-entity", "<foo>&text;</foo>");
            addInternalEntity("unbalanced-entity", "<foo>");
            addInternalEntity("recursive-entity", "<foo>&recursive-entity2;</foo>");
            addInternalEntity("recursive-entity2", "<bar>&recursive-entity3;</bar>");
            addInternalEntity("recursive-entity3", "<baz>&recursive-entity;</baz>");
            try {
                addExternalEntity("external-text", null, "external-text.ent", "test/external-text.xml");
                addExternalEntity("external-balanced-element", null, "external-balanced-element.ent", "test/external-balanced-element.xml");
                addExternalEntity("one", null, "ent/one.ent", "test/external-entity.xml");
                addExternalEntity("two", null, "ent/two.ent", "test/ent/one.xml");
            }
            catch (IOException ex) {
            }
        }
        if (fDeclaredEntities != null) {
            Iterator entries = fDeclaredEntities.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                fEntities.put(key, value);
            }
        }
        fEntityHandler = null;
    } 
    public String[] getRecognizedFeatures() {
        return (String[])(RECOGNIZED_FEATURES.clone());
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.ALLOW_JAVA_ENCODINGS_FEATURE.length() && 
                featureId.endsWith(Constants.ALLOW_JAVA_ENCODINGS_FEATURE)) {
                fAllowJavaEncodings = state;
            }
        }
    } 
    public String[] getRecognizedProperties() {
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } 
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.SYMBOL_TABLE_PROPERTY.length() && 
                propertyId.endsWith(Constants.SYMBOL_TABLE_PROPERTY)) {
                fSymbolTable = (SymbolTable)value;
                return;
            }
            if (suffixLength == Constants.ERROR_REPORTER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ERROR_REPORTER_PROPERTY)) {
                fErrorReporter = (XMLErrorReporter)value;
                return;
            }
            if (suffixLength == Constants.ENTITY_RESOLVER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ENTITY_RESOLVER_PROPERTY)) {
                fEntityResolver = (XMLEntityResolver)value;
                return;
            }
            if (suffixLength == Constants.BUFFER_SIZE_PROPERTY.length() && 
                propertyId.endsWith(Constants.BUFFER_SIZE_PROPERTY)) {
                Integer bufferSize = (Integer)value;
                if (bufferSize != null &&
                    bufferSize.intValue() > DEFAULT_XMLDECL_BUFFER_SIZE) {
                    fBufferSize = bufferSize.intValue();
                    fEntityScanner.setBufferSize(fBufferSize);
                    fSmallByteBufferPool.setBufferSize(fBufferSize);
                    fLargeByteBufferPool.setBufferSize(fBufferSize << 1);
                    fCharacterBufferPool.setExternalBufferSize(fBufferSize);
                }
            }
            if (suffixLength == Constants.SECURITY_MANAGER_PROPERTY.length() && 
                propertyId.endsWith(Constants.SECURITY_MANAGER_PROPERTY)) {
                fSecurityManager = (SecurityManager)value; 
                fEntityExpansionLimit = (fSecurityManager != null)?fSecurityManager.getEntityExpansionLimit():0;
            }
        }
    } 
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } 
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } 
    private static String gUserDir;
    private static URI gUserDirURI;
    private static final boolean gNeedEscaping[] = new boolean[128];
    private static final char gAfterEscaping1[] = new char[128];
    private static final char gAfterEscaping2[] = new char[128];
    private static final char[] gHexChs = {'0', '1', '2', '3', '4', '5', '6', '7',
                                           '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static {
        for (int i = 0; i <= 0x1f; i++) {
            gNeedEscaping[i] = true;
            gAfterEscaping1[i] = gHexChs[i >> 4];
            gAfterEscaping2[i] = gHexChs[i & 0xf];
        }
        gNeedEscaping[0x7f] = true;
        gAfterEscaping1[0x7f] = '7';
        gAfterEscaping2[0x7f] = 'F';
        char[] escChs = {' ', '<', '>', '#', '%', '"', '{', '}',
                         '|', '\\', '^', '~', '[', ']', '`'};
        int len = escChs.length;
        char ch;
        for (int i = 0; i < len; i++) {
            ch = escChs[i];
            gNeedEscaping[ch] = true;
            gAfterEscaping1[ch] = gHexChs[ch >> 4];
            gAfterEscaping2[ch] = gHexChs[ch & 0xf];
        }
    }
    private static PrivilegedAction GET_USER_DIR_SYSTEM_PROPERTY = new PrivilegedAction() {
        public Object run() {
            return System.getProperty("user.dir");
        }
    };
    private static synchronized URI getUserDir() throws URI.MalformedURIException {
        String userDir = "";
        try {
            userDir = (String) AccessController.doPrivileged(GET_USER_DIR_SYSTEM_PROPERTY);
        }
        catch (SecurityException se) {}
        if (userDir.length() == 0) 
            return new URI("file", "", "", null, null);
        if (gUserDirURI != null && userDir.equals(gUserDir)) {
            return gUserDirURI;
        }
        gUserDir = userDir;
        char separator = java.io.File.separatorChar;
        userDir = userDir.replace(separator, '/');
        int len = userDir.length(), ch;
        StringBuffer buffer = new StringBuffer(len*3);
        if (len >= 2 && userDir.charAt(1) == ':') {
            ch = Character.toUpperCase(userDir.charAt(0));
            if (ch >= 'A' && ch <= 'Z') {
                buffer.append('/');
            }
        }
        int i = 0;
        for (; i < len; i++) {
            ch = userDir.charAt(i);
            if (ch >= 128)
                break;
            if (gNeedEscaping[ch]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
            }
            else {
                buffer.append((char)ch);
            }
        }
        if (i < len) {
            byte[] bytes = null;
            byte b;
            try {
                bytes = userDir.substring(i).getBytes("UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                return new URI("file", "", userDir, null, null);
            }
            len = bytes.length;
            for (i = 0; i < len; i++) {
                b = bytes[i];
                if (b < 0) {
                    ch = b + 256;
                    buffer.append('%');
                    buffer.append(gHexChs[ch >> 4]);
                    buffer.append(gHexChs[ch & 0xf]);
                }
                else if (gNeedEscaping[b]) {
                    buffer.append('%');
                    buffer.append(gAfterEscaping1[b]);
                    buffer.append(gAfterEscaping2[b]);
                }
                else {
                    buffer.append((char)b);
                }
            }
        }
        if (!userDir.endsWith("/"))
            buffer.append('/');
        gUserDirURI = new URI("file", "", buffer.toString(), null, null);
        return gUserDirURI;
    }
    public static void absolutizeAgainstUserDir(URI uri) 
        throws URI.MalformedURIException {
        uri.absolutize(getUserDir());
    }
    public static String expandSystemId(String systemId, String baseSystemId,
                                        boolean strict)
            throws URI.MalformedURIException {
        if (systemId == null) {
            return null;
        }
        if (strict) {
            return expandSystemIdStrictOn(systemId, baseSystemId);
        }
        try {
            return expandSystemIdStrictOff(systemId, baseSystemId);
        }
        catch (URI.MalformedURIException e) {
        }
        if (systemId.length() == 0) {
            return systemId;
        }
        String id = fixURI(systemId);
        URI base = null;
        URI uri = null;
        try {
            if (baseSystemId == null || baseSystemId.length() == 0 ||
                baseSystemId.equals(systemId)) {
                base = getUserDir();
            }
            else {
                try {
                    base = new URI(fixURI(baseSystemId).trim());
                }
                catch (URI.MalformedURIException e) {
                    if (baseSystemId.indexOf(':') != -1) {
                        base = new URI("file", "", fixURI(baseSystemId).trim(), null, null);
                    }
                    else {
                        base = new URI(getUserDir(), fixURI(baseSystemId));
                    }
                }
             }
             uri = new URI(base, id.trim());
        }
        catch (Exception e) {
        }
        if (uri == null) {
            return systemId;
        }
        return uri.toString();
    } 
    private static String expandSystemIdStrictOn(String systemId, String baseSystemId)
        throws URI.MalformedURIException {
        URI systemURI = new URI(systemId, true);
        if (systemURI.isAbsoluteURI()) {
            return systemId;
        }
        URI baseURI = null;
        if (baseSystemId == null || baseSystemId.length() == 0) {
            baseURI = getUserDir();
        }
        else {
            baseURI = new URI(baseSystemId, true);
            if (!baseURI.isAbsoluteURI()) {
                baseURI.absolutize(getUserDir());
            }
        }
        systemURI.absolutize(baseURI);
        return systemURI.toString();
    } 
    private static String expandSystemIdStrictOff(String systemId, String baseSystemId)
        throws URI.MalformedURIException {
        URI systemURI = new URI(systemId, true);
        if (systemURI.isAbsoluteURI()) {
            if (systemURI.getScheme().length() > 1) {
                return systemId;
            }
            throw new URI.MalformedURIException();
        }
        URI baseURI = null;
        if (baseSystemId == null || baseSystemId.length() == 0) {
            baseURI = getUserDir();
        }
        else {
            baseURI = new URI(baseSystemId, true);
            if (!baseURI.isAbsoluteURI()) {
                baseURI.absolutize(getUserDir());
            }
        }
        systemURI.absolutize(baseURI);
        return systemURI.toString();
    } 
    public static OutputStream createOutputStream(String uri) throws IOException {
        final String expanded = XMLEntityManager.expandSystemId(uri, null, true);
        final URL url = new URL(expanded != null ? expanded : uri);
        OutputStream out = null;
        String protocol = url.getProtocol();
        String host = url.getHost();
        if (protocol.equals("file") 
                && (host == null || host.length() == 0 || host.equals("localhost"))) {
            File file = new File(getPathWithoutEscapes(url.getPath()));
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
            }
            out = new FileOutputStream(file);
        }
        else {
            URLConnection urlCon = url.openConnection();
            urlCon.setDoInput(false);
            urlCon.setDoOutput(true);
            urlCon.setUseCaches(false); 
            if (urlCon instanceof HttpURLConnection) {
                HttpURLConnection httpCon = (HttpURLConnection) urlCon;
                httpCon.setRequestMethod("PUT");
            }
            out = urlCon.getOutputStream();
        }
        return out;
    }
    private static String getPathWithoutEscapes(String origPath) {
        if (origPath != null && origPath.length() != 0 && origPath.indexOf('%') != -1) {
            StringTokenizer tokenizer = new StringTokenizer(origPath, "%");
            StringBuffer result = new StringBuffer(origPath.length());
            int size = tokenizer.countTokens();
            result.append(tokenizer.nextToken());
            for(int i = 1; i < size; ++i) {
                String token = tokenizer.nextToken();
                result.append((char)Integer.valueOf(token.substring(0, 2), 16).intValue());
                result.append(token.substring(2));
            }
            return result.toString();
        }
        return origPath;
    }
    void endEntity() throws XNIException {
        if (DEBUG_BUFFER) {
            System.out.print("(endEntity: ");
            print(fCurrentEntity);
            System.out.println();
        }
        if (fEntityHandler != null) {
            fEntityHandler.endEntity(fCurrentEntity.name, null);
        }
        try {
            fCurrentEntity.reader.close();
        }
        catch (IOException e) {
        }
        if (!fReaderStack.isEmpty()) {
            fReaderStack.pop();
        } 
        fCharacterBufferPool.returnBuffer(fCurrentEntity.fCharacterBuffer);
        if (fCurrentEntity.fByteBuffer != null) {
            if (fCurrentEntity.fByteBuffer.length == fBufferSize) {
                fSmallByteBufferPool.returnBuffer(fCurrentEntity.fByteBuffer);
            }
            else {
                fLargeByteBufferPool.returnBuffer(fCurrentEntity.fByteBuffer);
            }
        }
        fCurrentEntity = fEntityStack.size() > 0
                       ? (ScannedEntity)fEntityStack.pop() : null;
        fEntityScanner.setCurrentEntity(fCurrentEntity);
        if (DEBUG_BUFFER) {
            System.out.print(")endEntity: ");
            print(fCurrentEntity);
            System.out.println();
        }
    } 
    protected EncodingInfo getEncodingInfo(byte[] b4, int count) {
        if (count < 2) {
            return EncodingInfo.UTF_8;
        }
        int b0 = b4[0] & 0xFF;
        int b1 = b4[1] & 0xFF;
        if (b0 == 0xFE && b1 == 0xFF) {
            return EncodingInfo.UTF_16_BIG_ENDIAN_WITH_BOM;
        }
        if (b0 == 0xFF && b1 == 0xFE) {
            return EncodingInfo.UTF_16_LITTLE_ENDIAN_WITH_BOM;
        }
        if (count < 3) {
            return EncodingInfo.UTF_8;
        }
        int b2 = b4[2] & 0xFF;
        if (b0 == 0xEF && b1 == 0xBB && b2 == 0xBF) {
            return EncodingInfo.UTF_8_WITH_BOM;
        }
        if (count < 4) {
            return EncodingInfo.UTF_8;
        }
        int b3 = b4[3] & 0xFF;
        if (b0 == 0x00 && b1 == 0x00 && b2 == 0x00 && b3 == 0x3C) {
            return EncodingInfo.UCS_4_BIG_ENDIAN;
        }
        if (b0 == 0x3C && b1 == 0x00 && b2 == 0x00 && b3 == 0x00) {
            return EncodingInfo.UCS_4_LITTLE_ENDIAN;
        }
        if (b0 == 0x00 && b1 == 0x00 && b2 == 0x3C && b3 == 0x00) {
            return EncodingInfo.UCS_4_UNUSUAL_BYTE_ORDER;
        }
        if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x00) {
            return EncodingInfo.UCS_4_UNUSUAL_BYTE_ORDER;
        }
        if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x3F) {
            return EncodingInfo.UTF_16_BIG_ENDIAN;
        }
        if (b0 == 0x3C && b1 == 0x00 && b2 == 0x3F && b3 == 0x00) {
            return EncodingInfo.UTF_16_LITTLE_ENDIAN;
        }
        if (b0 == 0x4C && b1 == 0x6F && b2 == 0xA7 && b3 == 0x94) {
            return EncodingInfo.EBCDIC;
        }
        return EncodingInfo.UTF_8;
    } 
    protected Reader createReader(InputStream inputStream, String encoding, Boolean isBigEndian)
        throws IOException {
        if (encoding == "UTF-8" || encoding == null) {
            return createUTF8Reader(inputStream);
        }
        if (encoding == "UTF-16" && isBigEndian != null) {
            return createUTF16Reader(inputStream, isBigEndian.booleanValue());
        }
        String ENCODING = encoding.toUpperCase(Locale.ENGLISH);
        if (ENCODING.equals("UTF-8")) {
            return createUTF8Reader(inputStream);
        }
        if (ENCODING.equals("UTF-16BE")) {
            return createUTF16Reader(inputStream, true);
        }
        if (ENCODING.equals("UTF-16LE")) {
            return createUTF16Reader(inputStream, false);
        }
        if (ENCODING.equals("ISO-10646-UCS-4")) {
            if(isBigEndian != null) {
                boolean isBE = isBigEndian.booleanValue();
                if(isBE) {
                    return new UCSReader(inputStream, UCSReader.UCS4BE);
                } else {
                    return new UCSReader(inputStream, UCSReader.UCS4LE);
                }
            } else {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                       "EncodingByteOrderUnsupported",
                                       new Object[] { encoding },
                                       XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
        }
        if (ENCODING.equals("ISO-10646-UCS-2")) {
            if(isBigEndian != null) { 
                boolean isBE = isBigEndian.booleanValue();
                if(isBE) {
                    return new UCSReader(inputStream, UCSReader.UCS2BE);
                } else {
                    return new UCSReader(inputStream, UCSReader.UCS2LE);
                }
            } else {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                       "EncodingByteOrderUnsupported",
                                       new Object[] { encoding },
                                       XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
        }
        boolean validIANA = XMLChar.isValidIANAEncoding(encoding);
        boolean validJava = XMLChar.isValidJavaEncoding(encoding);
        if (!validIANA || (fAllowJavaEncodings && !validJava)) {
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                       "EncodingDeclInvalid",
                                       new Object[] { encoding },
                                       XMLErrorReporter.SEVERITY_FATAL_ERROR);
            return createLatin1Reader(inputStream);
        }
        String javaEncoding = EncodingMap.getIANA2JavaMapping(ENCODING);
        if (javaEncoding == null) {
            if (fAllowJavaEncodings) {
                javaEncoding = encoding;
            } 
            else {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                       "EncodingDeclInvalid",
                                       new Object[] { encoding },
                                       XMLErrorReporter.SEVERITY_FATAL_ERROR);
                return createLatin1Reader(inputStream);
            }
        }
        else if (javaEncoding.equals("ASCII")) {
            return createASCIIReader(inputStream);
        }
        else if (javaEncoding.equals("ISO8859_1")) {
            return createLatin1Reader(inputStream);
        }
        if (DEBUG_ENCODINGS) {
            System.out.print("$$$ creating Java InputStreamReader: encoding="+javaEncoding);
            if (javaEncoding == encoding) {
                System.out.print(" (IANA encoding)");
            }
            System.out.println();
        }
        return new InputStreamReader(inputStream, javaEncoding);
    } 
    private Reader createUTF8Reader(InputStream stream) {
        if (DEBUG_ENCODINGS) {
            System.out.println("$$$ creating UTF8Reader");
        }
        if (fTempByteBuffer == null) {
            fTempByteBuffer = fSmallByteBufferPool.getBuffer();
        }
        return new UTF8Reader(stream,
                fTempByteBuffer, 
                fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN), 
                fErrorReporter.getLocale());
    } 
    private Reader createUTF16Reader(InputStream stream, boolean isBigEndian) {
        if (DEBUG_ENCODINGS) {
            System.out.println("$$$ creating UTF16Reader");
        }
        if (fTempByteBuffer == null) {
            fTempByteBuffer = fLargeByteBufferPool.getBuffer();
        }
        else if (fTempByteBuffer.length == fBufferSize) {
            fSmallByteBufferPool.returnBuffer(fTempByteBuffer);
            fTempByteBuffer = fLargeByteBufferPool.getBuffer();
        }
        return new UTF16Reader(stream,
                fTempByteBuffer, 
                isBigEndian, 
                fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN), 
                fErrorReporter.getLocale());
    } 
    private Reader createASCIIReader(InputStream stream) {
        if (DEBUG_ENCODINGS) {
            System.out.println("$$$ creating ASCIIReader");
        }
        if (fTempByteBuffer == null) {
            fTempByteBuffer = fSmallByteBufferPool.getBuffer();
        }
        return new ASCIIReader(stream, 
                fTempByteBuffer, 
                fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN), 
                fErrorReporter.getLocale());
    } 
    private Reader createLatin1Reader(InputStream stream) {
        if (DEBUG_ENCODINGS) {
            System.out.println("$$$ creating Latin1Reader");
        }
        if (fTempByteBuffer == null) {
            fTempByteBuffer = fSmallByteBufferPool.getBuffer();
        }
        return new Latin1Reader(stream, fTempByteBuffer);
    } 
    protected static String fixURI(String str) {
        str = str.replace(java.io.File.separatorChar, '/');
        StringBuffer sb = null;
        if (str.length() >= 2) {
            char ch1 = str.charAt(1);
            if (ch1 == ':') {
                char ch0 = Character.toUpperCase(str.charAt(0));
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    sb = new StringBuffer(str.length() + 8);
                    sb.append("file:///");
                }
            }
            else if (ch1 == '/' && str.charAt(0) == '/') {
                sb = new StringBuffer(str.length() + 5);
                sb.append("file:");
            }
        }
        int pos = str.indexOf(' ');
        if (pos < 0) {
            if (sb != null) {
                sb.append(str);
                str = sb.toString();
            }
        }
        else {
            if (sb == null)
                sb = new StringBuffer(str.length());
            for (int i = 0; i < pos; i++)
                sb.append(str.charAt(i));
            sb.append("%20");
            for (int i = pos+1; i < str.length(); i++) {
                if (str.charAt(i) == ' ')
                    sb.append("%20");
                else
                    sb.append(str.charAt(i));
            }
            str = sb.toString();
        }
        return str;
    } 
    Hashtable getDeclaredEntities() {
        return fEntities;
    } 
    static final void print(ScannedEntity currentEntity) {
        if (DEBUG_BUFFER) {
            if (currentEntity != null) {
                System.out.print('[');
                System.out.print(currentEntity.count);
                System.out.print(' ');
                System.out.print(currentEntity.position);
                if (currentEntity.count > 0) {
                    System.out.print(" \"");
                    for (int i = 0; i < currentEntity.count; i++) {
                        if (i == currentEntity.position) {
                            System.out.print('^');
                        }
                        char c = currentEntity.ch[i];
                        switch (c) {
                            case '\n': {
                                System.out.print("\\n");
                                break;
                            }
                            case '\r': {
                                System.out.print("\\r");
                                break;
                            }
                            case '\t': {
                                System.out.print("\\t");
                                break;
                            }
                            case '\\': {
                                System.out.print("\\\\");
                                break;
                            }
                            default: {
                                System.out.print(c);
                            }
                        }
                    }
                    if (currentEntity.position == currentEntity.count) {
                        System.out.print('^');
                    }
                    System.out.print('"');
                }
                System.out.print(']');
                System.out.print(" @ ");
                System.out.print(currentEntity.lineNumber);
                System.out.print(',');
                System.out.print(currentEntity.columnNumber);
            }
            else {
                System.out.print("*NO CURRENT ENTITY*");
            }
        }
    } 
    public static abstract class Entity {
        public String name;
        public boolean inExternalSubset; 
        public Entity() {
            clear();
        } 
        public Entity(String name, boolean inExternalSubset) {
            this.name = name;
            this.inExternalSubset = inExternalSubset;
        } 
        public boolean isEntityDeclInExternalSubset () {
            return inExternalSubset;
        } 
        public abstract boolean isExternal();
        public abstract boolean isUnparsed();
        public void clear() {
            name = null;
            inExternalSubset = false;
        } 
        public void setValues(Entity entity) {
            name = entity.name;
            inExternalSubset = entity.inExternalSubset;
        } 
    } 
    protected static class InternalEntity
        extends Entity {
        public String text;
        public InternalEntity() {
            clear();
        } 
        public InternalEntity(String name, String text, boolean inExternalSubset) {
            super(name,inExternalSubset);
            this.text = text;
        } 
        public final boolean isExternal() {
            return false;
        } 
        public final boolean isUnparsed() {
            return false;
        } 
        public void clear() {
            super.clear();
            text = null;
        } 
        public void setValues(Entity entity) {
            super.setValues(entity);
            text = null;
        } 
        public void setValues(InternalEntity entity) {
            super.setValues(entity);
            text = entity.text;
        } 
    } 
    protected static class ExternalEntity
        extends Entity {
        public XMLResourceIdentifier entityLocation;
        public String notation;
        public ExternalEntity() {
            clear();
        } 
        public ExternalEntity(String name, XMLResourceIdentifier entityLocation,
                              String notation, boolean inExternalSubset) {
            super(name,inExternalSubset);
            this.entityLocation = entityLocation;
            this.notation = notation;
        } 
        public final boolean isExternal() {
            return true;
        } 
        public final boolean isUnparsed() {
            return notation != null;
        } 
        public void clear() {
            super.clear();
            entityLocation = null;
            notation = null;
        } 
        public void setValues(Entity entity) {
            super.setValues(entity);
            entityLocation = null;
            notation = null;
        } 
        public void setValues(ExternalEntity entity) {
            super.setValues(entity);
            entityLocation = entity.entityLocation;
            notation = entity.notation;
        } 
    } 
    public class ScannedEntity
        extends Entity {
        public InputStream stream;
        public Reader reader;
        public XMLResourceIdentifier entityLocation;
        public int lineNumber = 1;
        public int columnNumber = 1;
        public String encoding;
        boolean externallySpecifiedEncoding = false;
        public String xmlVersion = "1.0";
        public boolean literal;
        public boolean isExternal;
        public char[] ch = null;
        public int position;
        public int baseCharOffset;
        public int startPosition;
        public int count;
        public boolean mayReadChunks;
        private CharacterBuffer fCharacterBuffer;
        private byte [] fByteBuffer;
        public ScannedEntity(String name,
                             XMLResourceIdentifier entityLocation,
                             InputStream stream, Reader reader, byte [] byteBuffer,
                             String encoding, boolean literal, boolean mayReadChunks, boolean isExternal) {
            super(name,XMLEntityManager.this.fInExternalSubset);
            this.entityLocation = entityLocation;
            this.stream = stream;
            this.reader = reader;
            this.encoding = encoding;
            this.literal = literal;
            this.mayReadChunks = mayReadChunks;
            this.isExternal = isExternal;
            this.fCharacterBuffer = fCharacterBufferPool.getBuffer(isExternal);
            this.ch = fCharacterBuffer.ch;
            this.fByteBuffer = byteBuffer;
        } 
        public final boolean isExternal() {
            return isExternal;
        } 
        public final boolean isUnparsed() {
            return false;
        } 
        public void setReader(InputStream stream, String encoding, Boolean isBigEndian) throws IOException {
            fTempByteBuffer = fByteBuffer;
            reader = createReader(stream, encoding, isBigEndian);
            fByteBuffer = fTempByteBuffer;
        }
        public String getExpandedSystemId() {
            int size = fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
               ScannedEntity externalEntity =
                    (ScannedEntity)fEntityStack.elementAt(i);
                if (externalEntity.entityLocation != null &&
                        externalEntity.entityLocation.getExpandedSystemId() != null) {
                    return externalEntity.entityLocation.getExpandedSystemId();
                }
            }
            return null;
        }
        public String getLiteralSystemId() { 
            int size = fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
               ScannedEntity externalEntity =
                    (ScannedEntity)fEntityStack.elementAt(i);
                if (externalEntity.entityLocation != null &&
                        externalEntity.entityLocation.getLiteralSystemId() != null) {
                    return externalEntity.entityLocation.getLiteralSystemId();
                }
            }
            return null;
        }
        public int getLineNumber() {
            int size = fEntityStack.size();
            for (int i = size - 1; i >= 0 ; --i) {
                ScannedEntity firstExternalEntity = (ScannedEntity)fEntityStack.elementAt(i);
                if (firstExternalEntity.isExternal()) {
                    return firstExternalEntity.lineNumber;
                }
            }
            return -1;
        }
        public int getColumnNumber() {
            int size = fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
                ScannedEntity firstExternalEntity = (ScannedEntity)fEntityStack.elementAt(i);
                if (firstExternalEntity.isExternal()) {
                    return firstExternalEntity.columnNumber;
                }
            }
            return -1;
        }
        public int getCharacterOffset() {
            int size = fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
                ScannedEntity firstExternalEntity = (ScannedEntity)fEntityStack.elementAt(i);
                if (firstExternalEntity.isExternal()) {
                    return firstExternalEntity.baseCharOffset + (firstExternalEntity.position - firstExternalEntity.startPosition);
                }
            }
            return -1;
        }
        public String getEncoding() {
            int size = fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
                ScannedEntity firstExternalEntity = (ScannedEntity)fEntityStack.elementAt(i);
                if (firstExternalEntity.isExternal()) {
                    return firstExternalEntity.encoding;
                }
            }
            return null;
        }
        public String getXMLVersion() {
            int size = fEntityStack.size();
            for (int i = size - 1; i >= 0; --i) {
                ScannedEntity firstExternalEntity = (ScannedEntity)fEntityStack.elementAt(i);
                if (firstExternalEntity.isExternal()) {
                    return firstExternalEntity.xmlVersion;
                }
            }
            return null;
        }
        public boolean isEncodingExternallySpecified() {
            return externallySpecifiedEncoding;
        }
        public void setEncodingExternallySpecified(boolean value) {
            externallySpecifiedEncoding = value;
        }
        public String toString() {
            StringBuffer str = new StringBuffer();
            str.append("name=\"").append(name).append('"');
            str.append(",ch=");
            str.append(ch);
            str.append(",position=").append(position);
            str.append(",count=").append(count);
            str.append(",baseCharOffset=").append(baseCharOffset);
            str.append(",startPosition=").append(startPosition);
            return str.toString();
        } 
    } 
    private static class EncodingInfo {
        public static final EncodingInfo UTF_8 = new EncodingInfo("UTF-8", null, false);
        public static final EncodingInfo UTF_8_WITH_BOM = new EncodingInfo("UTF-8", null, true);
        public static final EncodingInfo UTF_16_BIG_ENDIAN = new EncodingInfo("UTF-16", Boolean.TRUE, false);
        public static final EncodingInfo UTF_16_BIG_ENDIAN_WITH_BOM = new EncodingInfo("UTF-16", Boolean.TRUE, true);
        public static final EncodingInfo UTF_16_LITTLE_ENDIAN = new EncodingInfo("UTF-16", Boolean.FALSE, false);
        public static final EncodingInfo UTF_16_LITTLE_ENDIAN_WITH_BOM = new EncodingInfo("UTF-16", Boolean.FALSE, true);
        public static final EncodingInfo UCS_4_BIG_ENDIAN = new EncodingInfo("ISO-10646-UCS-4", Boolean.TRUE, false);
        public static final EncodingInfo UCS_4_LITTLE_ENDIAN = new EncodingInfo("ISO-10646-UCS-4", Boolean.FALSE, false);
        public static final EncodingInfo UCS_4_UNUSUAL_BYTE_ORDER = new EncodingInfo("ISO-10646-UCS-4", null, false);
        public static final EncodingInfo EBCDIC = new EncodingInfo("CP037", null, false);
        public final String encoding;
        public final Boolean isBigEndian;
        public final boolean hasBOM;
        private EncodingInfo(String encoding, Boolean isBigEndian, boolean hasBOM) {
            this.encoding = encoding;
            this.isBigEndian = isBigEndian;
            this.hasBOM = hasBOM;
        } 
    } 
    private static final class ByteBufferPool {
        private static final int DEFAULT_POOL_SIZE = 3;
        private int fPoolSize;
        private int fBufferSize;
        private byte[][] fByteBufferPool;
        private int fDepth;
        public ByteBufferPool(int bufferSize) {
            this(DEFAULT_POOL_SIZE, bufferSize);
        }
        public ByteBufferPool(int poolSize, int bufferSize) {
            fPoolSize = poolSize;
            fBufferSize = bufferSize;
            fByteBufferPool = new byte[fPoolSize][];
            fDepth = 0;
        }
        public byte[] getBuffer() {
            return (fDepth > 0) ? fByteBufferPool[--fDepth] : new byte[fBufferSize];
        }
        public void returnBuffer(byte[] buffer) {
            if (fDepth < fByteBufferPool.length) {
                fByteBufferPool[fDepth++] = buffer;
            }
        }
        public void setBufferSize(int bufferSize) {
            fBufferSize = bufferSize;
            fByteBufferPool = new byte[fPoolSize][];
            fDepth = 0;
        } 
    }
    private static final class CharacterBuffer {
        private final char[] ch;
        private final boolean isExternal;
        public CharacterBuffer(boolean isExternal, int size) {
            this.isExternal = isExternal;
            ch = new char[size];
        }
    }
    private static final class CharacterBufferPool {
        private static final int DEFAULT_POOL_SIZE = 3;
        private CharacterBuffer[] fInternalBufferPool;
        private CharacterBuffer[] fExternalBufferPool;
        private int fExternalBufferSize;
        private int fInternalBufferSize;
        private int fPoolSize;
        private int fInternalTop;
        private int fExternalTop;
        public CharacterBufferPool(int externalBufferSize, int internalBufferSize) {
            this(DEFAULT_POOL_SIZE, externalBufferSize, internalBufferSize);
        }
        public CharacterBufferPool(int poolSize, int externalBufferSize, int internalBufferSize) {
            fExternalBufferSize = externalBufferSize;
            fInternalBufferSize = internalBufferSize;
            fPoolSize = poolSize;
            init();
        }
        private void init() {
            fInternalBufferPool = new CharacterBuffer[fPoolSize];
            fExternalBufferPool = new CharacterBuffer[fPoolSize];
            fInternalTop = -1;
            fExternalTop = -1;
        }
        public CharacterBuffer getBuffer(boolean external) {
            if (external) {
                if (fExternalTop > -1) {
                    return (CharacterBuffer)fExternalBufferPool[fExternalTop--];
                }
                else {
                    return new CharacterBuffer(true, fExternalBufferSize);
                }
            }
            else {
                if (fInternalTop > -1) {
                    return (CharacterBuffer)fInternalBufferPool[fInternalTop--];
                }
                else {
                    return new CharacterBuffer(false, fInternalBufferSize);
                }
            }
        }
        public void returnBuffer(CharacterBuffer buffer) {
            if (buffer.isExternal) {
                if (fExternalTop < fExternalBufferPool.length - 1) {
                    fExternalBufferPool[++fExternalTop] = buffer;
                }
            }
            else if (fInternalTop < fInternalBufferPool.length - 1) {
                fInternalBufferPool[++fInternalTop] = buffer;
            }
        }
        public void setExternalBufferSize(int bufferSize) {
            fExternalBufferSize = bufferSize;
            fExternalBufferPool = new CharacterBuffer[fPoolSize];
            fExternalTop = -1;
        }
    }
    protected final class RewindableInputStream extends InputStream {
        private InputStream fInputStream;
        private byte[] fData;
        private int fStartOffset;
        private int fEndOffset;
        private int fOffset;
        private int fLength;
        private int fMark;
        public RewindableInputStream(InputStream is) {
            fData = new byte[DEFAULT_XMLDECL_BUFFER_SIZE];
            fInputStream = is;
            fStartOffset = 0;
            fEndOffset = -1;
            fOffset = 0;
            fLength = 0;
            fMark = 0;
        }
        public void setStartOffset(int offset) {
            fStartOffset = offset;
        }
        public void rewind() {
            fOffset = fStartOffset;
        }
        public int readAndBuffer() throws IOException {
            if (fOffset == fData.length) {
                byte[] newData = new byte[fOffset << 1];
                System.arraycopy(fData, 0, newData, 0, fOffset);
                fData = newData;
            }
            final int b = fInputStream.read();
            if (b == -1) {
                fEndOffset = fOffset;
                return -1;
            }
            fData[fLength++] = (byte)b;
            fOffset++;
            return b & 0xff;
        }
        public int read() throws IOException {
            if (fOffset < fLength) {
                return fData[fOffset++] & 0xff;
            }
            if (fOffset == fEndOffset) {
                return -1;
            }
            if (fCurrentEntity.mayReadChunks) {
                return fInputStream.read();
            }
            return readAndBuffer();
        }
        public int read(byte[] b, int off, int len) throws IOException {
            final int bytesLeft = fLength - fOffset;
            if (bytesLeft == 0) {
                if (fOffset == fEndOffset) {
                    return -1;
                }
                if (fCurrentEntity.mayReadChunks) {
                    return fInputStream.read(b, off, len);
                }
                int returnedVal = readAndBuffer();
                if (returnedVal == -1) {
                    fEndOffset = fOffset;
                    return -1;
                }
                b[off] = (byte)returnedVal;
                return 1;
            }
            if (len < bytesLeft) {
                if (len <= 0) {
                    return 0;
                }
            }
            else {
                len = bytesLeft;
            }
            if (b != null) {
                System.arraycopy(fData, fOffset, b, off, len);
            }
            fOffset += len;
            return len;
        }
        public long skip(long n)
            throws IOException
        {
            int bytesLeft;
            if (n <= 0) {
                return 0;
            }
            bytesLeft = fLength - fOffset;
            if (bytesLeft == 0) {
                if (fOffset == fEndOffset) {
                    return 0;
                }
                return fInputStream.skip(n);
            }
            if (n <= bytesLeft) {
                fOffset += n;
                return n;
            }
            fOffset += bytesLeft;
            if (fOffset == fEndOffset) {
                return bytesLeft;
            }
            n -= bytesLeft;
            return fInputStream.skip(n) + bytesLeft;
        }
        public int available() throws IOException {
            final int bytesLeft = fLength - fOffset;
            if (bytesLeft == 0) {
                if (fOffset == fEndOffset) {
                    return -1;
                }
                return fCurrentEntity.mayReadChunks ? fInputStream.available()
                                                    : 0;
            }
            return bytesLeft;
        }
        public void mark(int howMuch) {
            fMark = fOffset;
        }
        public void reset() {
            fOffset = fMark;
        }
        public boolean markSupported() {
            return true;
        }
        public void close() throws IOException {
            if (fInputStream != null) {
                fInputStream.close();
                fInputStream = null;
            }
        }
    } 
} 
