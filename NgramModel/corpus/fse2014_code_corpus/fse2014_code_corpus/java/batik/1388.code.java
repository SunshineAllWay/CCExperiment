package org.apache.batik.util;
import java.net.MalformedURLException;
import java.net.URL;
public class ParsedURLDefaultProtocolHandler 
    extends AbstractParsedURLProtocolHandler {
    public ParsedURLDefaultProtocolHandler() {
        super(null);
    }
    protected ParsedURLDefaultProtocolHandler(String protocol) {
        super(protocol);
    }
    protected ParsedURLData constructParsedURLData() {
        return new ParsedURLData();
    }
    protected ParsedURLData constructParsedURLData(URL url) {
        return new ParsedURLData(url);
    }
    public ParsedURLData parseURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            return constructParsedURLData(url);
        } catch (MalformedURLException mue) {
        }
        ParsedURLData ret = constructParsedURLData();
        if (urlStr == null) return ret;
        int pidx=0, idx;
        int len = urlStr.length();
        idx = urlStr.indexOf('#');
        ret.ref = null;
        if (idx != -1) {
            if (idx+1 < len)
                ret.ref = urlStr.substring(idx+1);
            urlStr = urlStr.substring(0,idx);
            len = urlStr.length();
        }
        if (len == 0)
            return ret;
        idx = 0;
        char ch = urlStr.charAt(idx);
        while ((ch == '-') ||
               (ch == '+') ||
               (ch == '.') ||
               ((ch >= 'a') && (ch <= 'z')) ||
               ((ch >= 'A') && (ch <= 'Z'))) {
            idx++;
            if (idx == len) {
                ch=0;
                break;
            }
            ch = urlStr.charAt(idx);
        }
        if (ch == ':') {
            ret.protocol = urlStr.substring(pidx, idx).toLowerCase();
            pidx = idx+1; 
        }
        idx = urlStr.indexOf('/');
        if ((idx == -1) || ((pidx+2<len)                   &&
                            (urlStr.charAt(pidx)   == '/') &&
                            (urlStr.charAt(pidx+1) == '/'))) {
            if (idx != -1)
                pidx+=2;  
            idx = urlStr.indexOf('/', pidx);  
            String hostPort;
            if (idx == -1)
                hostPort = urlStr.substring(pidx);
            else
                hostPort = urlStr.substring(pidx, idx);
            int hidx = idx;  
            idx = hostPort.indexOf(':');
            ret.port = -1;
            if (idx == -1) {
                if (hostPort.length() == 0)
                    ret.host = null;
                else
                    ret.host = hostPort;
            } else {
                if (idx == 0) ret.host = null;
                else          ret.host = hostPort.substring(0,idx);
                if (idx+1 < hostPort.length()) {
                    String portStr = hostPort.substring(idx+1);
                    try {
                        ret.port = Integer.parseInt(portStr);
                    } catch (NumberFormatException nfe) { 
                    }
                }
            }
            if (((ret.host == null) || (ret.host.indexOf('.') == -1)) &&
                (ret.port == -1))
                ret.host = null;
            else
                pidx = hidx;
        }
        if ((pidx == -1) || (pidx >= len)) return ret; 
        ret.path = urlStr.substring(pidx);
        return ret;
    }
    public static String unescapeStr(String str) {
        int idx = str.indexOf('%');
        if (idx == -1) return str; 
        int prev=0;
        StringBuffer ret = new StringBuffer();
        while (idx != -1) {
            if (idx != prev)
                ret.append(str.substring(prev, idx));
            if (idx+2 >= str.length()) break;
            prev = idx+3;
            idx = str.indexOf('%', prev);
            int ch1 = charToHex(str.charAt(idx+1));
            int ch2 = charToHex(str.charAt(idx+1));
            if ((ch1 == -1) || (ch2==-1)) continue;
            ret.append((char)(ch1<<4 | ch2));
        }
        return ret.toString();
    }
    public static int charToHex(int ch) {
        switch(ch) {
        case '0': case '1': case '2':  case '3':  case '4': 
        case '5': case '6': case '7':  case '8':  case '9': 
            return ch-'0';
        case 'a': case 'A': return 10;
        case 'b': case 'B': return 11;
        case 'c': case 'C': return 12;
        case 'd': case 'D': return 13;
        case 'e': case 'E': return 14;
        case 'f': case 'F': return 15;
        default:            return -1;
        }
    }
    public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
        if (urlStr.length() == 0) 
            return baseURL.data;
        int idx = 0, len = urlStr.length();
        if (len == 0) return baseURL.data;
        char ch = urlStr.charAt(idx);
        while ((ch == '-') ||
               (ch == '+') ||
               (ch == '.') ||
               ((ch >= 'a') && (ch <= 'z')) ||
               ((ch >= 'A') && (ch <= 'Z'))) {
            idx++;
            if (idx == len) {
                ch=0;
                break;
            }
            ch = urlStr.charAt(idx);
        }
        String protocol = null;
        if (ch == ':') {
            protocol = urlStr.substring(0, idx).toLowerCase();
        }
        if (protocol != null) {
            if (!protocol.equals(baseURL.getProtocol()))
                return parseURL(urlStr);
            idx++;
            if (idx == urlStr.length()) 
                return parseURL(urlStr);
            if (urlStr.charAt(idx) == '/') 
                return parseURL(urlStr);
            urlStr = urlStr.substring(idx);
        }
        if (urlStr.startsWith("/")) {
            if ((urlStr.length() > 1) &&
                (urlStr.charAt(1) == '/')) {
                return parseURL(baseURL.getProtocol() + ":" + urlStr);
            }
            return parseURL(baseURL.getPortStr() + urlStr);
        }
        if (urlStr.startsWith("#")) {
            String base = baseURL.getPortStr();
            if (baseURL.getPath()    != null) base += baseURL.getPath();
            return parseURL(base + urlStr);
        }
        String path = baseURL.getPath();
        if (path == null) path = "";
        idx = path.lastIndexOf('/');
        if (idx == -1) 
            path = "";
        else
            path = path.substring(0,idx+1);
        return parseURL(baseURL.getPortStr() + path + urlStr);
    }
}
