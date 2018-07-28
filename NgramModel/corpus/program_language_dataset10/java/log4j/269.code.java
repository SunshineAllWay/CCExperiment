package org.apache.log4j;
import junit.framework.TestCase;
import org.apache.log4j.spi.ThrowableRenderer;
public class DefaultThrowableRendererTest extends TestCase {
    public DefaultThrowableRendererTest(final String name) {
        super(name);
    }
    public void testDefaultRender() {
        ThrowableRenderer r = new DefaultThrowableRenderer();
        Exception ex = new Exception();
        String[] strRep = r.doRender(ex);
        assertNotNull(strRep);
        assertTrue(strRep.length > 0);
        for(int i = 0; i < strRep.length; i++) {
            assertNotNull(strRep[i]);
        }
    }
}
