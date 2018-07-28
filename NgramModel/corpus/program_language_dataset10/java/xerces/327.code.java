package org.apache.xerces.impl.dv;
public interface DatatypeValidator {
    public void validate(String content, ValidationContext context)
        throws InvalidDatatypeValueException;
}
