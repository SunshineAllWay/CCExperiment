package org.apache.xml.serializer;
import java.io.IOException;
interface WriterChain
{
    public void write(int val) throws IOException;
    public void write(char[] chars) throws IOException;
    public void write(char[] chars, int start, int count) throws IOException;
    public void write(String chars) throws IOException;
    public void write(String chars, int start, int count) throws IOException;
    public void flush() throws IOException;
    public void close() throws IOException;
    public java.io.Writer getWriter();
    public java.io.OutputStream getOutputStream();
}
