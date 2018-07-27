package org.apache.batik.parser;
public interface ErrorHandler {
    void error(ParseException e) throws ParseException;
}
