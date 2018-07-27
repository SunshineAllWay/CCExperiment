package org.apache.xerces.impl.validation;
public final class ConfigurableValidationState extends ValidationState {
    private boolean fIdIdrefChecking;
    private boolean fUnparsedEntityChecking;
    public ConfigurableValidationState() {
        super();
        fIdIdrefChecking = true;
        fUnparsedEntityChecking = true;
    }
    public void setIdIdrefChecking(boolean setting) {
        fIdIdrefChecking = setting;
    }
    public void setUnparsedEntityChecking(boolean setting) {
        fUnparsedEntityChecking = setting;
    }
    public String checkIDRefID() {
        return (fIdIdrefChecking) ? super.checkIDRefID() : null;
    }
    public boolean isIdDeclared(String name) {
        return (fIdIdrefChecking) ? super.isIdDeclared(name) : false;
    }
    public boolean isEntityDeclared(String name) {
        return (fUnparsedEntityChecking) ? super.isEntityDeclared(name) : true;
    }
    public boolean isEntityUnparsed(String name) {
        return (fUnparsedEntityChecking) ? super.isEntityUnparsed(name) : true;
    }
    public void addId(String name) {
        if (fIdIdrefChecking) {
            super.addId(name);
        }
    }
    public void addIdRef(String name) {
        if (fIdIdrefChecking) {
            super.addIdRef(name);
        }
    }
}
