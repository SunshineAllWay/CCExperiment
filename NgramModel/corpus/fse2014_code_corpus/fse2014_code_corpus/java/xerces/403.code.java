package org.apache.xerces.impl.validation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.NamespaceContext;
public class ValidationState implements ValidationContext {
    private boolean fExtraChecking              = true;
    private boolean fFacetChecking              = true;
    private boolean fNormalize                  = true;
    private boolean fNamespaces                 = true;
    private EntityState fEntityState            = null;
    private NamespaceContext fNamespaceContext  = null;
    private SymbolTable fSymbolTable            = null;
    private Locale fLocale                      = null;
    private final HashMap fIdTable    = new HashMap();
    private final HashMap fIdRefTable = new HashMap();
    private final static Object fNullValue = new Object();
    public void setExtraChecking(boolean newValue) {
        fExtraChecking = newValue;
    }
    public void setFacetChecking(boolean newValue) {
        fFacetChecking = newValue;
    }
    public void setNormalizationRequired (boolean newValue) {
          fNormalize = newValue;
    }
    public void setUsingNamespaces (boolean newValue) {
          fNamespaces = newValue;
    }
    public void setEntityState(EntityState state) {
        fEntityState = state;
    }
    public void setNamespaceSupport(NamespaceContext namespace) {
        fNamespaceContext = namespace;
    }
    public void setSymbolTable(SymbolTable sTable) {
        fSymbolTable = sTable;
    }
    public String checkIDRefID () {
        Iterator iter = fIdRefTable.keySet().iterator();
        String key;
        while (iter.hasNext()) {
            key = (String) iter.next();
            if (!fIdTable.containsKey(key)) {
                  return key;
            }
        }
        return null;
    }
    public void reset () {
        fExtraChecking = true;
        fFacetChecking = true;
        fNamespaces = true;
        fIdTable.clear();
        fIdRefTable.clear();
        fEntityState = null;
        fNamespaceContext = null;
        fSymbolTable = null;
    }
    public void resetIDTables() {
        fIdTable.clear();
        fIdRefTable.clear();
    }
    public boolean needExtraChecking() {
        return fExtraChecking;
    }
    public boolean needFacetChecking() {
        return fFacetChecking;
    }
    public boolean needToNormalize (){
        return fNormalize;
    }
    public boolean useNamespaces() {
        return fNamespaces;
    }
    public boolean isEntityDeclared (String name) {
        if (fEntityState !=null) {
            return fEntityState.isEntityDeclared(getSymbol(name));
        }
        return false;
    }
    public boolean isEntityUnparsed (String name) {
        if (fEntityState !=null) {
            return fEntityState.isEntityUnparsed(getSymbol(name));
        }
        return false;
    }
    public boolean isIdDeclared(String name) {
        return fIdTable.containsKey(name);
    }
    public void addId(String name) {
        fIdTable.put(name, fNullValue);
    }
    public void addIdRef(String name) {
        fIdRefTable.put(name, fNullValue);
    }
    public String getSymbol (String symbol) {
        if (fSymbolTable != null)
            return fSymbolTable.addSymbol(symbol);
        return symbol.intern();
    }
    public String getURI(String prefix) {
        if (fNamespaceContext !=null) {
            return fNamespaceContext.getURI(prefix);
        }
        return null;
    }
    public void setLocale(Locale locale) {
        fLocale = locale;
    }
    public Locale getLocale() {
        return fLocale;
    }
}
