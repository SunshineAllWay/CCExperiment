package org.apache.batik.parser;
import java.io.IOException;
public class TimingSpecifierListParser extends TimingSpecifierParser {
    public TimingSpecifierListParser(boolean useSVG11AccessKeys,
                                     boolean useSVG12AccessKeys) {
        super(useSVG11AccessKeys, useSVG12AccessKeys);
        timingSpecifierHandler = DefaultTimingSpecifierListHandler.INSTANCE;
    }
    public void setTimingSpecifierListHandler
            (TimingSpecifierListHandler handler) {
        timingSpecifierHandler = handler;
    }
    public TimingSpecifierListHandler getTimingSpecifierListHandler() {
        return (TimingSpecifierListHandler) timingSpecifierHandler;
    }
    protected void doParse() throws ParseException, IOException {
        current = reader.read();
        ((TimingSpecifierListHandler) timingSpecifierHandler)
            .startTimingSpecifierList();
        skipSpaces();
        if (current != -1) {
            for (;;) {
                Object[] spec = parseTimingSpecifier();
                handleTimingSpecifier(spec);
                skipSpaces();
                if (current == -1) {
                    break;
                }
                if (current == ';') {
                    current = reader.read();
                    continue;
                }
                reportUnexpectedCharacterError( current );
            }
        }
        skipSpaces();
        if (current != -1) {
            reportUnexpectedCharacterError( current );
        }
        ((TimingSpecifierListHandler) timingSpecifierHandler)
            .endTimingSpecifierList();
    }
}
