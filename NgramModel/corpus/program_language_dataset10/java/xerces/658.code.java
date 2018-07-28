package org.apache.xerces.xni;
import org.apache.xerces.xni.parser.XMLDTDSource;
public interface XMLDTDHandler {
    public static final short CONDITIONAL_INCLUDE = 0;
    public static final short CONDITIONAL_IGNORE = 1;
    public void startDTD(XMLLocator locator, Augmentations augmentations) 
        throws XNIException;
    public void startParameterEntity(String name, 
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     Augmentations augmentations) throws XNIException;
    public void textDecl(String version, String encoding,
                         Augmentations augmentations) throws XNIException;
    public void endParameterEntity(String name, Augmentations augmentations) 
        throws XNIException;
    public void startExternalSubset(XMLResourceIdentifier identifier, 
                                    Augmentations augmentations) 
        throws XNIException;
    public void endExternalSubset(Augmentations augmentations) 
        throws XNIException;
    public void comment(XMLString text, Augmentations augmentations) 
        throws XNIException;
    public void processingInstruction(String target, XMLString data,
                                      Augmentations augmentations)
        throws XNIException;
    public void elementDecl(String name, String contentModel,
                            Augmentations augmentations)
        throws XNIException;
    public void startAttlist(String elementName,
                             Augmentations augmentations) throws XNIException;
    public void attributeDecl(String elementName, String attributeName, 
                              String type, String[] enumeration, 
                              String defaultType, XMLString defaultValue,
                              XMLString nonNormalizedDefaultValue, Augmentations augmentations)
        throws XNIException;
    public void endAttlist(Augmentations augmentations) throws XNIException;
    public void internalEntityDecl(String name, XMLString text, 
                                   XMLString nonNormalizedText,
                                   Augmentations augmentations) 
        throws XNIException;
    public void externalEntityDecl(String name, 
                                   XMLResourceIdentifier identifier,
                                   Augmentations augmentations) 
        throws XNIException;
    public void unparsedEntityDecl(String name, 
                                   XMLResourceIdentifier identifier, 
                                   String notation, Augmentations augmentations) 
        throws XNIException;
    public void notationDecl(String name, XMLResourceIdentifier identifier,
                             Augmentations augmentations) throws XNIException;
    public void startConditional(short type, Augmentations augmentations) 
        throws XNIException;
    public void ignoredCharacters(XMLString text, Augmentations augmentations) 
        throws XNIException;
    public void endConditional(Augmentations augmentations) throws XNIException;
    public void endDTD(Augmentations augmentations) throws XNIException;
    public void setDTDSource(XMLDTDSource source);
    public XMLDTDSource getDTDSource();
} 
