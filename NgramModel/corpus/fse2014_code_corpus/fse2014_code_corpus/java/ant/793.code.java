package org.apache.tools.ant.util;
public class MergingMapper implements FileNameMapper {
    protected String[] mergedFile = null;
    public MergingMapper() {}
    public MergingMapper(String to) {
        setTo(to);
    }
    public void setFrom(String from) {
    }
    public void setTo(String to) {
        mergedFile = new String[] {to};
    }
    public String[] mapFileName(String sourceFileName) {
        return mergedFile;
    }
}
