package org.apache.batik.parser;
import java.io.IOException;
public class LengthListParser extends LengthParser {
    public LengthListParser() {
        lengthHandler = DefaultLengthListHandler.INSTANCE;
    }
    public void setLengthListHandler(LengthListHandler handler) {
        lengthHandler = handler;
    }
    public LengthListHandler getLengthListHandler() {
        return (LengthListHandler)lengthHandler;
    }
    protected void doParse() throws ParseException, IOException {
        ((LengthListHandler)lengthHandler).startLengthList();
        current = reader.read();
        skipSpaces();
        try {
            for (;;) {
                lengthHandler.startLength();
                parseLength();
                lengthHandler.endLength();
                skipCommaSpaces();
                if (current == -1) {
                    break;
                }
            }
        } catch (NumberFormatException e) {
            reportUnexpectedCharacterError( current );
        }
        ((LengthListHandler)lengthHandler).endLengthList();
    }
}
