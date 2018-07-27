package org.apache.xerces.impl.dv.xs;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.util.XMLChar;
public class EntityDV extends TypeValidator {
    public short getAllowedFacets(){
        return (XSSimpleTypeDecl.FACET_LENGTH | XSSimpleTypeDecl.FACET_MINLENGTH | XSSimpleTypeDecl.FACET_MAXLENGTH | XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION | XSSimpleTypeDecl.FACET_WHITESPACE );
    }
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        if (!XMLChar.isValidNCName(content)) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "NCName"});
        }
        return content;
    }
    public void checkExtraRules(Object value, ValidationContext context) throws InvalidDatatypeValueException {
        if (!context.isEntityUnparsed((String)value)) {
            throw new InvalidDatatypeValueException("UndeclaredEntity", new Object[]{value});
        }
    }
} 
