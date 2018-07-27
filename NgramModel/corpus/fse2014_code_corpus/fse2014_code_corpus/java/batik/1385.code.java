package org.apache.batik.util;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.Version;
public class ParsedURL {
    ParsedURLData data;
    String userAgent;
    private static Map handlersMap = null;
    private static ParsedURLProtocolHandler defaultHandler
        = new ParsedURLDefaultProtocolHandler();
    private static String globalUserAgent = "Batik/"+Version.getVersion();
    public static String getGlobalUserAgent() { return globalUserAgent; }
    public static void setGlobalUserAgent(String userAgent) {
        globalUserAgent = userAgent;
    }
    private static synchronized Map getHandlersMap() {
        if (handlersMap != null) return handlersMap;
        handlersMap = new HashMap();
        registerHandler(new ParsedURLDataProtocolHandler());
        registerHandler(new ParsedURLJarProtocolHandler());
        Iterator iter = Service.providers(ParsedURLProtocolHandler.class);
        while (iter.hasNext()) {
            ParsedURLProtocolHandler handler;
            handler = (ParsedURLProtocolHandler)iter.next();
            registerHandler(handler);
        }
        return handlersMap;
    }
    public static synchronized ParsedURLProtocolHandler getHandler
        (String protocol) {
        if (protocol == null)
            return defaultHandler;
        Map handlers = getHandlersMap();
        ParsedURLProtocolHandler ret;
        ret = (ParsedURLProtocolHandler)handlers.get(protocol);
        if (ret == null)
            ret = defaultHandler;
        return ret;
    }
    public static synchronized void registerHandler
        (ParsedURLProtocolHandler handler) {
        if (handler.getProtocolHandled() == null) {
            defaultHandler = handler;
            return;
        }
        Map handlers = getHandlersMap();
        handlers.put(handler.getProtocolHandled(), handler);
    }
    public static InputStream checkGZIP(InputStream is)
        throws IOException {
        return ParsedURLData.checkGZIP(is);
    }
    public ParsedURL(String urlStr) {
        userAgent = getGlobalUserAgent();
        data      = parseURL(urlStr);
    }
    public ParsedURL(URL url) {
        userAgent = getGlobalUserAgent();
        data      = new ParsedURLData(url);
    }
    public ParsedURL(String baseStr, String urlStr) {
        userAgent = getGlobalUserAgent();
        if (baseStr != null)
            data = parseURL(baseStr, urlStr);
        else
            data = parseURL(urlStr);
    }
    public ParsedURL(URL baseURL, String urlStr) {
        userAgent = getGlobalUserAgent();
        if (baseURL != null)
            data = parseURL(new ParsedURL(baseURL), urlStr);
        else
            data = parseURL(urlStr);
    }
    public ParsedURL(ParsedURL baseURL, String urlStr) {
        if (baseURL != null) {
            userAgent = baseURL.getUserAgent();
            data = parseURL(baseURL, urlStr);
        } else {
            data = parseURL(urlStr);
        }
    }
    public String toString() {
        return data.toString();
    }
    public String getPostConnectionURL() {
        return data.getPostConnectionURL();
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof ParsedURL))
            return false;
        ParsedURL purl = (ParsedURL)obj;
        return data.equals(purl.data);
    }
    public int hashCode() {
        return data.hashCode();
    }
    public boolean complete() {
        return data.complete();
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getProtocol() {
        if (data.protocol == null) return null;
        return data.protocol;
    }
    public String getHost() {
        if (data.host == null) return null;
        return data.host;
    }
    public int    getPort()     { return data.port; }
    public String getPath() {
        if (data.path == null) return null;
        return data.path;
    }
    public String getRef() {
        if (data.ref == null) return null;
        return data.ref;
    }
    public String getPortStr() {
        return data.getPortStr();
    }
    public String getContentType() {
        return data.getContentType(userAgent);
    }
    public String getContentTypeMediaType() {
        return data.getContentTypeMediaType(userAgent);
    }
    public String getContentTypeCharset() {
        return data.getContentTypeCharset(userAgent);
    }
    public boolean hasContentTypeParameter(String param) {
        return data.hasContentTypeParameter(userAgent, param);
    }
    public String getContentEncoding() {
        return data.getContentEncoding(userAgent);
    }
    public InputStream openStream() throws IOException {
        return data.openStream(userAgent, null);
    }
    public InputStream openStream(String mimeType) throws IOException {
        List mt = new ArrayList(1);
        mt.add(mimeType);
        return data.openStream(userAgent, mt.iterator());
    }
    public InputStream openStream(String [] mimeTypes) throws IOException {
        List mt = new ArrayList(mimeTypes.length);
        for (int i=0; i<mimeTypes.length; i++)
            mt.add(mimeTypes[i]);
        return data.openStream(userAgent, mt.iterator());
    }
    public InputStream openStream(Iterator mimeTypes) throws IOException {
        return data.openStream(userAgent, mimeTypes);
    }
    public InputStream openStreamRaw() throws IOException {
        return data.openStreamRaw(userAgent, null);
    }
    public InputStream openStreamRaw(String mimeType) throws IOException {
        List mt = new ArrayList(1);
        mt.add(mimeType);
        return data.openStreamRaw(userAgent, mt.iterator());
    }
    public InputStream openStreamRaw(String [] mimeTypes) throws IOException {
        List mt = new ArrayList(mimeTypes.length);
        mt.addAll(Arrays.asList(mimeTypes));
        return data.openStreamRaw(userAgent, mt.iterator());
    }
    public InputStream openStreamRaw(Iterator mimeTypes) throws IOException {
        return data.openStreamRaw(userAgent, mimeTypes);
    }
    public boolean sameFile(ParsedURL other) {
        return data.sameFile(other.data);
    }
    protected static String getProtocol(String urlStr) {
        if (urlStr == null) return null;
        int idx = 0, len = urlStr.length();
        if (len == 0) return null;
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
            return urlStr.substring(0, idx).toLowerCase();
        }
        return null;
    }
    public static ParsedURLData parseURL(String urlStr) {
        ParsedURLProtocolHandler handler = getHandler(getProtocol(urlStr));
        return handler.parseURL(urlStr);
    }
    public static ParsedURLData parseURL(String baseStr, String urlStr) {
        if (baseStr == null)
            return parseURL(urlStr);
        ParsedURL purl = new ParsedURL(baseStr);
        return parseURL(purl, urlStr);
    }
    public static ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
        if (baseURL == null)
            return parseURL(urlStr);
        String protocol = getProtocol(urlStr);
        if (protocol == null)
            protocol = baseURL.getProtocol();
        ParsedURLProtocolHandler handler = getHandler(protocol);
        return handler.parseURL(baseURL, urlStr);
    }
}
