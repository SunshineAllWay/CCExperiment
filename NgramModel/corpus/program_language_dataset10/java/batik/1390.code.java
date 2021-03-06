package org.apache.batik.util;
public interface ParsedURLProtocolHandler {
    String getProtocolHandled();
    ParsedURLData parseURL(String urlStr);
    ParsedURLData parseURL(ParsedURL basepurl, String urlStr);
}
