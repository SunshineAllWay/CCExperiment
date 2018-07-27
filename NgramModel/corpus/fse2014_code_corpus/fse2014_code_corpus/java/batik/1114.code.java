package org.apache.batik.script;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
public interface Interpreter extends org.apache.batik.i18n.Localizable {
    String[] getMimeTypes();
    Object evaluate(Reader scriptreader, String description)
        throws InterpreterException, IOException;
    Object evaluate(Reader scriptreader)
        throws InterpreterException, IOException;
    Object evaluate(String script)
        throws InterpreterException;
    void bindObject(String name, Object object);
    void setOut(Writer output);
    void dispose();
}
