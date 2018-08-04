package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public interface ExecuteStreamHandler {
    void setProcessInputStream(OutputStream os) throws IOException;
    void setProcessErrorStream(InputStream is) throws IOException;
    void setProcessOutputStream(InputStream is) throws IOException;
    void start() throws IOException;
    void stop();
}