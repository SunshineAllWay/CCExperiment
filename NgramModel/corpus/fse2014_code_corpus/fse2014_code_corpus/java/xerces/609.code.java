package org.apache.xerces.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.apache.xerces.impl.ExternalSubsetResolver;
import org.apache.xerces.impl.XMLEntityDescription;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLDTDDescription;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
public class EntityResolver2Wrapper 
    implements ExternalSubsetResolver {
    protected EntityResolver2 fEntityResolver;
    public EntityResolver2Wrapper() {}
    public EntityResolver2Wrapper(EntityResolver2 entityResolver) {
        setEntityResolver(entityResolver);
    } 
    public void setEntityResolver(EntityResolver2 entityResolver) {
        fEntityResolver = entityResolver;
    } 
    public EntityResolver2 getEntityResolver() {
        return fEntityResolver;
    } 
    public XMLInputSource getExternalSubset(XMLDTDDescription grammarDescription)
            throws XNIException, IOException {
        if (fEntityResolver != null) {
            String name = grammarDescription.getRootName();
            String baseURI = grammarDescription.getBaseSystemId();
            try {
                InputSource inputSource = fEntityResolver.getExternalSubset(name, baseURI);
                return (inputSource != null) ? createXMLInputSource(inputSource, baseURI) : null;
            }
            catch (SAXException e) {
                Exception ex = e.getException();
                if (ex == null) {
                    ex = e;
                }
                throw new XNIException(ex);
            }
        }
        return null;
    } 
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException {
        if (fEntityResolver != null) {
            String pubId = resourceIdentifier.getPublicId();
            String sysId = resourceIdentifier.getLiteralSystemId();
            String baseURI = resourceIdentifier.getBaseSystemId();
            String name = null;
            if (resourceIdentifier instanceof XMLDTDDescription) {
                name = "[dtd]";
            }
            else if (resourceIdentifier instanceof XMLEntityDescription) {
                name = ((XMLEntityDescription) resourceIdentifier).getEntityName();
            }
            if (pubId == null && sysId == null) {
                return null;
            }
            try {
                InputSource inputSource = 
                    fEntityResolver.resolveEntity(name, pubId, baseURI, sysId);
                return (inputSource != null) ? createXMLInputSource(inputSource, baseURI) : null;
            }
            catch (SAXException e) {
                Exception ex = e.getException();
                if (ex == null) {
                    ex = e;
                }
                throw new XNIException(ex);
            }   
        }
        return null;
    } 
    private XMLInputSource createXMLInputSource(InputSource source, String baseURI) {
        String publicId = source.getPublicId();
        String systemId = source.getSystemId();
        String baseSystemId = baseURI;
        InputStream byteStream = source.getByteStream();
        Reader charStream = source.getCharacterStream();
        String encoding = source.getEncoding();
        XMLInputSource xmlInputSource =
            new XMLInputSource(publicId, systemId, baseSystemId);
        xmlInputSource.setByteStream(byteStream);
        xmlInputSource.setCharacterStream(charStream);
        xmlInputSource.setEncoding(encoding);
        return xmlInputSource;
    } 
} 
