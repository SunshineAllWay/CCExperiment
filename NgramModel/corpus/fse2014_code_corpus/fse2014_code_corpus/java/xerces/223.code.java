package org.apache.xerces.dom;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;
public class DOMImplementationSourceImpl
    implements DOMImplementationSource {
    public DOMImplementation getDOMImplementation(String features) {
        DOMImplementation impl =
            CoreDOMImplementationImpl.getDOMImplementation();
        if (testImpl(impl, features)) {
            return impl;
        }
        impl = DOMImplementationImpl.getDOMImplementation();
        if (testImpl(impl, features)) {
            return impl;
        }
        return null;
    }
    public DOMImplementationList getDOMImplementationList(String features) {
        DOMImplementation impl = CoreDOMImplementationImpl.getDOMImplementation();
        final ArrayList implementations = new ArrayList();
        if (testImpl(impl, features)) {
            implementations.add(impl);
        }
        impl = DOMImplementationImpl.getDOMImplementation();
        if (testImpl(impl, features)) {
            implementations.add(impl);
        }
        return new DOMImplementationListImpl(implementations);
    }
    boolean testImpl(DOMImplementation impl, String features) {
        StringTokenizer st = new StringTokenizer(features);
        String feature = null;
        String version = null;
        if (st.hasMoreTokens()) {
           feature = st.nextToken();
        }
        while (feature != null) {
           boolean isVersion = false;
           if (st.hasMoreTokens()) {
               char c;
               version = st.nextToken();
               c = version.charAt(0);
               switch (c) {
               case '0': case '1': case '2': case '3': case '4':
               case '5': case '6': case '7': case '8': case '9':
                   isVersion = true;
               }
           } else {
               version = null;
           }
           if (isVersion) {
               if (!impl.hasFeature(feature, version)) {
                   return false;
               }
               if (st.hasMoreTokens()) {
                   feature = st.nextToken();
               } else {
                   feature = null;
               }
           } else {
               if (!impl.hasFeature(feature, null)) {
                   return false;
               }
               feature = version;
           }
        }
        return true;
    }
}
