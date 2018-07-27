package org.apache.xerces.xni.parser;
import java.io.IOException;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
public interface XMLEntityResolver {
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
        throws XNIException, IOException;
} 
