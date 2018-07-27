package org.apache.batik.parser;
public interface PointsHandler {
    void startPoints() throws ParseException;
    void point(float x, float y) throws ParseException;
    void endPoints() throws ParseException;
}
