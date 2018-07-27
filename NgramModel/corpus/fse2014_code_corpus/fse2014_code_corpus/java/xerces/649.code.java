package org.apache.xerces.xinclude;
import org.apache.xerces.xni.NamespaceContext;
public class XIncludeNamespaceSupport extends MultipleScopeNamespaceSupport {
    private boolean[] fValidContext = new boolean[8];
    public XIncludeNamespaceSupport() {
        super();
    }
    public XIncludeNamespaceSupport(NamespaceContext context) {
        super(context);
    }
    public void pushContext() {
        super.pushContext();
        if (fCurrentContext + 1 == fValidContext.length) {
            boolean[] contextarray = new boolean[fValidContext.length * 2];
            System.arraycopy(fValidContext, 0, contextarray, 0, fValidContext.length);
            fValidContext = contextarray;
        }
        fValidContext[fCurrentContext] = true;
    }
    public void setContextInvalid() {
        fValidContext[fCurrentContext] = false;
    }
    public String getURIFromIncludeParent(String prefix) {
        int lastValidContext = fCurrentContext - 1;
        while (lastValidContext > 0 && !fValidContext[lastValidContext]) {
            lastValidContext--;
        }
        return getURI(prefix, lastValidContext);
    }
}