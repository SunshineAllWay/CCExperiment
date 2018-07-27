package org.apache.tools.ant.loader;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import junit.framework.TestCase;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.CollectionUtils;
public class AntClassLoader5Test extends TestCase {
    public void testGetResources() throws IOException {
        AntClassLoader acl = new AntClassLoader5(new EmptyLoader(), null,
                                                 new Path(null), true);
        assertNull(acl.getResource("META-INF/MANIFEST.MF"));
        assertFalse(acl.getResources("META-INF/MANIFEST.MF").hasMoreElements());
        acl = new AntClassLoader5(null, null, new Path(null), true);
        assertNotNull(acl.getResource("META-INF/MANIFEST.MF"));
        assertTrue(acl.getResources("META-INF/MANIFEST.MF").hasMoreElements());
    }
    public void testGetResourcesUsingFactory() throws IOException {
        AntClassLoader acl =
            AntClassLoader.newAntClassLoader(new EmptyLoader(), null,
                                             new Path(null), true);
        assertNull(acl.getResource("META-INF/MANIFEST.MF"));
        assertFalse(acl.getResources("META-INF/MANIFEST.MF").hasMoreElements());
    }
    private static class EmptyLoader extends ClassLoader {
        public URL getResource(String n) {
            return null;
        }
        public Enumeration getResources(String n) {
            return new CollectionUtils.EmptyEnumeration();
        }
    }
}