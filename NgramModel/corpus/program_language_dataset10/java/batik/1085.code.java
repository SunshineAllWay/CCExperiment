package org.apache.batik.parser;
import java.io.IOException;
import org.apache.batik.xml.XMLUtilities;
public class FragmentIdentifierParser extends NumberParser {
    protected char[] buffer = new char[16];
    protected int bufferSize;
    protected FragmentIdentifierHandler fragmentIdentifierHandler;
    public FragmentIdentifierParser() {
        fragmentIdentifierHandler =
            DefaultFragmentIdentifierHandler.INSTANCE;
    }
    public void
        setFragmentIdentifierHandler(FragmentIdentifierHandler handler) {
        fragmentIdentifierHandler = handler;
    }
    public FragmentIdentifierHandler getFragmentIdentifierHandler() {
        return fragmentIdentifierHandler;
    }
    protected void doParse() throws ParseException, IOException {
        bufferSize = 0;
        current = reader.read();
        fragmentIdentifierHandler.startFragmentIdentifier();
        ident: {
            String id = null;
            switch (current) {
            case 'x':
                bufferize();
                current = reader.read();
                if (current != 'p') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'o') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'i') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'n') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 't') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'e') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'r') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != '(') {
                    parseIdentifier();
                    break;
                }
                bufferSize = 0;
                current = reader.read();
                if (current != 'i') {
                    reportCharacterExpectedError( 'i', current );
                    break ident;
                }
                current = reader.read();
                if (current != 'd') {
                    reportCharacterExpectedError( 'd', current );
                    break ident;
                }
                current = reader.read();
                if (current != '(') {
                    reportCharacterExpectedError( '(', current );
                    break ident;
                }
                current = reader.read();
                if (current != '"' && current != '\'') {
                    reportCharacterExpectedError( '\'', current );
                    break ident;
                }
                char q = (char)current;
                current = reader.read();
                parseIdentifier();
                id = getBufferContent();
                bufferSize = 0;
                fragmentIdentifierHandler.idReference(id);
                if (current != q) {
                    reportCharacterExpectedError( q, current );
                    break ident;
                }
                current = reader.read();
                if (current != ')') {
                    reportCharacterExpectedError( ')', current );
                    break ident;
                }
                current = reader.read();
                if (current != ')') {
                    reportCharacterExpectedError( ')', current );
                }
                break ident;
            case 's':
                bufferize();
                current = reader.read();
                if (current != 'v') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'g') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'V') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'i') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'e') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != 'w') {
                    parseIdentifier();
                    break;
                }
                bufferize();
                current = reader.read();
                if (current != '(') {
                    parseIdentifier();
                    break;
                }
                bufferSize = 0;
                current = reader.read();
                parseViewAttributes();
                if (current != ')') {
                    reportCharacterExpectedError( ')', current );
                }
                break ident;
            default:
                if (current == -1 ||
                    !XMLUtilities.isXMLNameFirstCharacter((char)current)) {
                    break ident;
                }
                bufferize();
                current = reader.read();
                parseIdentifier();
            }
            id = getBufferContent();
            fragmentIdentifierHandler.idReference(id);
        }
        fragmentIdentifierHandler.endFragmentIdentifier();
    }
    protected void parseViewAttributes() throws ParseException, IOException {
        boolean first = true;
        loop: for (;;) {
            switch (current) {
            case -1:
            case ')':
                if (first) {
                    reportUnexpectedCharacterError( current );
                    break loop;
                }
            default:
                break loop;
            case ';':
                if (first) {
                    reportUnexpectedCharacterError( current );
                    break loop;
                }
                current = reader.read();
                break;
            case 'v':
                first = false;
                current = reader.read();
                if (current != 'i') {
                    reportCharacterExpectedError( 'i', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'e') {
                    reportCharacterExpectedError( 'e', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'w') {
                    reportCharacterExpectedError( 'w', current );
                    break loop;
                }
                current = reader.read();
                switch (current) {
                case 'B':
                    current = reader.read();
                    if (current != 'o') {
                        reportCharacterExpectedError( 'o', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'x') {
                        reportCharacterExpectedError( 'x', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != '(') {
                        reportCharacterExpectedError( '(', current );
                        break loop;
                    }
                    current = reader.read();
                    float x = parseFloat();
                    if (current != ',') {
                        reportCharacterExpectedError( ',', current );
                        break loop;
                    }
                    current = reader.read();
                    float y = parseFloat();
                    if (current != ',') {
                        reportCharacterExpectedError( ',', current );
                        break loop;
                    }
                    current = reader.read();
                    float w = parseFloat();
                    if (current != ',') {
                        reportCharacterExpectedError( ',', current );
                        break loop;
                    }
                    current = reader.read();
                    float h = parseFloat();
                    if (current != ')') {
                        reportCharacterExpectedError( ')', current );
                        break loop;
                    }
                    current = reader.read();
                    fragmentIdentifierHandler.viewBox(x, y, w, h);
                    if (current != ')' && current != ';') {
                        reportCharacterExpectedError( ')', current );
                        break loop;
                    }
                    break;
                case 'T':
                    current = reader.read();
                    if (current != 'a') {
                        reportCharacterExpectedError( 'a', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'r') {
                        reportCharacterExpectedError( 'r', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'g') {
                        reportCharacterExpectedError( 'g', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'e') {
                        reportCharacterExpectedError( 'e', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 't') {
                        reportCharacterExpectedError( 't', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != '(') {
                        reportCharacterExpectedError( '(', current );
                        break loop;
                    }
                    current = reader.read();
                    fragmentIdentifierHandler.startViewTarget();
                    id: for (;;) {
                        bufferSize = 0;
                        if (current == -1 ||
                            !XMLUtilities.isXMLNameFirstCharacter((char)current)) {
                            reportUnexpectedCharacterError( current );
                            break loop;
                        }
                        bufferize();
                        current = reader.read();
                        parseIdentifier();
                        String s = getBufferContent();
                        fragmentIdentifierHandler.viewTarget(s);
                        bufferSize = 0;
                        switch (current) {
                        case ')':
                            current = reader.read();
                            break id;
                        case ',':
                        case ';':
                            current = reader.read();
                            break;
                        default:
                            reportUnexpectedCharacterError( current );
                            break loop;
                        }
                    }
                    fragmentIdentifierHandler.endViewTarget();
                    break;
                default:
                    reportUnexpectedCharacterError( current );
                    break loop;
                }
                break;
            case 'p':
                first = false;
                current = reader.read();
                if (current != 'r') {
                    reportCharacterExpectedError( 'r', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'e') {
                    reportCharacterExpectedError( 'e', current );
                    break loop;
                }
                current = reader.read();
                if (current != 's') {
                    reportCharacterExpectedError( 's', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'e') {
                    reportCharacterExpectedError( 'e', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'r') {
                    reportCharacterExpectedError( 'r', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'v') {
                    reportCharacterExpectedError( 'v', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'e') {
                    reportCharacterExpectedError( 'e', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'A') {
                    reportCharacterExpectedError( 'A', current );
                    break loop;
                }
                current = reader.read();
                if (current != 's') {
                    reportCharacterExpectedError( 's', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'p') {
                    reportCharacterExpectedError( 'p', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'e') {
                    reportCharacterExpectedError( 'e', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'c') {
                    reportCharacterExpectedError( 'c', current );
                    break loop;
                }
                current = reader.read();
                if (current != 't') {
                    reportCharacterExpectedError( 't', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'R') {
                    reportCharacterExpectedError( 'R', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'a') {
                    reportCharacterExpectedError( 'a', current );
                    break loop;
                }
                current = reader.read();
                if (current != 't') {
                    reportCharacterExpectedError( 't', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'i') {
                    reportCharacterExpectedError( 'i', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'o') {
                    reportCharacterExpectedError( 'o', current );
                    break loop;
                }
                current = reader.read();
                if (current != '(') {
                    reportCharacterExpectedError( '(', current );
                    break loop;
                }
                current = reader.read();
                parsePreserveAspectRatio();
                if (current != ')') {
                    reportCharacterExpectedError( ')', current );
                    break loop;
                }
                current = reader.read();
                break;
            case 't':
                first = false;
                current = reader.read();
                if (current != 'r') {
                    reportCharacterExpectedError( 'r', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'a') {
                    reportCharacterExpectedError( 'a', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'n') {
                    reportCharacterExpectedError( 'n', current );
                    break loop;
                }
                current = reader.read();
                if (current != 's') {
                    reportCharacterExpectedError( 's', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'f') {
                    reportCharacterExpectedError( 'f', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'o') {
                    reportCharacterExpectedError( 'o', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'r') {
                    reportCharacterExpectedError( 'r', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'm') {
                    reportCharacterExpectedError( 'm', current );
                    break loop;
                }
                current = reader.read();
                if (current != '(') {
                    reportCharacterExpectedError( '(', current );
                    break loop;
                }
                fragmentIdentifierHandler.startTransformList();
                tloop: for (;;) {
                    try {
                        current = reader.read();
                        switch (current) {
                        case ',':
                            break;
                        case 'm':
                            parseMatrix();
                            break;
                        case 'r':
                            parseRotate();
                            break;
                        case 't':
                            parseTranslate();
                            break;
                        case 's':
                            current = reader.read();
                            switch (current) {
                            case 'c':
                                parseScale();
                                break;
                            case 'k':
                                parseSkew();
                                break;
                            default:
                                reportUnexpectedCharacterError( current );
                                skipTransform();
                            }
                            break;
                        default:
                            break tloop;
                        }
                    } catch (ParseException e) {
                        errorHandler.error(e);
                        skipTransform();
                    }
                }
                fragmentIdentifierHandler.endTransformList();
                break;
            case 'z':
                first = false;
                current = reader.read();
                if (current != 'o') {
                    reportCharacterExpectedError( 'o', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'o') {
                    reportCharacterExpectedError( 'o', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'm') {
                    reportCharacterExpectedError( 'm', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'A') {
                    reportCharacterExpectedError( 'A', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'n') {
                    reportCharacterExpectedError( 'n', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'd') {
                    reportCharacterExpectedError( 'd', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'P') {
                    reportCharacterExpectedError( 'P', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'a') {
                    reportCharacterExpectedError( 'a', current );
                    break loop;
                }
                current = reader.read();
                if (current != 'n') {
                    reportCharacterExpectedError( 'n', current );
                    break loop;
                }
                current = reader.read();
                if (current != '(') {
                    reportCharacterExpectedError( '(', current );
                    break loop;
                }
                current = reader.read();
                switch (current) {
                case 'm':
                    current = reader.read();
                    if (current != 'a') {
                        reportCharacterExpectedError( 'a', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'g') {
                        reportCharacterExpectedError( 'g', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'n') {
                        reportCharacterExpectedError( 'n', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'i') {
                        reportCharacterExpectedError( 'i', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'f') {
                        reportCharacterExpectedError( 'f', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'y') {
                        reportCharacterExpectedError( 'y', current );
                        break loop;
                    }
                    current = reader.read();
                    fragmentIdentifierHandler.zoomAndPan(true);
                    break;
                case 'd':
                    current = reader.read();
                    if (current != 'i') {
                        reportCharacterExpectedError( 'i', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 's') {
                        reportCharacterExpectedError( 's', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'a') {
                        reportCharacterExpectedError( 'a', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'b') {
                        reportCharacterExpectedError( 'b', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'l') {
                        reportCharacterExpectedError( 'l', current );
                        break loop;
                    }
                    current = reader.read();
                    if (current != 'e') {
                        reportCharacterExpectedError( 'e', current );
                        break loop;
                    }
                    current = reader.read();
                    fragmentIdentifierHandler.zoomAndPan(false);
                    break;
                default:
                    reportUnexpectedCharacterError( current );
                    break loop;
                }
                if (current != ')') {
                    reportCharacterExpectedError( ')', current );
                    break loop;
                }
                current = reader.read();
            }
        }
    }
    protected void parseIdentifier() throws ParseException, IOException {
        for (;;) {
            if (current == -1 ||
                !XMLUtilities.isXMLNameCharacter((char)current)) {
                break;
            }
            bufferize();
            current = reader.read();
        }
    }
    protected String getBufferContent() {
        return new String(buffer, 0, bufferSize);
    }
    protected void bufferize() {
        if (bufferSize >= buffer.length) {
            char[] t = new char[buffer.length * 2];
            System.arraycopy( buffer, 0, t, 0, bufferSize );
            buffer = t;
        }
        buffer[bufferSize++] = (char)current;
    }
    protected void skipSpaces() throws IOException {
        if (current == ',') {
            current = reader.read();
        }
    }
    protected void skipCommaSpaces() throws IOException {
        if (current == ',') {
            current = reader.read();
        }
    }
    protected void parseMatrix() throws ParseException, IOException {
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError( 'a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError( 't', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'r') {
            reportCharacterExpectedError( 'r', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'i') {
            reportCharacterExpectedError( 'i', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'x') {
            reportCharacterExpectedError( 'x', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError( '(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float a = parseFloat();
        skipCommaSpaces();
        float b = parseFloat();
        skipCommaSpaces();
        float c = parseFloat();
        skipCommaSpaces();
        float d = parseFloat();
        skipCommaSpaces();
        float e = parseFloat();
        skipCommaSpaces();
        float f = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError( ')', current );
            skipTransform();
            return;
        }
        fragmentIdentifierHandler.matrix(a, b, c, d, e, f);
    }
    protected void parseRotate() throws ParseException, IOException {
        current = reader.read();
        if (current != 'o') {
            reportCharacterExpectedError( 'o', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError( 't', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError( 'a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError( 't', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError( 'e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError( '(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float theta = parseFloat();
        skipSpaces();
        switch (current) {
        case ')':
            fragmentIdentifierHandler.rotate(theta);
            return;
        case ',':
            current = reader.read();
            skipSpaces();
        }
        float cx = parseFloat();
        skipCommaSpaces();
        float cy = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError( ')', current );
            skipTransform();
            return;
        }
        fragmentIdentifierHandler.rotate(theta, cx, cy);
    }
    protected void parseTranslate() throws ParseException, IOException {
        current = reader.read();
        if (current != 'r') {
            reportCharacterExpectedError( 'r', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError( 'a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'n') {
            reportCharacterExpectedError( 'n', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 's') {
            reportCharacterExpectedError( 's', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'l') {
            reportCharacterExpectedError( 'l', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError( 'a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 't') {
            reportCharacterExpectedError( 't', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError( 'e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError( '(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float tx = parseFloat();
        skipSpaces();
        switch (current) {
        case ')':
            fragmentIdentifierHandler.translate(tx);
            return;
        case ',':
            current = reader.read();
            skipSpaces();
        }
        float ty = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError( ')', current );
            skipTransform();
            return;
        }
        fragmentIdentifierHandler.translate(tx, ty);
    }
    protected void parseScale() throws ParseException, IOException {
        current = reader.read();
        if (current != 'a') {
            reportCharacterExpectedError( 'a', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'l') {
            reportCharacterExpectedError( 'l', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError( 'e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError( '(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float sx = parseFloat();
        skipSpaces();
        switch (current) {
        case ')':
            fragmentIdentifierHandler.scale(sx);
            return;
        case ',':
            current = reader.read();
            skipSpaces();
        }
        float sy = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError( ')', current );
            skipTransform();
            return;
        }
        fragmentIdentifierHandler.scale(sx, sy);
    }
    protected void parseSkew() throws ParseException, IOException {
        current = reader.read();
        if (current != 'e') {
            reportCharacterExpectedError( 'e', current );
            skipTransform();
            return;
        }
        current = reader.read();
        if (current != 'w') {
            reportCharacterExpectedError( 'w', current );
            skipTransform();
            return;
        }
        current = reader.read();
        boolean skewX = false;
        switch (current) {
        case 'X':
            skewX = true;
        case 'Y':
            break;
        default:
            reportCharacterExpectedError( 'X', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        if (current != '(') {
            reportCharacterExpectedError( '(', current );
            skipTransform();
            return;
        }
        current = reader.read();
        skipSpaces();
        float sk = parseFloat();
        skipSpaces();
        if (current != ')') {
            reportCharacterExpectedError( ')', current );
            skipTransform();
            return;
        }
        if (skewX) {
            fragmentIdentifierHandler.skewX(sk);
        } else {
            fragmentIdentifierHandler.skewY(sk);
        }
    }
    protected void skipTransform() throws IOException {
        loop: for (;;) {
            current = reader.read();
            switch (current) {
                case ')':
                    break loop;
                default:
                    if (current == -1) {
                        break loop;
                    }
            }
        }
    }
    protected void parsePreserveAspectRatio()
        throws ParseException, IOException {
        fragmentIdentifierHandler.startPreserveAspectRatio();
        align: switch (current) {
        case 'n':
            current = reader.read();
            if (current != 'o') {
                reportCharacterExpectedError( 'o', current );
                skipIdentifier();
                break align;
            }
            current = reader.read();
            if (current != 'n') {
                reportCharacterExpectedError( 'n', current );
                skipIdentifier();
                break align;
            }
            current = reader.read();
            if (current != 'e') {
                reportCharacterExpectedError( 'e', current );
                skipIdentifier();
                break align;
            }
            current = reader.read();
            skipSpaces();
            fragmentIdentifierHandler.none();
            break;
        case 'x':
            current = reader.read();
            if (current != 'M') {
                reportCharacterExpectedError( 'M', current );
                skipIdentifier();
                break;
            }
            current = reader.read();
            switch (current) {
            case 'a':
                current = reader.read();
                if (current != 'x') {
                    reportCharacterExpectedError( 'x', current );
                    skipIdentifier();
                    break align;
                }
                current = reader.read();
                if (current != 'Y') {
                    reportCharacterExpectedError( 'Y', current );
                    skipIdentifier();
                    break align;
                }
                current = reader.read();
                if (current != 'M') {
                    reportCharacterExpectedError( 'M', current );
                    skipIdentifier();
                    break align;
                }
                current = reader.read();
                switch (current) {
                case 'a':
                    current = reader.read();
                    if (current != 'x') {
                        reportCharacterExpectedError( 'x', current );
                        skipIdentifier();
                        break align;
                    }
                    fragmentIdentifierHandler.xMaxYMax();
                    current = reader.read();
                    break;
                case 'i':
                    current = reader.read();
                    switch (current) {
                    case 'd':
                        fragmentIdentifierHandler.xMaxYMid();
                        current = reader.read();
                        break;
                    case 'n':
                        fragmentIdentifierHandler.xMaxYMin();
                        current = reader.read();
                        break;
                    default:
                        reportUnexpectedCharacterError( current );
                        skipIdentifier();
                        break align;
                    }
                }
                break;
            case 'i':
                current = reader.read();
                switch (current) {
                case 'd':
                    current = reader.read();
                    if (current != 'Y') {
                        reportCharacterExpectedError( 'Y', current );
                        skipIdentifier();
                        break align;
                    }
                    current = reader.read();
                    if (current != 'M') {
                        reportCharacterExpectedError( 'M', current );
                        skipIdentifier();
                        break align;
                    }
                    current = reader.read();
                    switch (current) {
                    case 'a':
                        current = reader.read();
                        if (current != 'x') {
                            reportCharacterExpectedError( 'x', current );
                            skipIdentifier();
                            break align;
                        }
                        fragmentIdentifierHandler.xMidYMax();
                        current = reader.read();
                        break;
                    case 'i':
                        current = reader.read();
                        switch (current) {
                        case 'd':
                            fragmentIdentifierHandler.xMidYMid();
                            current = reader.read();
                            break;
                        case 'n':
                            fragmentIdentifierHandler.xMidYMin();
                            current = reader.read();
                            break;
                        default:
                            reportUnexpectedCharacterError( current );
                            skipIdentifier();
                            break align;
                        }
                    }
                    break;
                case 'n':
                    current = reader.read();
                    if (current != 'Y') {
                        reportCharacterExpectedError( 'Y', current );
                        skipIdentifier();
                        break align;
                    }
                    current = reader.read();
                    if (current != 'M') {
                        reportCharacterExpectedError( 'M', current );
                        skipIdentifier();
                        break align;
                    }
                    current = reader.read();
                    switch (current) {
                    case 'a':
                        current = reader.read();
                        if (current != 'x') {
                            reportCharacterExpectedError( 'x', current );
                            skipIdentifier();
                            break align;
                        }
                        fragmentIdentifierHandler.xMinYMax();
                        current = reader.read();
                        break;
                    case 'i':
                        current = reader.read();
                        switch (current) {
                        case 'd':
                            fragmentIdentifierHandler.xMinYMid();
                            current = reader.read();
                            break;
                        case 'n':
                            fragmentIdentifierHandler.xMinYMin();
                            current = reader.read();
                            break;
                        default:
                            reportUnexpectedCharacterError( current );
                            skipIdentifier();
                            break align;
                        }
                    }
                    break;
                default:
                    reportUnexpectedCharacterError( current );
                    skipIdentifier();
                    break align;
                }
                break;
            default:
                reportUnexpectedCharacterError( current );
                skipIdentifier();
            }
            break;
        default:
            if (current != -1) {
                reportUnexpectedCharacterError( current );
                skipIdentifier();
            }
        }
        skipCommaSpaces();
        switch (current) {
        case 'm':
            current = reader.read();
            if (current != 'e') {
                reportCharacterExpectedError( 'e', current );
                skipIdentifier();
                break;
            }
            current = reader.read();
            if (current != 'e') {
                reportCharacterExpectedError( 'e', current );
                skipIdentifier();
                break;
            }
            current = reader.read();
            if (current != 't') {
                reportCharacterExpectedError( 't', current );
                skipIdentifier();
                break;
            }
            fragmentIdentifierHandler.meet();
            current = reader.read();
            break;
        case 's':
            current = reader.read();
            if (current != 'l') {
                reportCharacterExpectedError( 'l', current );
                skipIdentifier();
                break;
            }
            current = reader.read();
            if (current != 'i') {
                reportCharacterExpectedError( 'i', current );
                skipIdentifier();
                break;
            }
            current = reader.read();
            if (current != 'c') {
                reportCharacterExpectedError( 'c', current );
                skipIdentifier();
                break;
            }
            current = reader.read();
            if (current != 'e') {
                reportCharacterExpectedError( 'e', current );
                skipIdentifier();
                break;
            }
            fragmentIdentifierHandler.slice();
            current = reader.read();
        }
        fragmentIdentifierHandler.endPreserveAspectRatio();
    }
    protected void skipIdentifier() throws IOException {
        loop: for (;;) {
          current = reader.read();
          switch(current) {
              case 0xD: case 0xA: case 0x20: case 0x9:
                  current = reader.read();
              case -1:
                  break loop;
          }
      }
    }
}
