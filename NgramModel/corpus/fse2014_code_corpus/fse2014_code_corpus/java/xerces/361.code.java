package org.apache.xerces.impl.dv.xs;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
public class BooleanDV extends TypeValidator {
    public short getAllowedFacets() {
        return (XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_WHITESPACE);
    }
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        if ("false".equals(content) || "0".equals(content)) {
            return Boolean.FALSE;
        }
        else if ("true".equals(content) || "1".equals(content)) {
            return Boolean.TRUE;
        }
        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "boolean"});
    }
} 
