package org.apache.tools.ant.util;
import java.io.File;
public class PackageNameMapper extends GlobPatternMapper {
    protected String extractVariablePart(String name) {
        String var = name.substring(prefixLength,
                name.length() - postfixLength);
        return var.replace(File.separatorChar, '.');
    }
}
