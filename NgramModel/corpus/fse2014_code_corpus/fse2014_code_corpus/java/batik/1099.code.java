package org.apache.batik.parser;
import java.io.IOException;
public class PathParser extends NumberParser {
    protected PathHandler pathHandler;
    public PathParser() {
        pathHandler = DefaultPathHandler.INSTANCE;
    }
    public void setPathHandler(PathHandler handler) {
        pathHandler = handler;
    }
    public PathHandler getPathHandler() {
        return pathHandler;
    }
    protected void doParse() throws ParseException, IOException {
        pathHandler.startPath();
        current = reader.read();
        loop: for (;;) {
            try {
                switch (current) {
                case 0xD:
                case 0xA:
                case 0x20:
                case 0x9:
                    current = reader.read();
                    break;
                case 'z':
                case 'Z':
                    current = reader.read();
                    pathHandler.closePath();
                    break;
                case 'm': parsem(); break;
                case 'M': parseM(); break;
                case 'l': parsel(); break;
                case 'L': parseL(); break;
                case 'h': parseh(); break;
                case 'H': parseH(); break;
                case 'v': parsev(); break;
                case 'V': parseV(); break;
                case 'c': parsec(); break;
                case 'C': parseC(); break;
                case 'q': parseq(); break;
                case 'Q': parseQ(); break;
                case 's': parses(); break;
                case 'S': parseS(); break;
                case 't': parset(); break;
                case 'T': parseT(); break;
                case 'a': parsea(); break;
                case 'A': parseA(); break;
                case -1:  break loop;
                default:
                    reportUnexpected(current);
                    break;
                }
            } catch (ParseException e) {
                errorHandler.error(e);
                skipSubPath();
            }
        }
        skipSpaces();
        if (current != -1) {
            reportError("end.of.stream.expected",
                        new Object[] { new Integer(current) });
        }
        pathHandler.endPath();
    }
    protected void parsem() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        float x = parseFloat();
        skipCommaSpaces();
        float y = parseFloat();
        pathHandler.movetoRel(x, y);
        boolean expectNumber = skipCommaSpaces2();
        _parsel(expectNumber);
    }
    protected void parseM() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        float x = parseFloat();
        skipCommaSpaces();
        float y = parseFloat();
        pathHandler.movetoAbs(x, y);
        boolean expectNumber = skipCommaSpaces2();
        _parseL(expectNumber);
    }
    protected void parsel() throws ParseException, IOException {
            current = reader.read();
        skipSpaces();
        _parsel(true);
    }
    protected void _parsel(boolean expectNumber)
        throws ParseException, IOException {
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.linetoRel(x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseL() throws ParseException, IOException {
            current = reader.read();
        skipSpaces();
        _parseL(true);
    }
    protected void _parseL(boolean expectNumber)
        throws ParseException, IOException {
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.linetoAbs(x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseh() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            pathHandler.linetoHorizontalRel(x);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseH() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            pathHandler.linetoHorizontalAbs(x);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parsev() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            pathHandler.linetoVerticalRel(x);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseV() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            pathHandler.linetoVerticalAbs(x);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parsec() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x1 = parseFloat();
            skipCommaSpaces();
            float y1 = parseFloat();
            skipCommaSpaces();
            float x2 = parseFloat();
            skipCommaSpaces();
            float y2 = parseFloat();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoCubicRel(x1, y1, x2, y2, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseC() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x1 = parseFloat();
            skipCommaSpaces();
            float y1 = parseFloat();
            skipCommaSpaces();
            float x2 = parseFloat();
            skipCommaSpaces();
            float y2 = parseFloat();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoCubicAbs(x1, y1, x2, y2, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseq() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x1 = parseFloat();
            skipCommaSpaces();
            float y1 = parseFloat();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoQuadraticRel(x1, y1, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseQ() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x1 = parseFloat();
            skipCommaSpaces();
            float y1 = parseFloat();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoQuadraticAbs(x1, y1, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parses() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x2 = parseFloat();
            skipCommaSpaces();
            float y2 = parseFloat();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoCubicSmoothRel(x2, y2, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseS() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x2 = parseFloat();
            skipCommaSpaces();
            float y2 = parseFloat();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoCubicSmoothAbs(x2, y2, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parset() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoQuadraticSmoothRel(x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseT() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.curvetoQuadraticSmoothAbs(x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parsea() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float rx = parseFloat();
            skipCommaSpaces();
            float ry = parseFloat();
            skipCommaSpaces();
            float ax = parseFloat();
            skipCommaSpaces();
            boolean laf;
            switch (current) {
            default:  reportUnexpected(current); return;
            case '0': laf = false; break;
            case '1': laf = true;  break;
            }
            current = reader.read();
            skipCommaSpaces();
            boolean sf;
            switch (current) {
            default: reportUnexpected(current); return;
            case '0': sf = false; break;
            case '1': sf = true;  break;
            }
            current = reader.read();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.arcRel(rx, ry, ax, laf, sf, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void parseA() throws ParseException, IOException {
        current = reader.read();
        skipSpaces();
        boolean expectNumber = true;
        for (;;) {
            switch (current) {
            default:
                if (expectNumber) reportUnexpected(current);
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
            }
            float rx = parseFloat();
            skipCommaSpaces();
            float ry = parseFloat();
            skipCommaSpaces();
            float ax = parseFloat();
            skipCommaSpaces();
            boolean laf;
            switch (current) {
            default: reportUnexpected(current); return;
            case '0': laf = false; break;
            case '1': laf = true;  break;
            }
            current = reader.read();
            skipCommaSpaces();
            boolean sf;
            switch (current) {
            default: reportUnexpected(current); return;
            case '0': sf = false; break;
            case '1': sf = true; break;
            }
            current = reader.read();
            skipCommaSpaces();
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pathHandler.arcAbs(rx, ry, ax, laf, sf, x, y);
            expectNumber = skipCommaSpaces2();
        }
    }
    protected void skipSubPath() throws ParseException, IOException {
        for (;;) {
            switch (current) {
            case -1: case 'm': case 'M': return;
            default:                     break;
            }
            current = reader.read();
        }
    }
    protected void reportUnexpected(int ch)
        throws ParseException, IOException {
        reportUnexpectedCharacterError( current );
        skipSubPath();
    }
    protected boolean skipCommaSpaces2() throws IOException {
        wsp1: for (;;) {
            switch (current) {
            default: break wsp1;
            case 0x20: case 0x9: case 0xD: case 0xA: break;
            }
            current = reader.read();
        }
        if (current != ',')
            return false; 
        wsp2: for (;;) {
            switch (current = reader.read()) {
            default: break wsp2;
            case 0x20: case 0x9: case 0xD: case 0xA: break;
            }
        }
        return true;  
    }
}
