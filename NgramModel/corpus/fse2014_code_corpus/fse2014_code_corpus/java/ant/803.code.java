package org.apache.tools.ant.util;
import java.io.IOException;
public interface Retryable {
    int RETRY_FOREVER = -1;
    void execute() throws IOException;
}
