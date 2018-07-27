package org.apache.xerces.impl.dv.xs;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
public class UnionDV extends TypeValidator{
    public short getAllowedFacets(){
          return (XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION );
    }
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        return content;
    }
} 
