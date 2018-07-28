package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.SymbolicLinkUtils;
public class TokenizedPath {
    public static final TokenizedPath EMPTY_PATH =
        new TokenizedPath("", new String[0]);
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final SymbolicLinkUtils SYMLINK_UTILS =
        SymbolicLinkUtils.getSymbolicLinkUtils();
    private static final boolean[] CS_SCAN_ONLY = new boolean[] {true};
    private static final boolean[] CS_THEN_NON_CS = new boolean[] {true, false};
    private final String path;
    private final String tokenizedPath[];
    public TokenizedPath(String path) {
        this(path, SelectorUtils.tokenizePathAsArray(path));
    }
    public TokenizedPath(TokenizedPath parent, String child) {
        if (parent.path.length() > 0
            && parent.path.charAt(parent.path.length() - 1)
               != File.separatorChar) {
            path = parent.path + File.separatorChar + child;
        } else {
            path = parent.path + child;
        }
        tokenizedPath = new String[parent.tokenizedPath.length + 1];
        System.arraycopy(parent.tokenizedPath, 0, tokenizedPath, 0,
                         parent.tokenizedPath.length);
        tokenizedPath[parent.tokenizedPath.length] = child;
    }
     TokenizedPath(String path, String[] tokens) {
        this.path = path;
        this.tokenizedPath = tokens;
    }
    public String toString() {
        return path;
    }
    public int depth() {
        return tokenizedPath.length;
    }
     String[] getTokens() {
        return tokenizedPath;
    }
    public File findFile(File base, final boolean cs) {
        String[] tokens = tokenizedPath;
        if (FileUtils.isAbsolutePath(path)) {
            if (base == null) {
                String[] s = FILE_UTILS.dissect(path);
                base = new File(s[0]);
                tokens = SelectorUtils.tokenizePathAsArray(s[1]);
            } else {
                File f = FILE_UTILS.normalize(path);
                String s = FILE_UTILS.removeLeadingPath(base, f);
                if (s.equals(f.getAbsolutePath())) {
                    return null;
                }
                tokens = SelectorUtils.tokenizePathAsArray(s);
            }
        }
        return findFile(base, tokens, cs);
    }
    public boolean isSymlink(File base) {
        for (int i = 0; i < tokenizedPath.length; i++) {
            try {
                if ((base != null
                     && SYMLINK_UTILS.isSymbolicLink(base, tokenizedPath[i]))
                    ||
                    (base == null
                     && SYMLINK_UTILS.isSymbolicLink(tokenizedPath[i]))
                    ) {
                    return true;
                }
                base = new File(base, tokenizedPath[i]);
            } catch (java.io.IOException ioe) {
                String msg = "IOException caught while checking "
                    + "for links, couldn't get canonical path!";
                System.err.println(msg);
            }
        }
        return false;
    }
    public boolean equals(Object o) {
        return o instanceof TokenizedPath
            && path.equals(((TokenizedPath) o).path);
    }
    public int hashCode() {
        return path.hashCode();
    }
    private static File findFile(File base, final String[] pathElements,
                                 final boolean cs) {
        for (int current = 0; current < pathElements.length; current++) {
            if (!base.isDirectory()) {
                return null;
            }
            String[] files = base.list();
            if (files == null) {
                throw new BuildException("IO error scanning directory "
                                         + base.getAbsolutePath());
            }
            boolean found = false;
            boolean[] matchCase = cs ? CS_SCAN_ONLY : CS_THEN_NON_CS;
            for (int i = 0; !found && i < matchCase.length; i++) {
                for (int j = 0; !found && j < files.length; j++) {
                    if (matchCase[i]
                        ? files[j].equals(pathElements[current])
                        : files[j].equalsIgnoreCase(pathElements[current])) {
                        base = new File(base, files[j]);
                        found = true;
                    }
                }
            }
            if (!found) {
                return null;
            }
        }
        return pathElements.length == 0 && !base.isDirectory() ? null : base;
    }
    public TokenizedPattern toPattern() {
        return new TokenizedPattern(path, tokenizedPath); 
    }
}
