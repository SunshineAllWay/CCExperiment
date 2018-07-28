package org.apache.batik.parser;
import java.io.IOException;
public class AngleParser extends NumberParser {
    protected AngleHandler angleHandler = DefaultAngleHandler.INSTANCE;
    public void setAngleHandler(AngleHandler handler) {
        angleHandler = handler;
    }
    public AngleHandler getAngleHandler() {
        return angleHandler;
    }
    protected void doParse() throws ParseException, IOException {
        angleHandler.startAngle();
        current = reader.read();
        skipSpaces();
        try {
            float f = parseFloat();
            angleHandler.angleValue(f);
            s: if (current != -1) {
                switch (current) {
                case 0xD: case 0xA: case 0x20: case 0x9:
                    break s;
                }
                switch (current) {
                case 'd':
                    current = reader.read();
                    if (current != 'e') {
                        reportCharacterExpectedError('e', current );
                        break;
                    }
                    current = reader.read();
                    if (current != 'g') {
                        reportCharacterExpectedError('g', current );
                        break;
                    }
                    angleHandler.deg();
                    current = reader.read();
                    break;
                case 'g':
                    current = reader.read();
                    if (current != 'r') {
                        reportCharacterExpectedError('r', current );
                        break;
                    }
                    current = reader.read();
                    if (current != 'a') {
                        reportCharacterExpectedError('a', current );
                        break;
                    }
                    current = reader.read();
                    if (current != 'd') {
                        reportCharacterExpectedError('d', current );
                        break;
                    }
                    angleHandler.grad();
                    current = reader.read();
                    break;
                case 'r':
                    current = reader.read();
                    if (current != 'a') {
                        reportCharacterExpectedError('a', current );
                        break;
                    }
                    current = reader.read();
                    if (current != 'd') {
                        reportCharacterExpectedError('d', current );
                        break;
                    }
                    angleHandler.rad();
                    current = reader.read();
                    break;
                default:
                    reportUnexpectedCharacterError( current );
                }
            }
            skipSpaces();
            if (current != -1) {
                reportError("end.of.stream.expected",
                            new Object[] { new Integer(current) });
            }
        } catch (NumberFormatException e) {
            reportUnexpectedCharacterError( current );
        }
        angleHandler.endAngle();
    }
}
