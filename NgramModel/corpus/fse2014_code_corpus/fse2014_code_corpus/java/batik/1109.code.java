package org.apache.batik.parser;
import java.io.IOException;
import java.util.Calendar;
public class TimingSpecifierParser extends TimingParser {
    protected TimingSpecifierHandler timingSpecifierHandler;
    public TimingSpecifierParser(boolean useSVG11AccessKeys,
                                 boolean useSVG12AccessKeys) {
        super(useSVG11AccessKeys, useSVG12AccessKeys);
        timingSpecifierHandler = DefaultTimingSpecifierHandler.INSTANCE;
    }
    public void setTimingSpecifierHandler(TimingSpecifierHandler handler) {
        timingSpecifierHandler = handler;
    }
    public TimingSpecifierHandler getTimingSpecifierHandler() {
        return timingSpecifierHandler;
    }
    protected void doParse() throws ParseException, IOException {
        current = reader.read();
        Object[] spec = parseTimingSpecifier();
        skipSpaces();
        if (current != -1) {
            reportError("end.of.stream.expected",
                        new Object[] { new Integer(current) });
        }
        handleTimingSpecifier(spec);
    }
    protected void handleTimingSpecifier(Object[] spec) {
        int type = ((Integer) spec[0]).intValue();
        switch (type) {
            case TIME_OFFSET:
                timingSpecifierHandler.offset(((Float) spec[1]).floatValue());
                break;
            case TIME_SYNCBASE:
                timingSpecifierHandler.syncbase(((Float) spec[1]).floatValue(),
                                                (String) spec[2],
                                                (String) spec[3]);
                break;
            case TIME_EVENTBASE:
                timingSpecifierHandler.eventbase(((Float) spec[1]).floatValue(),
                                                 (String) spec[2],
                                                 (String) spec[3]);
                break;
            case TIME_REPEAT: {
                float offset = ((Float) spec[1]).floatValue();
                String syncbaseID = (String) spec[2];
                if (spec[3] == null) {
                    timingSpecifierHandler.repeat(offset, syncbaseID);
                } else {
                    timingSpecifierHandler.repeat
                        (offset, syncbaseID, ((Integer) spec[3]).intValue());
                }
                break;
            }
            case TIME_ACCESSKEY:
                timingSpecifierHandler.accesskey
                    (((Float) spec[1]).floatValue(),
                     ((Character) spec[2]).charValue());
                break;
            case TIME_ACCESSKEY_SVG12:
                timingSpecifierHandler.accessKeySVG12
                    (((Float) spec[1]).floatValue(),
                     (String) spec[2]);
                break;
            case TIME_MEDIA_MARKER:
                timingSpecifierHandler.mediaMarker((String) spec[1],
                                                   (String) spec[2]);
                break;
            case TIME_WALLCLOCK:
                timingSpecifierHandler.wallclock((Calendar) spec[1]);
                break;
            case TIME_INDEFINITE:
                timingSpecifierHandler.indefinite();
                break;
        }
    }
}
