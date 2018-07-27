package org.apache.batik.bridge;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.Enumeration;
public class DocumentJarClassLoader extends URLClassLoader {
    protected CodeSource documentCodeSource = null;
    public DocumentJarClassLoader(URL jarURL,
                                  URL documentURL){
        super(new URL[]{jarURL});
        if (documentURL != null) {
            documentCodeSource = new CodeSource
                (documentURL, (Certificate[])null);
        }
    }
    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        Policy p = Policy.getPolicy();
        PermissionCollection pc = null;
        if (p != null) {
            pc = p.getPermissions(codesource);
        }
        if (documentCodeSource != null){
            PermissionCollection urlPC 
                = super.getPermissions(documentCodeSource);
            if (pc != null) {
                Enumeration items = urlPC.elements();
                while (items.hasMoreElements()) {
                    pc.add((Permission)(items.nextElement()));
                }
            } else {
                pc = urlPC;
            }
        }
        return pc;
    }
}
