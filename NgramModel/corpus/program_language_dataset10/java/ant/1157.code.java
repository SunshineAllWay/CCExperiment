package org.apache.tools.ant.util;
import java.io.IOException;
import junit.framework.TestCase;
public class SymlinkUtilsTest extends TestCase {
    private static final SymbolicLinkUtils SYMLINK_UTILS =
        SymbolicLinkUtils.getSymbolicLinkUtils();
    public void testRootIsNoSymlink() throws IOException {
        assertFalse(SYMLINK_UTILS.isSymbolicLink("/"));
    }
}
