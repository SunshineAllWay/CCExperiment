package org.apache.tools.ant.util;
import java.io.Reader;
import java.io.IOException;
public interface Tokenizer {
    String getToken(Reader in) throws IOException;
    String getPostToken();
}
