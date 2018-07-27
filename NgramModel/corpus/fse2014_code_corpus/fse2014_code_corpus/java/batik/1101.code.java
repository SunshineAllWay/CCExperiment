package org.apache.batik.parser;
import java.io.IOException;
public class PointsParser extends NumberParser {
    protected PointsHandler pointsHandler;
    protected boolean eRead;
    public PointsParser() {
        pointsHandler = DefaultPointsHandler.INSTANCE;
    }
    public void setPointsHandler(PointsHandler handler) {
        pointsHandler = handler;
    }
    public PointsHandler getPointsHandler() {
        return pointsHandler;
    }
    protected void doParse() throws ParseException, IOException {
        pointsHandler.startPoints();
        current = reader.read();
        skipSpaces();
        loop: for (;;) {
            if (current == -1) {
                break loop;
            }
            float x = parseFloat();
            skipCommaSpaces();
            float y = parseFloat();
            pointsHandler.point(x, y);
            skipCommaSpaces();
        }
        pointsHandler.endPoints();
    }
}
