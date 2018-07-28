package org.apache.xerces.impl;
import java.io.EOFException;
import java.io.IOException;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.XML11Char;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
public class XML11EntityScanner
    extends XMLEntityScanner {
    public XML11EntityScanner() {
        super();
    } 
    public int peekChar() throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int c = fCurrentEntity.ch[fCurrentEntity.position];
        if (fCurrentEntity.isExternal()) {
            return (c != '\r' && c != 0x85 && c != 0x2028) ? c : '\n';
        }
        else {
            return c;
        }
    } 
    public int scanChar() throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int c = fCurrentEntity.ch[fCurrentEntity.position++];
        boolean external = false;
        if (c == '\n' ||
            ((c == '\r' || c == 0x85 || c == 0x2028) && (external = fCurrentEntity.isExternal()))) {
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            if (fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = (char)c;
                load(1, false);
            }
            if (c == '\r' && external) {
                int cc = fCurrentEntity.ch[fCurrentEntity.position++];
                if (cc != '\n' && cc != 0x85) {
                    fCurrentEntity.position--;
                }
            }
            c = '\n';
        }
        fCurrentEntity.columnNumber++;
        return c;
    } 
    public String scanNmtoken() throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int offset = fCurrentEntity.position;
        do {
            char ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11Name(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        --fCurrentEntity.startPosition;
                        --fCurrentEntity.position;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        return symbol;
    } 
    public String scanName() throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int offset = fCurrentEntity.position;
        char ch = fCurrentEntity.ch[offset];
        if (XML11Char.isXML11NameStart(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    return symbol;
                }
            }
        }
        else if (XML11Char.isXML11NameHighSurrogate(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    --fCurrentEntity.position;
                    --fCurrentEntity.startPosition;
                    return null;
                }
            }
            char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
            if ( !XMLChar.isLowSurrogate(ch2) ||
                 !XML11Char.isXML11NameStart(XMLChar.supplemental(ch, ch2)) ) {
                --fCurrentEntity.position;
                return null;
            }
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                fCurrentEntity.ch[1] = ch2;
                offset = 0;
                if (load(2, false)) {
                    fCurrentEntity.columnNumber += 2;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 2);
                    return symbol;
                }
            }
        }
        else {
            return null;
        }
        do {
            ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11Name(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        --fCurrentEntity.position;
                        --fCurrentEntity.startPosition;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        return symbol;
    } 
    public String scanNCName() throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int offset = fCurrentEntity.position;
        char ch = fCurrentEntity.ch[offset];
        if (XML11Char.isXML11NCNameStart(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    return symbol;
                }
            }
        }
        else if (XML11Char.isXML11NameHighSurrogate(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    --fCurrentEntity.position;
                    --fCurrentEntity.startPosition;
                    return null;
                }
            }
            char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
            if ( !XMLChar.isLowSurrogate(ch2) ||
                 !XML11Char.isXML11NCNameStart(XMLChar.supplemental(ch, ch2)) ) {
                --fCurrentEntity.position;
                return null;
            }
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                fCurrentEntity.ch[1] = ch2;
                offset = 0;
                if (load(2, false)) {
                    fCurrentEntity.columnNumber += 2;
                    String symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 2);
                    return symbol;
                }
            }
        }
        else {
            return null;
        }
        do {
            ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11NCName(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        --fCurrentEntity.startPosition;
                        --fCurrentEntity.position;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11NCName(XMLChar.supplemental(ch, ch2)) ) {
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;
        String symbol = null;
        if (length > 0) {
            symbol = fSymbolTable.addSymbol(fCurrentEntity.ch, offset, length);
        }
        return symbol;
    } 
    public boolean scanQName(QName qname) throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int offset = fCurrentEntity.position;
        char ch = fCurrentEntity.ch[offset];
        if (XML11Char.isXML11NCNameStart(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    fCurrentEntity.columnNumber++;
                    String name = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 1);
                    qname.setValues(null, name, name, null);
                    return true;
                }
            }
        }
        else if (XML11Char.isXML11NameHighSurrogate(ch)) {
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                offset = 0;
                if (load(1, false)) {
                    --fCurrentEntity.startPosition;
                    --fCurrentEntity.position;
                    return false;
                }
            }
            char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
            if ( !XMLChar.isLowSurrogate(ch2) ||
                 !XML11Char.isXML11NCNameStart(XMLChar.supplemental(ch, ch2)) ) {
                --fCurrentEntity.position;
                return false;
            }
            if (++fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = ch;
                fCurrentEntity.ch[1] = ch2;
                offset = 0;
                if (load(2, false)) {
                    fCurrentEntity.columnNumber += 2;
                    String name = fSymbolTable.addSymbol(fCurrentEntity.ch, 0, 2);
                    qname.setValues(null, name, name, null);
                    return true;
                }
            }
        }
        else {
            return false;
        }
        int index = -1;
        boolean sawIncompleteSurrogatePair = false;
        do {
            ch = fCurrentEntity.ch[fCurrentEntity.position];
            if (XML11Char.isXML11Name(ch)) {
                if (ch == ':') {
                    if (index != -1) {
                        break;
                    }
                    index = fCurrentEntity.position;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    if (index != -1) {
                        index = index - offset;
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else if (XML11Char.isXML11NameHighSurrogate(ch)) {
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    if (index != -1) {
                        index = index - offset;
                    }
                    offset = 0;
                    if (load(length, false)) {
                        sawIncompleteSurrogatePair = true;
                        --fCurrentEntity.startPosition;
                        --fCurrentEntity.position;
                        break;
                    }
                }
                char ch2 = fCurrentEntity.ch[fCurrentEntity.position];
                if ( !XMLChar.isLowSurrogate(ch2) ||
                     !XML11Char.isXML11Name(XMLChar.supplemental(ch, ch2)) ) {
                    sawIncompleteSurrogatePair = true;
                    --fCurrentEntity.position;
                    break;
                }
                if (++fCurrentEntity.position == fCurrentEntity.count) {
                    int length = fCurrentEntity.position - offset;
                    if (length == fCurrentEntity.ch.length) {
                        char[] tmp = new char[fCurrentEntity.ch.length << 1];
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         tmp, 0, length);
                        fCurrentEntity.ch = tmp;
                    }
                    else {
                        System.arraycopy(fCurrentEntity.ch, offset,
                                         fCurrentEntity.ch, 0, length);
                    }
                    if (index != -1) {
                        index = index - offset;
                    }
                    offset = 0;
                    if (load(length, false)) {
                        break;
                    }
                }
            }
            else {
                break;
            }
        }
        while (true);
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length;
        if (length > 0) {
            String prefix = null;
            String localpart = null;
            String rawname = fSymbolTable.addSymbol(fCurrentEntity.ch,
                                                    offset, length);
            if (index != -1) {
                int prefixLength = index - offset;
                prefix = fSymbolTable.addSymbol(fCurrentEntity.ch,
                                                    offset, prefixLength);
                int len = length - prefixLength - 1;
                int startLocal = index +1;
                if (!XML11Char.isXML11NCNameStart(fCurrentEntity.ch[startLocal]) &&
                    (!XML11Char.isXML11NameHighSurrogate(fCurrentEntity.ch[startLocal]) ||
                    sawIncompleteSurrogatePair)){
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "IllegalQName",
                                               null,
                                               XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                localpart = fSymbolTable.addSymbol(fCurrentEntity.ch,
                                                   index + 1, len);
            }
            else {
                localpart = rawname;
            }
            qname.setValues(prefix, localpart, rawname, null);
            return true;
        }
        return false;
    } 
    public int scanContent(XMLString content) throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        else if (fCurrentEntity.position == fCurrentEntity.count - 1) {
            fCurrentEntity.ch[0] = fCurrentEntity.ch[fCurrentEntity.count - 1];
            load(1, false);
            fCurrentEntity.position = 0;
            fCurrentEntity.startPosition = 0;
        }
        int offset = fCurrentEntity.position;
        int c = fCurrentEntity.ch[offset];
        int newlines = 0;
        boolean external = fCurrentEntity.isExternal();
        if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ((c == '\r' ) && external) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                    int cc = fCurrentEntity.ch[fCurrentEntity.position]; 
                    if (cc == '\n' || cc == 0x85) {
                        fCurrentEntity.position++;
                        offset++;
                    }
                    else {
                        newlines++;
                    }
                }
                else if (c == '\n' || ((c == 0x85 || c == 0x2028) && external)) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                }
                else {
                    fCurrentEntity.position--;
                    break;
                }
            } while (fCurrentEntity.position < fCurrentEntity.count - 1);
            for (int i = offset; i < fCurrentEntity.position; i++) {
                fCurrentEntity.ch[i] = '\n';
            }
            int length = fCurrentEntity.position - offset;
            if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                content.setValues(fCurrentEntity.ch, offset, length);
                return -1;
            }
        }
        if (external) {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (!XML11Char.isXML11Content(c) || c == 0x85 || c == 0x2028) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        else {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (!XML11Char.isXML11InternalEntityContent(c)) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length - newlines;
        content.setValues(fCurrentEntity.ch, offset, length);
        if (fCurrentEntity.position != fCurrentEntity.count) {
            c = fCurrentEntity.ch[fCurrentEntity.position];
            if ((c == '\r' || c == 0x85 || c == 0x2028) && external) {
                c = '\n';
            }
        }
        else {
            c = -1;
        }
        return c;
    } 
    public int scanLiteral(int quote, XMLString content)
        throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        else if (fCurrentEntity.position == fCurrentEntity.count - 1) {
            fCurrentEntity.ch[0] = fCurrentEntity.ch[fCurrentEntity.count - 1];
            load(1, false);
            fCurrentEntity.startPosition = 0;
            fCurrentEntity.position = 0;
        }
        int offset = fCurrentEntity.position;
        int c = fCurrentEntity.ch[offset];
        int newlines = 0;
        boolean external = fCurrentEntity.isExternal();
        if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ((c == '\r' ) && external) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                    int cc = fCurrentEntity.ch[fCurrentEntity.position]; 
                    if (cc == '\n' || cc == 0x85) {
                        fCurrentEntity.position++;
                        offset++;
                    }
                    else {
                        newlines++;
                    }
                }
                else if (c == '\n' || ((c == 0x85 || c == 0x2028) && external)) {
                    newlines++;
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        offset = 0;
                        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                        fCurrentEntity.position = newlines;
                        fCurrentEntity.startPosition = newlines;
                        if (load(newlines, false)) {
                            break;
                        }
                    }
                }
                else {
                    fCurrentEntity.position--;
                    break;
                }
            } while (fCurrentEntity.position < fCurrentEntity.count - 1);
            for (int i = offset; i < fCurrentEntity.position; i++) {
                fCurrentEntity.ch[i] = '\n';
            }
            int length = fCurrentEntity.position - offset;
            if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                content.setValues(fCurrentEntity.ch, offset, length);
                return -1;
            }
        }
        if (external) {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (c == quote || c == '%' || !XML11Char.isXML11Content(c) 
                    || c == 0x85 || c == 0x2028) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        else {
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ((c == quote && !fCurrentEntity.literal)
                    || c == '%' || !XML11Char.isXML11InternalEntityContent(c)) {
                    fCurrentEntity.position--;
                    break;
                }
            }
        }
        int length = fCurrentEntity.position - offset;
        fCurrentEntity.columnNumber += length - newlines;
        content.setValues(fCurrentEntity.ch, offset, length);
        if (fCurrentEntity.position != fCurrentEntity.count) {
            c = fCurrentEntity.ch[fCurrentEntity.position];
            if (c == quote && fCurrentEntity.literal) {
                c = -1;
            }
        }
        else {
            c = -1;
        }
        return c;
    } 
    public boolean scanData(String delimiter, XMLStringBuffer buffer)
        throws IOException {
        boolean done = false;
        int delimLen = delimiter.length();
        char charAt0 = delimiter.charAt(0);
        boolean external = fCurrentEntity.isExternal();
        do {
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }
            boolean bNextEntity = false;
            while ((fCurrentEntity.position >= fCurrentEntity.count - delimLen)
                && (!bNextEntity))
            {
              System.arraycopy(fCurrentEntity.ch,
                               fCurrentEntity.position,
                               fCurrentEntity.ch,
                               0,
                               fCurrentEntity.count - fCurrentEntity.position);
              bNextEntity = load(fCurrentEntity.count - fCurrentEntity.position, false);
              fCurrentEntity.position = 0;
              fCurrentEntity.startPosition = 0;
            }
            if (fCurrentEntity.position >= fCurrentEntity.count - delimLen) {
                int length = fCurrentEntity.count - fCurrentEntity.position;
                buffer.append (fCurrentEntity.ch, fCurrentEntity.position, length); 
                fCurrentEntity.columnNumber += fCurrentEntity.count;
                fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                fCurrentEntity.position = fCurrentEntity.count;
                fCurrentEntity.startPosition = fCurrentEntity.count;
                load(0,true);
                return false;
            }
            int offset = fCurrentEntity.position;
            int c = fCurrentEntity.ch[offset];
            int newlines = 0;
            if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
                do {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if ((c == '\r' ) && external) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                            fCurrentEntity.position = newlines;
                            fCurrentEntity.startPosition = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                        int cc = fCurrentEntity.ch[fCurrentEntity.position]; 
                        if (cc == '\n' || cc == 0x85) {
                            fCurrentEntity.position++;
                            offset++;
                        }
                        else {
                            newlines++;
                        }
                    }
                    else if (c == '\n' || ((c == 0x85 || c == 0x2028) && external)) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
                            fCurrentEntity.position = newlines;
                            fCurrentEntity.startPosition = newlines;
                            fCurrentEntity.count = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                    }
                    else {
                        fCurrentEntity.position--;
                        break;
                    }
                } while (fCurrentEntity.position < fCurrentEntity.count - 1);
                for (int i = offset; i < fCurrentEntity.position; i++) {
                    fCurrentEntity.ch[i] = '\n';
                }
                int length = fCurrentEntity.position - offset;
                if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                    buffer.append(fCurrentEntity.ch, offset, length);
                    return true;
                }
            }
            if (external) {
                OUTER: while (fCurrentEntity.position < fCurrentEntity.count) {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if (c == charAt0) {
                        int delimOffset = fCurrentEntity.position - 1;
                        for (int i = 1; i < delimLen; i++) {
                            if (fCurrentEntity.position == fCurrentEntity.count) {
                                fCurrentEntity.position -= i;
                                break OUTER;
                            }
                            c = fCurrentEntity.ch[fCurrentEntity.position++];
                            if (delimiter.charAt(i) != c) {
                                fCurrentEntity.position--;
                                break;
                            }
                         }
                         if (fCurrentEntity.position == delimOffset + delimLen) {
                            done = true;
                            break;
                         }
                    }
                    else if (c == '\n' || c == '\r' || c == 0x85 || c == 0x2028) {
                        fCurrentEntity.position--;
                        break;
                    }
                    else if (!XML11Char.isXML11ValidLiteral(c)) {
                        fCurrentEntity.position--;
                        int length = fCurrentEntity.position - offset;
                        fCurrentEntity.columnNumber += length - newlines;
                        buffer.append(fCurrentEntity.ch, offset, length); 
                        return true;
                    }
                }
            }
            else {
                OUTER: while (fCurrentEntity.position < fCurrentEntity.count) {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if (c == charAt0) {
                        int delimOffset = fCurrentEntity.position - 1;
                        for (int i = 1; i < delimLen; i++) {
                            if (fCurrentEntity.position == fCurrentEntity.count) {
                                fCurrentEntity.position -= i;
                                break OUTER;
                            }
                            c = fCurrentEntity.ch[fCurrentEntity.position++];
                            if (delimiter.charAt(i) != c) {
                                fCurrentEntity.position--;
                                break;
                            }
                        }
                        if (fCurrentEntity.position == delimOffset + delimLen) {
                            done = true;
                            break;
                        }
                    }
                    else if (c == '\n') {
                        fCurrentEntity.position--;
                        break;
                    }
                    else if (!XML11Char.isXML11Valid(c)) {
                        fCurrentEntity.position--;
                        int length = fCurrentEntity.position - offset;
                        fCurrentEntity.columnNumber += length - newlines;
                        buffer.append(fCurrentEntity.ch, offset, length); 
                        return true;
                    }
                }
            }
            int length = fCurrentEntity.position - offset;
            fCurrentEntity.columnNumber += length - newlines;
            if (done) {
                length -= delimLen;
            }
            buffer.append(fCurrentEntity.ch, offset, length);
        } while (!done);
        return !done;
    } 
    public boolean skipChar(int c) throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int cc = fCurrentEntity.ch[fCurrentEntity.position];
        if (cc == c) {
            fCurrentEntity.position++;
            if (c == '\n') {
                fCurrentEntity.lineNumber++;
                fCurrentEntity.columnNumber = 1;
            }
            else {
                fCurrentEntity.columnNumber++;
            }
            return true;
        }
        else if (c == '\n' && ((cc == 0x2028 || cc == 0x85) && fCurrentEntity.isExternal())) {
            fCurrentEntity.position++;
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            return true;
        }
        else if (c == '\n' && (cc == '\r' ) && fCurrentEntity.isExternal()) {
            if (fCurrentEntity.position == fCurrentEntity.count) {
                fCurrentEntity.ch[0] = (char)cc;
                load(1, false);
            }
            int ccc = fCurrentEntity.ch[++fCurrentEntity.position];
            if (ccc == '\n' || ccc == 0x85) {
                fCurrentEntity.position++;
            }
            fCurrentEntity.lineNumber++;
            fCurrentEntity.columnNumber = 1;
            return true;
        }
        return false;
    } 
    public boolean skipSpaces() throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        int c = fCurrentEntity.ch[fCurrentEntity.position];
        if (fCurrentEntity.isExternal()) {
            if (XML11Char.isXML11Space(c)) {
                do {
                    boolean entityChanged = false;
                    if (c == '\n' || c == '\r' || c == 0x85 || c == 0x2028) {
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                            fCurrentEntity.ch[0] = (char)c;
                            entityChanged = load(1, true);
                            if (!entityChanged) {
                                fCurrentEntity.startPosition = 0;
                                fCurrentEntity.position = 0;
                            }
                        }
                        if (c == '\r') {
                            int cc = fCurrentEntity.ch[++fCurrentEntity.position];
                            if (cc != '\n' && cc != 0x85 ) {
                                fCurrentEntity.position--;
                            }
                        }
                    }
                    else {
                        fCurrentEntity.columnNumber++;
                    }
                    if (!entityChanged)
                        fCurrentEntity.position++;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        load(0, true);
                    }
                } while (XML11Char.isXML11Space(c = fCurrentEntity.ch[fCurrentEntity.position]));
                return true;
            }
        }
        else if (XMLChar.isSpace(c)) {
            do {
                boolean entityChanged = false;
                if (c == '\n') {
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                    if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                        fCurrentEntity.ch[0] = (char)c;
                        entityChanged = load(1, true);
                        if (!entityChanged) {
                            fCurrentEntity.startPosition = 0;
                            fCurrentEntity.position = 0;
                        }
                    }
                }
                else {
                    fCurrentEntity.columnNumber++;
                }
                if (!entityChanged)
                    fCurrentEntity.position++;
                if (fCurrentEntity.position == fCurrentEntity.count) {
                    load(0, true);
                }
            } while (XMLChar.isSpace(c = fCurrentEntity.ch[fCurrentEntity.position]));
            return true;
        }
        return false;
    } 
    public boolean skipString(String s) throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            load(0, true);
        }
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = fCurrentEntity.ch[fCurrentEntity.position++];
            if (c != s.charAt(i)) {
                fCurrentEntity.position -= i + 1;
                return false;
            }
            if (i < length - 1 && fCurrentEntity.position == fCurrentEntity.count) {
                System.arraycopy(fCurrentEntity.ch, fCurrentEntity.count - i - 1, fCurrentEntity.ch, 0, i + 1);
                if (load(i + 1, false)) {
                    fCurrentEntity.startPosition -= i + 1;
                    fCurrentEntity.position -= i + 1;
                    return false;
                }
            }
        }
        fCurrentEntity.columnNumber += length;
        return true;
    } 
} 
