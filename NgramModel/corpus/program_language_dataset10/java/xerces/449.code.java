package org.apache.xerces.impl.xs.identity;
public interface FieldActivator {
    public void startValueScopeFor(IdentityConstraint identityConstraint,
            int initialDepth);
    public XPathMatcher activateField(Field field, int initialDepth);
    public void endValueScopeFor(IdentityConstraint identityConstraint, int initialDepth);
} 
