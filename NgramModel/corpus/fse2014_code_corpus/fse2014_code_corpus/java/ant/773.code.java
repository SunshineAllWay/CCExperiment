package org.apache.tools.ant.util;
public interface FileNameMapper {
    void setFrom(String from);
    void setTo(String to);
    String[] mapFileName(String sourceFileName);
}
