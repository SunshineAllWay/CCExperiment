package org.apache.tools.ant.filters;
import java.io.Reader;
public interface ChainableReader {
    Reader chain(Reader rdr);
}
