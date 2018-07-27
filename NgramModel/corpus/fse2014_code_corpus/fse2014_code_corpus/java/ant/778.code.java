package org.apache.tools.ant.util;
import org.apache.tools.ant.BuildException;
public class GlobPatternMapper implements FileNameMapper {
    protected String fromPrefix = null;
    protected String fromPostfix = null;
    protected int prefixLength;
    protected int postfixLength;
    protected String toPrefix = null;
    protected String toPostfix = null;
    private boolean fromContainsStar = false;
    private boolean toContainsStar = false;
    private boolean handleDirSep = false;
    private boolean caseSensitive = true;
    public void setHandleDirSep(boolean handleDirSep) {
        this.handleDirSep = handleDirSep;
    }
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    public void setFrom(String from) {
        if (from != null) {
            int index = from.lastIndexOf("*");
            if (index == -1) {
                fromPrefix = from;
                fromPostfix = "";
            } else {
                fromPrefix = from.substring(0, index);
                fromPostfix = from.substring(index + 1);
                fromContainsStar = true;
            }
            prefixLength = fromPrefix.length();
            postfixLength = fromPostfix.length();
        } else {
            throw new BuildException("this mapper requires a 'from' attribute");
        }
    }
    public void setTo(String to) {
        if (to != null) {
            int index = to.lastIndexOf("*");
            if (index == -1) {
                toPrefix = to;
                toPostfix = "";
            } else {
                toPrefix = to.substring(0, index);
                toPostfix = to.substring(index + 1);
                toContainsStar = true;
            }
        } else {
            throw new BuildException("this mapper requires a 'to' attribute");
        }
    }
    public String[] mapFileName(String sourceFileName) {
        String modName = modifyName(sourceFileName);
        if (fromPrefix == null
            || (sourceFileName.length() < (prefixLength + postfixLength))
            || (!fromContainsStar
                && !modName.equals(modifyName(fromPrefix))
                )
            || (fromContainsStar
                && (!modName.startsWith(modifyName(fromPrefix))
                    || !modName.endsWith(modifyName(fromPostfix)))
                )
            ) {
            return null;
        }
        return new String[] {toPrefix
                             + (toContainsStar
                                ? extractVariablePart(sourceFileName)
                                  + toPostfix
                                : "")};
    }
    protected String extractVariablePart(String name) {
        return name.substring(prefixLength,
                              name.length() - postfixLength);
    }
    private String modifyName(String name) {
        if (!caseSensitive) {
            name = name.toLowerCase();
        }
        if (handleDirSep) {
            if (name.indexOf('\\') != -1) {
                name = name.replace('\\', '/');
            }
        }
        return name;
    }
}
