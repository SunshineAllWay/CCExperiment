package org.apache.batik.transcoder.wmf.tosvg;
import java.io.UnsupportedEncodingException;
import org.apache.batik.transcoder.wmf.WMFConstants;
public class WMFUtilities {
    public static String decodeString(WMFFont wmfFont, byte[] bstr) {
        try {
            switch (wmfFont.charset) {
            case WMFConstants.META_CHARSET_ANSI:
                return new String(bstr, WMFConstants.CHARSET_ANSI);
            case WMFConstants.META_CHARSET_DEFAULT:
                return new String(bstr, WMFConstants.CHARSET_DEFAULT);
            case WMFConstants.META_CHARSET_SHIFTJIS:
                return new String(bstr, WMFConstants.CHARSET_SHIFTJIS);
            case WMFConstants.META_CHARSET_HANGUL:
                return new String(bstr, WMFConstants.CHARSET_HANGUL);
            case WMFConstants.META_CHARSET_JOHAB:
                return new String(bstr, WMFConstants.CHARSET_JOHAB);
            case WMFConstants.META_CHARSET_GB2312:
                return new String(bstr, WMFConstants.CHARSET_GB2312);
            case WMFConstants.META_CHARSET_CHINESEBIG5:
                return new String(bstr, WMFConstants.CHARSET_CHINESEBIG5);
            case WMFConstants.META_CHARSET_GREEK:
                return new String(bstr, WMFConstants.CHARSET_GREEK);
            case WMFConstants.META_CHARSET_TURKISH:
                return new String(bstr, WMFConstants.CHARSET_TURKISH);
            case WMFConstants.META_CHARSET_VIETNAMESE:
                return new String(bstr, WMFConstants.CHARSET_VIETNAMESE);
            case WMFConstants.META_CHARSET_HEBREW:
                return new String(bstr, WMFConstants.CHARSET_HEBREW);
            case WMFConstants.META_CHARSET_ARABIC:
                return new String(bstr, WMFConstants.CHARSET_ARABIC);
            case WMFConstants.META_CHARSET_RUSSIAN:
                return new String(bstr, WMFConstants.CHARSET_CYRILLIC);
            case WMFConstants.META_CHARSET_THAI:
                return new String(bstr, WMFConstants.CHARSET_THAI);
            case WMFConstants.META_CHARSET_EASTEUROPE:
                return new String(bstr, WMFConstants.CHARSET_EASTEUROPE);
            case WMFConstants.META_CHARSET_OEM:
                return new String(bstr, WMFConstants.CHARSET_OEM);
            default:
            }
        } catch (UnsupportedEncodingException e) {
        }
        return new String(bstr);
    }
    public static int getHorizontalAlignment(int align) {
        int v = align;
        v = v % WMFConstants.TA_BASELINE; 
        v = v % WMFConstants.TA_BOTTOM;  
        if (v >= 6) return WMFConstants.TA_CENTER;
        else if (v >= 2) return WMFConstants.TA_RIGHT;
        else return WMFConstants.TA_LEFT;
    }
    public static int getVerticalAlignment(int align) {
        int v = align;
        if ((v/WMFConstants.TA_BASELINE) != 0) return WMFConstants.TA_BASELINE;
        v = v % WMFConstants.TA_BASELINE; 
        if ((v/WMFConstants.TA_BOTTOM) != 0) return WMFConstants.TA_BOTTOM;
        else return WMFConstants.TA_TOP;
    }
}
