package org.apache.xml.serialize;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Locale;
import org.apache.xerces.util.EncodingMap;
public class Encodings
{
    static final int DEFAULT_LAST_PRINTABLE = 0x7F;
    static final int LAST_PRINTABLE_UNICODE = 0xffff;
    static final String[] UNICODE_ENCODINGS = {
        "Unicode", "UnicodeBig", "UnicodeLittle", "GB2312", "UTF8", "UTF-16",
    };
    static final String DEFAULT_ENCODING = "UTF8";
    static Hashtable _encodings = new Hashtable();
    static EncodingInfo getEncodingInfo(String encoding, boolean allowJavaNames) throws UnsupportedEncodingException {
        EncodingInfo eInfo = null;
        if (encoding == null) {
            if((eInfo = (EncodingInfo)_encodings.get(DEFAULT_ENCODING)) != null) 
                return eInfo;
            eInfo = new EncodingInfo(EncodingMap.getJava2IANAMapping(DEFAULT_ENCODING), DEFAULT_ENCODING, LAST_PRINTABLE_UNICODE);
            _encodings.put(DEFAULT_ENCODING, eInfo);
            return eInfo;
        }
        encoding = encoding.toUpperCase(Locale.ENGLISH);
        String jName = EncodingMap.getIANA2JavaMapping(encoding);
        if (jName == null) {
            if (allowJavaNames) {
                EncodingInfo.testJavaEncodingName(encoding);
                if ((eInfo = (EncodingInfo)_encodings.get(encoding)) != null) 
                    return eInfo;
                int i = 0;
                for(; i<UNICODE_ENCODINGS.length; i++) {
                    if(UNICODE_ENCODINGS[i].equalsIgnoreCase(encoding)) {
                        eInfo = new EncodingInfo(EncodingMap.getJava2IANAMapping(encoding), encoding, LAST_PRINTABLE_UNICODE);
                        break;
                    }
                }
                if(i == UNICODE_ENCODINGS.length) {
                    eInfo = new EncodingInfo(EncodingMap.getJava2IANAMapping(encoding), encoding, DEFAULT_LAST_PRINTABLE);
                }
                _encodings.put(encoding, eInfo); 
                return eInfo;
            }
            throw new UnsupportedEncodingException(encoding);
        }
        if ((eInfo = (EncodingInfo)_encodings.get(jName)) != null)
            return eInfo;
        int i=0;
        for(; i<UNICODE_ENCODINGS.length; i++) {
            if(UNICODE_ENCODINGS[i].equalsIgnoreCase(jName)) {
                eInfo = new EncodingInfo(encoding, jName, LAST_PRINTABLE_UNICODE);
                break;
            }
        }
        if(i == UNICODE_ENCODINGS.length) {
            eInfo = new EncodingInfo(encoding, jName, DEFAULT_LAST_PRINTABLE);
        }
        _encodings.put(jName, eInfo); 
        return eInfo;
    }
    static final String JIS_DANGER_CHARS
    = "\\\u007e\u007f\u00a2\u00a3\u00a5\u00ac"
    +"\u2014\u2015\u2016\u2026\u203e\u203e\u2225\u222f\u301c"
    +"\uff3c\uff5e\uffe0\uffe1\uffe2\uffe3";
}
