package org.apache.tools.ant.types.optional;
import org.apache.tools.ant.util.FileNameMapper;
import java.util.ArrayList;
public class ScriptMapper extends AbstractScriptComponent implements FileNameMapper {
    private ArrayList files;
    static final String[] EMPTY_STRING_ARRAY = new String[0];
    public void setFrom(String from) {
    }
    public void setTo(String to) {
    }
    public void clear() {
        files = new ArrayList(1);
    }
    public void addMappedName(String mapping) {
        files.add(mapping);
    }
    public String[] mapFileName(String sourceFileName) {
        initScriptRunner();
        getRunner().addBean("source", sourceFileName);
        clear();
        executeScript("ant_mapper");
        if (files.size() == 0) {
            return null;
        } else {
            return (String[]) files.toArray(EMPTY_STRING_ARRAY);
        }
    }
}
