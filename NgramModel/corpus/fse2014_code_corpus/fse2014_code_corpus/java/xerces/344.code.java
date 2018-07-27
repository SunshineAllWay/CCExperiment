package org.apache.xerces.impl.dv.dtd;
import org.apache.xerces.impl.dv.DatatypeValidator;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.util.XMLChar;
public class NMTOKENDatatypeValidator implements DatatypeValidator {
    public NMTOKENDatatypeValidator() {
    }
    public void validate(String content, ValidationContext context) throws InvalidDatatypeValueException {
        if (!XMLChar.isValidNmtoken(content)) {
           throw new InvalidDatatypeValueException("NMTOKENInvalid", new Object[]{content});
       }
   }
} 
