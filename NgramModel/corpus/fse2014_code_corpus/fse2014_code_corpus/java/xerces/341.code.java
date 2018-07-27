package org.apache.xerces.impl.dv.dtd;
import org.apache.xerces.impl.dv.DatatypeValidator;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.util.XMLChar;
public class IDDatatypeValidator implements DatatypeValidator {
    public IDDatatypeValidator() {
    }
    public void validate(String content, ValidationContext context) throws InvalidDatatypeValueException {
        if(context.useNamespaces()) {
            if (!XMLChar.isValidNCName(content)) {
                throw new InvalidDatatypeValueException("IDInvalidWithNamespaces", new Object[]{content});
            }
        }
        else {
            if (!XMLChar.isValidName(content)) {
                throw new InvalidDatatypeValueException("IDInvalid", new Object[]{content});
            }
        }
        if (context.isIdDeclared(content)) {
            throw new InvalidDatatypeValueException("IDNotUnique", new Object[]{content});
        }
        context.addId(content);
    }
}
