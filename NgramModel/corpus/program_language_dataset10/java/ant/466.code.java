package org.apache.tools.ant.taskdefs.optional.jsp;
import java.io.File;
public interface JspMangler {
    String mapJspToJavaName(File jspFile);
    String mapPath(String path);
}