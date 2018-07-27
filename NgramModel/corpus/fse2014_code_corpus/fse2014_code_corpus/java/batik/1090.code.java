package org.apache.batik.parser;
import java.io.IOException;
public class LengthPairListParser extends LengthListParser {
    protected void doParse() throws ParseException, IOException {
        ((LengthListHandler) lengthHandler).startLengthList();
        current = reader.read();
        skipSpaces();
        try {
            for (;;) {
                lengthHandler.startLength();
                parseLength();
                lengthHandler.endLength();
                skipCommaSpaces();
                lengthHandler.startLength();
                parseLength();
                lengthHandler.endLength();
                skipSpaces();
                if (current == -1) {
                    break;
                }
                if (current != ';') {
                    reportUnexpectedCharacterError( current );
                }
                current = reader.read();
                skipSpaces();
            }
        } catch (NumberFormatException e) {
            reportUnexpectedCharacterError( current );
        }
        ((LengthListHandler) lengthHandler).endLengthList();
    }
}
