package org.apache.tools.ant.types;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.VectorSet;
public class FilterSet extends DataType implements Cloneable {
    public static class Filter {
        String token;
        String value;
        public Filter(String token, String value) {
           setToken(token);
           setValue(value);
        }
        public Filter() {
        }
        public void setToken(String token) {
           this.token = token;
        }
        public void setValue(String value) {
           this.value = value;
        }
        public String getToken() {
           return token;
        }
        public String getValue() {
           return value;
        }
     }
    public class FiltersFile {
        public FiltersFile() {
        }
        public void setFile(File file) {
           filtersFiles.add(file);
        }
    }
    public static class OnMissing extends EnumeratedAttribute {
        private static final String[] VALUES
            = new String[] {"fail", "warn", "ignore"};
        public static final OnMissing FAIL = new OnMissing("fail");
        public static final OnMissing WARN = new OnMissing("warn");
        public static final OnMissing IGNORE = new OnMissing("ignore");
        private static final int FAIL_INDEX = 0;
        private static final int WARN_INDEX = 1;
        private static final int IGNORE_INDEX = 2;
        public OnMissing() {
        }
        public OnMissing(String value) {
            setValue(value);
        }
        public String[] getValues() {
            return VALUES;
        }
    }
    public static final String DEFAULT_TOKEN_START = "@";
    public static final String DEFAULT_TOKEN_END = "@";
    private String startOfToken = DEFAULT_TOKEN_START;
    private String endOfToken = DEFAULT_TOKEN_END;
    private Vector passedTokens;
    private boolean duplicateToken = false;
    private boolean recurse = true;
    private Hashtable filterHash = null;
    private Vector filtersFiles = new Vector();
    private OnMissing onMissingFiltersFile = OnMissing.FAIL;
    private boolean readingFiles = false;
    private int recurseDepth = 0;
    private Vector filters = new Vector();
    public FilterSet() {
    }
    protected FilterSet(FilterSet filterset) {
        super();
        this.filters = (Vector) filterset.getFilters().clone();
    }
    protected synchronized Vector getFilters() {
        if (isReference()) {
            return getRef().getFilters();
        }
        dieOnCircularReference();
        if (!readingFiles) {
            readingFiles = true;
            for (int i = 0, sz = filtersFiles.size(); i < sz; i++) {
                readFiltersFromFile((File) filtersFiles.get(i));
            }
            filtersFiles.clear();
            readingFiles = false;
        }
        return filters;
    }
    protected FilterSet getRef() {
        return (FilterSet) getCheckedRef(FilterSet.class, "filterset");
    }
    public synchronized Hashtable getFilterHash() {
        if (isReference()) {
            return getRef().getFilterHash();
        }
        dieOnCircularReference();
        if (filterHash == null) {
            filterHash = new Hashtable(getFilters().size());
            for (Enumeration e = getFilters().elements(); e.hasMoreElements();) {
               Filter filter = (Filter) e.nextElement();
               filterHash.put(filter.getToken(), filter.getValue());
            }
        }
        return filterHash;
    }
    public void setFiltersfile(File filtersFile) throws BuildException {
        if (isReference()) {
            throw tooManyAttributes();
        }
        filtersFiles.add(filtersFile);
    }
    public void setBeginToken(String startOfToken) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (startOfToken == null || "".equals(startOfToken)) {
            throw new BuildException("beginToken must not be empty");
        }
        this.startOfToken = startOfToken;
    }
    public String getBeginToken() {
        if (isReference()) {
            return getRef().getBeginToken();
        }
        return startOfToken;
    }
    public void setEndToken(String endOfToken) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (endOfToken == null || "".equals(endOfToken)) {
            throw new BuildException("endToken must not be empty");
        }
        this.endOfToken = endOfToken;
    }
    public String getEndToken() {
        if (isReference()) {
            return getRef().getEndToken();
        }
        return endOfToken;
    }
    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }
    public boolean isRecurse() {
        return recurse;
    }
    public synchronized void readFiltersFromFile(File filtersFile) throws BuildException {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (!filtersFile.exists()) {
           handleMissingFile("Could not read filters from file "
                                     + filtersFile + " as it doesn't exist.");
        }
        if (filtersFile.isFile()) {
           log("Reading filters from " + filtersFile, Project.MSG_VERBOSE);
           FileInputStream in = null;
           try {
              Properties props = new Properties();
              in = new FileInputStream(filtersFile);
              props.load(in);
              Enumeration e = props.propertyNames();
              Vector filts = getFilters();
              while (e.hasMoreElements()) {
                 String strPropName = (String) e.nextElement();
                 String strValue = props.getProperty(strPropName);
                 filts.addElement(new Filter(strPropName, strValue));
              }
           } catch (Exception ex) {
              throw new BuildException("Could not read filters from file: "
                  + filtersFile, ex);
           } finally {
              FileUtils.close(in);
           }
        } else {
           handleMissingFile(
               "Must specify a file rather than a directory in "
               + "the filtersfile attribute:" + filtersFile);
        }
        filterHash = null;
    }
    public synchronized String replaceTokens(String line) {
        return iReplaceTokens(line);
    }
    public synchronized void addFilter(Filter filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        filters.addElement(filter);
        filterHash = null;
    }
    public FiltersFile createFiltersfile() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        return new FiltersFile();
    }
    public synchronized void addFilter(String token, String value) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        addFilter(new Filter(token, value));
    }
    public synchronized void addConfiguredFilterSet(FilterSet filterSet) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        for (Enumeration e = filterSet.getFilters().elements(); e.hasMoreElements();) {
            addFilter((Filter) e.nextElement());
        }
    }
    public synchronized boolean hasFilters() {
        return getFilters().size() > 0;
    }
    public synchronized Object clone() throws BuildException {
        if (isReference()) {
            return ((FilterSet) getRef()).clone();
        }
        try {
            FilterSet fs = (FilterSet) super.clone();
            fs.filters = (Vector) getFilters().clone();
            fs.setProject(getProject());
            return fs;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
    public void setOnMissingFiltersFile(OnMissing onMissingFiltersFile) {
        this.onMissingFiltersFile = onMissingFiltersFile;
    }
    public OnMissing getOnMissingFiltersFile() {
        return onMissingFiltersFile;
    }
    private synchronized String iReplaceTokens(String line) {
        String beginToken = getBeginToken();
        String endToken = getEndToken();
        int index = line.indexOf(beginToken);
        if (index > -1) {
            Hashtable tokens = getFilterHash();
            try {
                StringBuffer b = new StringBuffer();
                int i = 0;
                String token = null;
                String value = null;
                while (index > -1) {
                    int endIndex = line.indexOf(endToken,
                        index + beginToken.length() + 1);
                    if (endIndex == -1) {
                        break;
                    }
                    token
                        = line.substring(index + beginToken.length(), endIndex);
                    b.append(line.substring(i, index));
                    if (tokens.containsKey(token)) {
                        value = (String) tokens.get(token);
                        if (recurse && !value.equals(token)) {
                            value = replaceTokens(value, token);
                        }
                        log("Replacing: " + beginToken + token + endToken
                            + " -> " + value, Project.MSG_VERBOSE);
                        b.append(value);
                        i = index + beginToken.length() + token.length()
                            + endToken.length();
                    } else {
                        b.append(beginToken.charAt(0));
                        i = index + 1;
                    }
                    index = line.indexOf(beginToken, i);
                }
                b.append(line.substring(i));
                return b.toString();
            } catch (StringIndexOutOfBoundsException e) {
                return line;
            }
        } else {
           return line;
        }
    }
    private synchronized String replaceTokens(String line, String parent)
        throws BuildException {
        String beginToken = getBeginToken();
        String endToken = getEndToken();
        if (recurseDepth == 0) {
            passedTokens = new VectorSet();
        }
        recurseDepth++;
        if (passedTokens.contains(parent) && !duplicateToken) {
            duplicateToken = true;
            System.out.println(
                "Infinite loop in tokens. Currently known tokens : "
                + passedTokens.toString() + "\nProblem token : " + beginToken
                + parent + endToken + " called from " + beginToken
                + passedTokens.lastElement().toString() + endToken);
            recurseDepth--;
            return parent;
        }
        passedTokens.addElement(parent);
        String value = iReplaceTokens(line);
        if (value.indexOf(beginToken) == -1 && !duplicateToken
                && recurseDepth == 1) {
            passedTokens = null;
        } else if (duplicateToken) {
            if (passedTokens.size() > 0) {
                value = (String) passedTokens.remove(passedTokens.size() - 1);
                if (passedTokens.size() == 0) {
                    value = beginToken + value + endToken;
                    duplicateToken = false;
                }
            }
        } else if (passedTokens.size() > 0) {
            passedTokens.remove(passedTokens.size() - 1);
        }
        recurseDepth--;
        return value;
    }
    private void handleMissingFile(String message) {
        switch (onMissingFiltersFile.getIndex()) {
        case OnMissing.IGNORE_INDEX:
            return;
        case OnMissing.FAIL_INDEX:
            throw new BuildException(message);
        case OnMissing.WARN_INDEX:
            log(message, Project.MSG_WARN);
            return;
        default:
            throw new BuildException("Invalid value for onMissingFiltersFile");
        }
    }
}
