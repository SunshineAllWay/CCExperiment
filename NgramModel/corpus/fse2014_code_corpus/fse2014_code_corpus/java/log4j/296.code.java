package org.apache.log4j;
import junit.framework.TestCase;
public class TestLogXF extends TestCase {
    private final Logger logger = Logger.getLogger(
            "org.apache.log4j.formatter.TestLogXF");
    public TestLogXF(String testName) {
        super(testName);
    }
    public void tearDown() {
        LogManager.resetConfiguration();
    }
    private static class BadStringifier {
        private BadStringifier() {}
        public static BadStringifier INSTANCE = new BadStringifier();
        public String toString() {
            throw new NullPointerException();
        }
    }
    public void testEnteringNullNull() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.entering(logger, null, null);
        assertEquals("null.null ENTRY", capture.getMessage());
    }
    public void testEnteringNullNullNull() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.entering(logger, null, null, (String) null);
        assertEquals("null.null ENTRY null", capture.getMessage());
    }
    public void testEnteringNullNullNullArray() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.entering(logger, null, null, (Object[]) null);
        assertEquals("null.null ENTRY {}", capture.getMessage());
    }
    public void testEntering() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.entering(logger, "SomeClass", "someMethod");
        assertEquals("SomeClass.someMethod ENTRY", capture.getMessage());
    }
    public void testEnteringWithParam() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.entering(logger, "SomeClass", "someMethod", "someParam");
        assertEquals("SomeClass.someMethod ENTRY someParam", capture.getMessage());
    }
    public void testEnteringWithBadParam() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.entering(logger, "SomeClass", "someMethod", BadStringifier.INSTANCE);
        assertEquals("SomeClass.someMethod ENTRY ?", capture.getMessage());
    }
    public void testEnteringWithBadParams() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.entering(logger, "SomeClass", "someMethod", new Object[]{"param1",BadStringifier.INSTANCE});
        assertEquals("SomeClass.someMethod ENTRY {param1,?}", capture.getMessage());
    }
    public void testExitingNullNull() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.exiting(logger, null, null);
        assertEquals("null.null RETURN", capture.getMessage());
    }
    public void testExitingNullNullNull() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.exiting(logger, null, null, (String) null);
        assertEquals("null.null RETURN null", capture.getMessage());
    }
    public void testExiting() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.exiting(logger, "SomeClass", "someMethod");
        assertEquals("SomeClass.someMethod RETURN", capture.getMessage());
    }
    public void testExitingWithValue() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.exiting(logger, "SomeClass", "someMethod", "someValue");
        assertEquals("SomeClass.someMethod RETURN someValue", capture.getMessage());
    }
    public void testExitingWithBadValue() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.exiting(logger, "SomeClass", "someMethod", BadStringifier.INSTANCE);
        assertEquals("SomeClass.someMethod RETURN ?", capture.getMessage());
    }
    public void testThrowingNullNullNull() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.throwing(logger, null, null, null);
        assertEquals("null.null THROW", capture.getMessage());
    }
    public void testThrowing() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        logger.setLevel(Level.DEBUG);
        LogXF.throwing(logger, "SomeClass", "someMethod", new IllegalArgumentException());
        assertEquals("SomeClass.someMethod THROW", capture.getMessage());
    }
}
