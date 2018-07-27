package org.apache.batik.parser;
import java.io.Reader;
import org.apache.batik.i18n.Localizable;
public interface Parser extends Localizable {
    void parse(Reader r) throws ParseException;
    void parse(String s) throws ParseException;
    void setErrorHandler(ErrorHandler handler);
}
