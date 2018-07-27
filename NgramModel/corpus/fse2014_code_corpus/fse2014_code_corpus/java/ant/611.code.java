package org.apache.tools.ant.types;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.util.FileUtils;
public class PatternSet extends DataType implements Cloneable {
    private Vector includeList = new Vector();
    private Vector excludeList = new Vector();
    private Vector includesFileList = new Vector();
    private Vector excludesFileList = new Vector();
    public class NameEntry {
        private String name;
        private Object ifCond;
        private Object unlessCond;
        public void setName(String name) {
            this.name = name;
        }
        public void setIf(Object cond) {
            ifCond = cond;
        }
        public void setIf(String cond) {
            setIf((Object) cond);
        }
        public void setUnless(Object cond) {
            unlessCond = cond;
        }
        public void setUnless(String cond) {
            setUnless((Object) cond);
        }
        public String getName() {
            return name;
        }
        public String evalName(Project p) {
            return valid(p) ? name : null;
        }
        private boolean valid(Project p) {
            PropertyHelper ph = PropertyHelper.getPropertyHelper(p);
            return ph.testIfCondition(ifCond)
                && ph.testUnlessCondition(unlessCond);
        }
        public String toString() {
            StringBuffer buf = new StringBuffer();
            if (name == null) {
                buf.append("noname");
            } else {
                buf.append(name);
            }
            if ((ifCond != null) || (unlessCond != null)) {
                buf.append(":");
                String connector = "";
                if (ifCond != null) {
                    buf.append("if->");
                    buf.append(ifCond);
                    connector = ";";
                }
                if (unlessCond != null) {
                    buf.append(connector);
                    buf.append("unless->");
                    buf.append(unlessCond);
                }
            }
            return buf.toString();
        }
    }
    private static final class InvertedPatternSet extends PatternSet {
        private InvertedPatternSet(PatternSet p) {
            setProject(p.getProject());
            addConfiguredPatternset(p);
        }
        public String[] getIncludePatterns(Project p) {
            return super.getExcludePatterns(p);
        }
        public String[] getExcludePatterns(Project p) {
            return super.getIncludePatterns(p);
        }
    }
    public PatternSet() {
        super();
    }
    public void setRefid(Reference r) throws BuildException {
        if (!includeList.isEmpty() || !excludeList.isEmpty()) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public void addConfiguredPatternset(PatternSet p) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        String[] nestedIncludes = p.getIncludePatterns(getProject());
        String[] nestedExcludes = p.getExcludePatterns(getProject());
        if (nestedIncludes != null) {
            for (int i = 0; i < nestedIncludes.length; i++) {
                createInclude().setName(nestedIncludes[i]);
            }
        }
        if (nestedExcludes != null) {
            for (int i = 0; i < nestedExcludes.length; i++) {
                createExclude().setName(nestedExcludes[i]);
            }
        }
    }
    public NameEntry createInclude() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        return addPatternToList(includeList);
    }
    public NameEntry createIncludesFile() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        return addPatternToList(includesFileList);
    }
    public NameEntry createExclude() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        return addPatternToList(excludeList);
    }
    public NameEntry createExcludesFile() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        return addPatternToList(excludesFileList);
    }
    public void setIncludes(String includes) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (includes != null && includes.length() > 0) {
            StringTokenizer tok = new StringTokenizer(includes, ", ", false);
            while (tok.hasMoreTokens()) {
                createInclude().setName(tok.nextToken());
            }
        }
    }
    public void setExcludes(String excludes) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (excludes != null && excludes.length() > 0) {
            StringTokenizer tok = new StringTokenizer(excludes, ", ", false);
            while (tok.hasMoreTokens()) {
                createExclude().setName(tok.nextToken());
            }
        }
    }
    private NameEntry addPatternToList(Vector list) {
        NameEntry result = new NameEntry();
        list.addElement(result);
        return result;
    }
     public void setIncludesfile(File includesFile) throws BuildException {
         if (isReference()) {
             throw tooManyAttributes();
         }
         createIncludesFile().setName(includesFile.getAbsolutePath());
     }
     public void setExcludesfile(File excludesFile) throws BuildException {
         if (isReference()) {
             throw tooManyAttributes();
         }
         createExcludesFile().setName(excludesFile.getAbsolutePath());
     }
    private void readPatterns(File patternfile, Vector patternlist, Project p)
            throws BuildException {
        BufferedReader patternReader = null;
        try {
            patternReader = new BufferedReader(new FileReader(patternfile));
            String line = patternReader.readLine();
            while (line != null) {
                if (line.length() > 0) {
                    line = p.replaceProperties(line);
                    addPatternToList(patternlist).setName(line);
                }
                line = patternReader.readLine();
            }
        } catch (IOException ioe)  {
            throw new BuildException("An error occurred while reading from pattern file: "
                    + patternfile, ioe);
        } finally {
            FileUtils.close(patternReader);
        }
    }
    public void append(PatternSet other, Project p) {
        if (isReference()) {
            throw new BuildException("Cannot append to a reference");
        }
        dieOnCircularReference(p);
        String[] incl = other.getIncludePatterns(p);
        if (incl != null) {
            for (int i = 0; i < incl.length; i++) {
                createInclude().setName(incl[i]);
            }
        }
        String[] excl = other.getExcludePatterns(p);
        if (excl != null) {
            for (int i = 0; i < excl.length; i++) {
                createExclude().setName(excl[i]);
            }
        }
    }
    public String[] getIncludePatterns(Project p) {
        if (isReference()) {
            return getRef(p).getIncludePatterns(p);
        }
        dieOnCircularReference(p);
        readFiles(p);
        return makeArray(includeList, p);
    }
    public String[] getExcludePatterns(Project p) {
        if (isReference()) {
            return getRef(p).getExcludePatterns(p);
        }
        dieOnCircularReference(p);
        readFiles(p);
        return makeArray(excludeList, p);
    }
    public boolean hasPatterns(Project p) {
        if (isReference()) {
            return getRef(p).hasPatterns(p);
        }
        dieOnCircularReference(p);
        return includesFileList.size() > 0 || excludesFileList.size() > 0
                || includeList.size() > 0 || excludeList.size() > 0;
    }
    private PatternSet getRef(Project p) {
        return (PatternSet) getCheckedRef(p);
    }
    private String[] makeArray(Vector list, Project p) {
        if (list.size() == 0) {
            return null;
        }
        Vector tmpNames = new Vector();
        for (Enumeration e = list.elements(); e.hasMoreElements();) {
            NameEntry ne = (NameEntry) e.nextElement();
            String pattern = ne.evalName(p);
            if (pattern != null && pattern.length() > 0) {
                tmpNames.addElement(pattern);
            }
        }
        String[] result = new String[tmpNames.size()];
        tmpNames.copyInto(result);
        return result;
    }
    private void readFiles(Project p) {
        if (includesFileList.size() > 0) {
            Enumeration e = includesFileList.elements();
            while (e.hasMoreElements()) {
                NameEntry ne = (NameEntry) e.nextElement();
                String fileName = ne.evalName(p);
                if (fileName != null) {
                    File inclFile = p.resolveFile(fileName);
                    if (!inclFile.exists()) {
                        throw new BuildException("Includesfile " + inclFile.getAbsolutePath()
                                + " not found.");
                    }
                    readPatterns(inclFile, includeList, p);
                }
            }
            includesFileList.removeAllElements();
        }
        if (excludesFileList.size() > 0) {
            Enumeration e = excludesFileList.elements();
            while (e.hasMoreElements()) {
                NameEntry ne = (NameEntry) e.nextElement();
                String fileName = ne.evalName(p);
                if (fileName != null) {
                    File exclFile = p.resolveFile(fileName);
                    if (!exclFile.exists()) {
                        throw new BuildException("Excludesfile " + exclFile.getAbsolutePath()
                                + " not found.");
                    }
                    readPatterns(exclFile, excludeList, p);
                }
            }
            excludesFileList.removeAllElements();
        }
    }
    public String toString() {
        return "patternSet{ includes: " + includeList + " excludes: " + excludeList + " }";
    }
    public Object clone() {
        try {
            PatternSet ps = (PatternSet) super.clone();
            ps.includeList = (Vector) includeList.clone();
            ps.excludeList = (Vector) excludeList.clone();
            ps.includesFileList = (Vector) includesFileList.clone();
            ps.excludesFileList = (Vector) excludesFileList.clone();
            return ps;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
    public void addConfiguredInvert(PatternSet p) {
        addConfiguredPatternset(new InvertedPatternSet(p));
    }
}
