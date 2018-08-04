package org.apache.tools.ant.taskdefs;
import java.io.File;
public interface XSLTLiaison {
    String FILE_PROTOCOL_PREFIX = "file://";
    void setStylesheet(File stylesheet) throws Exception;
    void addParam(String name, String expression) throws Exception;
    void transform(File infile, File outfile) throws Exception;
} 