package org.apache.tools.ant.types.selectors;
public interface SelectorScanner {
    void setSelectors(FileSelector[] selectors);
    String[] getDeselectedDirectories();
    String[] getDeselectedFiles();
}
