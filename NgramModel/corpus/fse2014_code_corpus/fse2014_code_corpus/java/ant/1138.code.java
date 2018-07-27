package org.apache.tools.ant.types.selectors;
import java.io.File;
import junit.framework.TestCase;
public class TokenizedPatternTest extends TestCase {
    private static final String DOT_SVN_PATTERN =
        SelectorUtils.DEEP_TREE_MATCH + File.separator + ".svn"
        + File.separator + SelectorUtils.DEEP_TREE_MATCH;
    public void testTokenization() {
        TokenizedPattern pat = new TokenizedPattern(DOT_SVN_PATTERN);
        assertEquals(3, pat.depth());
        assertEquals(DOT_SVN_PATTERN, pat.getPattern());
        assertTrue(pat.containsPattern(SelectorUtils.DEEP_TREE_MATCH));
        assertTrue(pat.containsPattern(".svn"));
    }
    public void testEndsWith() {
        assertTrue(new TokenizedPattern(DOT_SVN_PATTERN)
                   .endsWith(SelectorUtils.DEEP_TREE_MATCH));
    }
    public void testWithoutLastToken() {
        assertEquals(SelectorUtils.DEEP_TREE_MATCH + File.separatorChar
                     + ".svn" + File.separator,
                     new TokenizedPattern(DOT_SVN_PATTERN)
                     .withoutLastToken().getPattern());
    }
    public void testMatchPath() {
        File f = new File(".svn");
        TokenizedPath p = new TokenizedPath(f.getAbsolutePath());
        assertTrue(new TokenizedPattern(DOT_SVN_PATTERN).matchPath(p, true));
        assertTrue(new TokenizedPattern(DOT_SVN_PATTERN)
                   .withoutLastToken().matchPath(p, true));
    }
}
