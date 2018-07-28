package org.apache.xerces.xinclude;
import java.util.Enumeration;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
public class MultipleScopeNamespaceSupport extends NamespaceSupport {
    protected int[] fScope = new int[8];
    protected int fCurrentScope;
    public MultipleScopeNamespaceSupport() {
        super();
        fCurrentScope = 0;
        fScope[0] = 0;
    }
    public MultipleScopeNamespaceSupport(NamespaceContext context) {
        super(context);
        fCurrentScope = 0;
        fScope[0] = 0;
    }
    public Enumeration getAllPrefixes() {
        int count = 0;
        if (fPrefixes.length < (fNamespace.length / 2)) {
            String[] prefixes = new String[fNamespaceSize];
            fPrefixes = prefixes;
        }
        String prefix = null;
        boolean unique = true;
        for (int i = fContext[fScope[fCurrentScope]];
            i <= (fNamespaceSize - 2);
            i += 2) {
            prefix = fNamespace[i];
            for (int k = 0; k < count; k++) {
                if (fPrefixes[k] == prefix) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                fPrefixes[count++] = prefix;
            }
            unique = true;
        }
        return new Prefixes(fPrefixes, count);
    }
    public int getScopeForContext(int context) {
        int scope = fCurrentScope;
        while (context < fScope[scope]) {
            scope--;
        }
        return scope;
    }
    public String getPrefix(String uri) {
        return getPrefix(uri, fNamespaceSize, fContext[fScope[fCurrentScope]]);
    }
    public String getURI(String prefix) {
        return getURI(prefix, fNamespaceSize, fContext[fScope[fCurrentScope]]);
    }
    public String getPrefix(String uri, int context) {
        return getPrefix(uri, fContext[context+1], fContext[fScope[getScopeForContext(context)]]);
    }
    public String getURI(String prefix, int context) {
        return getURI(prefix, fContext[context+1], fContext[fScope[getScopeForContext(context)]]);
    }
    public String getPrefix(String uri, int start, int end) {
        if (uri == NamespaceContext.XML_URI) {
            return XMLSymbols.PREFIX_XML;
        }
        if (uri == NamespaceContext.XMLNS_URI) {
            return XMLSymbols.PREFIX_XMLNS;
        }
        for (int i = start; i > end; i -= 2) {
            if (fNamespace[i - 1] == uri) {
                if (getURI(fNamespace[i - 2]) == uri)
                    return fNamespace[i - 2];
            }
        }
        return null;
    }
    public String getURI(String prefix, int start, int end) {
        if (prefix == XMLSymbols.PREFIX_XML) {
            return NamespaceContext.XML_URI;
        }
        if (prefix == XMLSymbols.PREFIX_XMLNS) {
            return NamespaceContext.XMLNS_URI;
        }
        for (int i = start; i > end; i -= 2) {
            if (fNamespace[i - 2] == prefix) {
                return fNamespace[i - 1];
            }
        }
        return null;
    }
    public void reset() {
        fCurrentContext = fScope[fCurrentScope];
        fNamespaceSize = fContext[fCurrentContext];
    }
    public void pushScope() {
        if (fCurrentScope + 1 == fScope.length) {
            int[] contextarray = new int[fScope.length * 2];
            System.arraycopy(fScope, 0, contextarray, 0, fScope.length);
            fScope = contextarray;
        }
        pushContext();
        fScope[++fCurrentScope] = fCurrentContext;
    }
    public void popScope() {
        fCurrentContext = fScope[fCurrentScope--];
        popContext();
    }
}
