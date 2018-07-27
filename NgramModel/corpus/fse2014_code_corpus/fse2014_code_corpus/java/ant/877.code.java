package org.apache.tools.zip;
import java.io.IOException;
import java.nio.ByteBuffer;
interface ZipEncoding {
    boolean canEncode(String name);
    ByteBuffer encode(String name) throws IOException;
    String decode(byte [] data) throws IOException;
}
