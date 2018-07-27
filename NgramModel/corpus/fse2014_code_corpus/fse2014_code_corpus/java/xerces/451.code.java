package org.apache.xerces.impl.xs.identity;
import org.apache.xerces.xs.XSIDCDefinition;
public class KeyRef
    extends IdentityConstraint {
    protected final UniqueOrKey fKey;
    public KeyRef(String namespace, String identityConstraintName,
                  String elemName, UniqueOrKey key) {
        super(namespace, identityConstraintName, elemName);
        fKey = key;
        type = IC_KEYREF;
    } 
    public UniqueOrKey getKey() {
        return fKey;
    } 
    public XSIDCDefinition getRefKey() {
        return fKey;
    }
} 
