package org.apache.tools.ant.types.resources;
import java.io.IOException;
import java.io.OutputStream;
public interface Appendable {
    OutputStream getAppendOutputStream() throws IOException;
}
