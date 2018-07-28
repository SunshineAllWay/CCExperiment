package org.apache.xerces.impl.validation;
import java.util.ArrayList;
public class ValidationManager {
    protected final ArrayList fVSs = new ArrayList();
    protected boolean fGrammarFound = false;
    protected boolean fCachedDTD = false;    
    public final void addValidationState(ValidationState vs) {
        fVSs.add(vs);
    }
    public final void setEntityState(EntityState state) {
        for (int i = fVSs.size()-1; i >= 0; i--) {
            ((ValidationState)fVSs.get(i)).setEntityState(state);
        }
    }
    public final void setGrammarFound(boolean grammar){
        fGrammarFound = grammar;
    }
    public final boolean isGrammarFound(){
        return fGrammarFound;
    }
    public final void setCachedDTD(boolean cachedDTD) {
        fCachedDTD = cachedDTD;
    } 
    public final boolean isCachedDTD() {
        return fCachedDTD;
    } 
    public final void reset () {
        fVSs.clear();
        fGrammarFound = false;
        fCachedDTD = false;
    }
}
