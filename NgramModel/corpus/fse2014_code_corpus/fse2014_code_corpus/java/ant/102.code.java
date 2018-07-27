package org.apache.tools.ant.filters;
import java.io.IOException;
import java.io.Reader;
import org.apache.tools.ant.Project;
public final class ExpandProperties
    extends BaseFilterReader
    implements ChainableReader {
    private String queuedData = null;
    public ExpandProperties() {
        super();
    }
    public ExpandProperties(final Reader in) {
        super(in);
    }
    public int read() throws IOException {
        int ch = -1;
        if (queuedData != null && queuedData.length() == 0) {
            queuedData = null;
        }
        if (queuedData != null) {
            ch = queuedData.charAt(0);
            queuedData = queuedData.substring(1);
            if (queuedData.length() == 0) {
                queuedData = null;
            }
        } else {
            queuedData = readFully();
            if (queuedData == null || queuedData.length() == 0) {
                ch = -1;
            } else {
                Project project = getProject();
                queuedData = project.replaceProperties(queuedData);
                return read();
            }
        }
        return ch;
    }
    public Reader chain(final Reader rdr) {
        ExpandProperties newFilter = new ExpandProperties(rdr);
        newFilter.setProject(getProject());
        return newFilter;
    }
}
