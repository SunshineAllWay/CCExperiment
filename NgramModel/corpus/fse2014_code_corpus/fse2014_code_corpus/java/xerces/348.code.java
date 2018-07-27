package org.apache.xerces.impl.dv.dtd;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.util.XML11Char;
public class XML11IDDatatypeValidator extends IDDatatypeValidator {
    public XML11IDDatatypeValidator() {
        super();
    }
    public void validate(String content, ValidationContext context) throws InvalidDatatypeValueException {
        if(context.useNamespaces()) {
            if (!XML11Char.isXML11ValidNCName(content)) {
                throw new InvalidDatatypeValueException("IDInvalidWithNamespaces", new Object[]{content});
            }
        }
        else {
            if (!XML11Char.isXML11ValidName(content)) {
                throw new InvalidDatatypeValueException("IDInvalid", new Object[]{content});
            }
        }
        if (context.isIdDeclared(content)) {
            throw new InvalidDatatypeValueException("IDNotUnique", new Object[]{content});
        }
        context.addId(content);
    }
}
