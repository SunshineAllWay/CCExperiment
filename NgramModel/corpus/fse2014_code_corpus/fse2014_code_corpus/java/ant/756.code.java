package org.apache.tools.ant.types.selectors.modifiedselector;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
public class HashvalueAlgorithm implements Algorithm {
    public boolean isValid() {
        return true;
    }
    public String getValue(File file) {
        Reader r = null;
        try {
            if (!file.canRead()) {
                return null;
            }
            r = new FileReader(file);
            int hash = FileUtils.readFully(r).hashCode();
            return Integer.toString(hash);
        } catch (Exception e) {
            return null;
        } finally {
            FileUtils.close(r);
        }
    }
    public String toString() {
        return "HashvalueAlgorithm";
    }
}
