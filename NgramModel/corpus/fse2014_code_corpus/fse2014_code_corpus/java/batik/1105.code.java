package org.apache.batik.parser;
import java.io.IOException;
import java.util.Calendar;
import java.util.SimpleTimeZone;
import org.apache.batik.xml.XMLUtilities;
public abstract class TimingParser extends AbstractParser {
    protected static final int TIME_OFFSET          = 0;
    protected static final int TIME_SYNCBASE        = 1;
    protected static final int TIME_EVENTBASE       = 2;
    protected static final int TIME_REPEAT          = 3;
    protected static final int TIME_ACCESSKEY       = 4;
    protected static final int TIME_ACCESSKEY_SVG12 = 5;
    protected static final int TIME_MEDIA_MARKER    = 6;
    protected static final int TIME_WALLCLOCK       = 7;
    protected static final int TIME_INDEFINITE      = 8;
    protected boolean useSVG11AccessKeys;
    protected boolean useSVG12AccessKeys;
    public TimingParser(boolean useSVG11AccessKeys,
                        boolean useSVG12AccessKeys) {
        this.useSVG11AccessKeys = useSVG11AccessKeys;
        this.useSVG12AccessKeys = useSVG12AccessKeys;
    }
    protected Object[] parseTimingSpecifier() throws ParseException, IOException {
        skipSpaces();
        boolean escaped = false;
        if (current == '\\') {
            escaped = true;
            current = reader.read();
        }
        Object[] ret = null;
        if (current == '+' || (current == '-' && !escaped)
                || (current >= '0' && current <= '9')) {
            float offset = parseOffset();
            ret = new Object[] { new Integer(TIME_OFFSET), new Float(offset) };
        } else if (XMLUtilities.isXMLNameFirstCharacter((char) current)) {
            ret = parseIDValue(escaped);
        } else {
            reportUnexpectedCharacterError( current );
        }
        return ret;
    }
    protected String parseName() throws ParseException, IOException {
        StringBuffer sb = new StringBuffer();
        boolean midEscaped = false;
        do {
            sb.append((char) current);
            current = reader.read();
            midEscaped = false;
            if (current == '\\') {
                midEscaped = true;
                current = reader.read();
            }
        } while (XMLUtilities.isXMLNameCharacter((char) current)
                && (midEscaped || (current != '-' && current != '.')));
        return sb.toString();
    }
    protected Object[] parseIDValue(boolean escaped)
            throws ParseException, IOException {
        String id = parseName();
        if ((id.equals("accessKey") && useSVG11AccessKeys
                || id.equals("accesskey"))
                && !escaped) {
            if (current != '(') {
                reportUnexpectedCharacterError( current );
            }
            current = reader.read();
            if (current == -1) {
                reportError("end.of.stream", new Object[0]);
            }
            char key = (char) current;
            current = reader.read();
            if (current != ')') {
                reportUnexpectedCharacterError( current );
            }
            current = reader.read();
            skipSpaces();
            float offset = 0;
            if (current == '+' || current == '-') {
                offset = parseOffset();
            }
            return new Object[] { new Integer(TIME_ACCESSKEY),
                                  new Float(offset),
                                  new Character(key) };
        } else if (id.equals("accessKey") && useSVG12AccessKeys && !escaped) {
            if (current != '(') {
                reportUnexpectedCharacterError( current );
            }
            current = reader.read();
            StringBuffer keyName = new StringBuffer();
            while (current >= 'A' && current <= 'Z'
                    || current >= 'a' && current <= 'z'
                    || current >= '0' && current <= '9'
                    || current == '+') {
                keyName.append((char) current);
                current = reader.read();
            }
            if (current != ')') {
                reportUnexpectedCharacterError( current );
            }
            current = reader.read();
            skipSpaces();
            float offset = 0;
            if (current == '+' || current == '-') {
                offset = parseOffset();
            }
            return new Object[] { new Integer(TIME_ACCESSKEY_SVG12),
                                  new Float(offset),
                                  keyName.toString() };
        } else if (id.equals("wallclock") && !escaped) {
            if (current != '(') {
                reportUnexpectedCharacterError( current );
            }
            current = reader.read();
            skipSpaces();
            Calendar wallclockValue = parseWallclockValue();
            skipSpaces();
            if (current != ')') {
                reportError("character.unexpected",
                            new Object[] { new Integer(current) });
            }
            current = reader.read();
            return new Object[] { new Integer(TIME_WALLCLOCK), wallclockValue };
        } else if (id.equals("indefinite") && !escaped) {
            return new Object[] { new Integer(TIME_INDEFINITE) };
        } else {
            if (current == '.') {
                current = reader.read();
                if (current == '\\') {
                    escaped = true;
                    current = reader.read();
                }
                if (!XMLUtilities.isXMLNameFirstCharacter((char) current)) {
                    reportUnexpectedCharacterError( current );
                }
                String id2 = parseName();
                if ((id2.equals("begin") || id2.equals("end")) && !escaped) {
                    skipSpaces();
                    float offset = 0;
                    if (current == '+' || current == '-') {
                        offset = parseOffset();
                    }
                    return new Object[] { new Integer(TIME_SYNCBASE),
                                          new Float(offset),
                                          id,
                                          id2 };
                } else if (id2.equals("repeat") && !escaped) {
                    Integer repeatIteration = null;
                    if (current == '(') {
                        current = reader.read();
                        repeatIteration = new Integer(parseDigits());
                        if (current != ')') {
                            reportUnexpectedCharacterError( current );
                        }
                        current = reader.read();
                    }
                    skipSpaces();
                    float offset = 0;
                    if (current == '+' || current == '-') {
                        offset = parseOffset();
                    }
                    return new Object[] { new Integer(TIME_REPEAT),
                                          new Float(offset),
                                          id,
                                          repeatIteration };
                } else if (id2.equals("marker") && !escaped) {
                    if (current != '(') {
                        reportUnexpectedCharacterError( current );
                    }
                    String markerName = parseName();
                    if (current != ')') {
                        reportUnexpectedCharacterError( current );
                    }
                    current = reader.read();
                    return new Object[] { new Integer(TIME_MEDIA_MARKER),
                                          id,
                                          markerName };
                } else {
                    skipSpaces();
                    float offset = 0;
                    if (current == '+' || current == '-') {
                        offset = parseOffset();
                    }
                    return new Object[] { new Integer(TIME_EVENTBASE),
                                          new Float(offset),
                                          id,
                                          id2 };
                }
            } else {
                skipSpaces();
                float offset = 0;
                if (current == '+' || current == '-') {
                    offset = parseOffset();
                }
                return new Object[] { new Integer(TIME_EVENTBASE),
                                      new Float(offset),
                                      null,
                                      id };
            }
        }
    }
    protected float parseClockValue() throws ParseException, IOException {
        int d1 = parseDigits();
        float offset;
        if (current == ':') {
            current = reader.read();
            int d2 = parseDigits();
            if (current == ':') {
                current = reader.read();
                int d3 = parseDigits();
                offset = d1 * 3600 + d2 * 60 + d3;
            } else {
                offset = d1 * 60 + d2;
            }
            if (current == '.') {
                current = reader.read();
                offset += parseFraction();
            }
        } else if (current == '.') {
            current = reader.read();
            offset = (parseFraction() + d1) * parseUnit();
        } else {
            offset = d1 * parseUnit();
        }
        return offset;
    }
    protected float parseOffset() throws ParseException, IOException {
        boolean offsetNegative = false;
        if (current == '-') {
            offsetNegative = true;
            current = reader.read();
            skipSpaces();
        } else if (current == '+') {
            current = reader.read();
            skipSpaces();
        }
        if (offsetNegative) {
            return -parseClockValue();
        }
        return parseClockValue();
    }
    protected int parseDigits() throws ParseException, IOException {
        int value = 0;
        if (current < '0' || current > '9') {
            reportUnexpectedCharacterError( current );
        }
        do {
            value = value * 10 + (current - '0');
            current = reader.read();
        } while (current >= '0' && current <= '9');
        return value;
    }
    protected float parseFraction() throws ParseException, IOException {
        float value = 0;
        if (current < '0' || current > '9') {
            reportUnexpectedCharacterError( current );
        }
        float weight = 0.1f;
        do {
            value += weight * (current - '0');
            weight *= 0.1f;
            current = reader.read();
        } while (current >= '0' && current <= '9');
        return value;
    }
    protected float parseUnit() throws ParseException, IOException {
        if (current == 'h') {
            current = reader.read();
            return 3600;
        } else if (current == 'm') {
            current = reader.read();
            if (current == 'i') {
                current = reader.read();
                if (current != 'n') {
                    reportUnexpectedCharacterError( current );
                }
                current = reader.read();
                return 60;
            } else if (current == 's') {
                current = reader.read();
                return 0.001f;
            } else {
                reportUnexpectedCharacterError( current );
            }
        } else if (current == 's') {
            current = reader.read();
        }
        return 1;
    }
    protected Calendar parseWallclockValue()
            throws ParseException, IOException {
        int y = 0, M = 0, d = 0, h = 0, m = 0, s = 0, tzh = 0, tzm = 0;
        float frac = 0;
        boolean dateSpecified = false;
        boolean timeSpecified = false;
        boolean tzSpecified = false;
        boolean tzNegative = false;
        String tzn = null;
        int digits1 = parseDigits();
        do {
            if (current == '-') {
                dateSpecified = true;
                y = digits1;
                current = reader.read();
                M = parseDigits();
                if (current != '-') {
                    reportUnexpectedCharacterError( current );
                }
                current = reader.read();
                d = parseDigits();
                if (current != 'T') {
                    break;
                }
                current = reader.read();
                digits1 = parseDigits();
                if (current != ':') {
                    reportUnexpectedCharacterError( current );
                }
            }
            if (current == ':') {
                timeSpecified = true;
                h = digits1;
                current = reader.read();
                m = parseDigits();
                if (current == ':') {
                    current = reader.read();
                    s = parseDigits();
                    if (current == '.') {
                        current = reader.read();
                        frac = parseFraction();
                    }
                }
                if (current == 'Z') {
                    tzSpecified = true;
                    tzn = "UTC";
                    current = reader.read();
                } else if (current == '+' || current == '-') {
                    StringBuffer tznb = new StringBuffer();
                    tzSpecified = true;
                    if (current == '-') {
                        tzNegative = true;
                        tznb.append('-');
                    } else {
                        tznb.append('+');
                    }
                    current = reader.read();
                    tzh = parseDigits();
                    if (tzh < 10) {
                        tznb.append('0');
                    }
                    tznb.append(tzh);
                    if (current != ':') {
                        reportUnexpectedCharacterError( current );
                    }
                    tznb.append(':');
                    current = reader.read();
                    tzm = parseDigits();
                    if (tzm < 10) {
                        tznb.append('0');
                    }
                    tznb.append(tzm);
                    tzn = tznb.toString();
                }
            }
        } while (false);
        if (!dateSpecified && !timeSpecified) {
            reportUnexpectedCharacterError( current );
        }
        Calendar wallclockTime;
        if (tzSpecified) {
            int offset = (tzNegative ? -1 : 1)
                * (tzh * 3600000 + tzm * 60000);
            wallclockTime = Calendar.getInstance(new SimpleTimeZone(offset, tzn));
        } else {
            wallclockTime = Calendar.getInstance();
        }
        if (dateSpecified && timeSpecified) {
            wallclockTime.set(y, M, d, h, m, s);
        } else if (dateSpecified) {
            wallclockTime.set(y, M, d, 0, 0, 0);
        } else {
            wallclockTime.set(Calendar.HOUR, h);
            wallclockTime.set(Calendar.MINUTE, m);
            wallclockTime.set(Calendar.SECOND, s);
        }
        if (frac == 0.0f) {
            wallclockTime.set(Calendar.MILLISECOND, (int) (frac * 1000));
        } else {
            wallclockTime.set(Calendar.MILLISECOND, 0);
        }
        return wallclockTime;
    }
}
