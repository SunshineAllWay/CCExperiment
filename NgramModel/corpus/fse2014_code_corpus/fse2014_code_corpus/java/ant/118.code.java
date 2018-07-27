package org.apache.tools.ant.filters;
public class UniqFilter extends TokenFilter.ChainableReaderFilter {
    private String lastLine = null;
    public String filter(String string) {
        return lastLine == null || !lastLine.equals(string)
            ? (lastLine = string) : null;
    }
}
