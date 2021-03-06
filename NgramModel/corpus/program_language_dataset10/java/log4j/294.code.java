package org.apache.log4j;
import junit.framework.TestCase;
import java.io.CharArrayWriter;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.text.DateFormat;
public class TestLogMF extends TestCase {
    private static final Level TRACE = getTraceLevel();
    private static Level getTraceLevel() {
        try {
            return (Level) Level.class.getField("TRACE").get(null);
        } catch(Exception ex) {
            return new Level(5000, "TRACE", 7);
        }
    }
    private final Logger logger = Logger.getLogger(
            "org.apache.log4j.formatter.TestLogMF");
    public TestLogMF(String testName) {
        super(testName);
    }
    public void tearDown() {
        LogManager.resetConfiguration();
    }
    public void testClassName() {
        CharArrayWriter writer = new CharArrayWriter();
        PatternLayout layout = new PatternLayout("%C");
        WriterAppender appender = new WriterAppender(layout, writer);
        appender.activateOptions();
        Logger.getRootLogger().addAppender(appender);
        LogMF.debug(logger, null, Math.PI);
        assertEquals(TestLogMF.class.getName(), writer.toString());
    }
    public void testTraceNullPattern() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, null, Math.PI);
        assertNull(capture.getMessage());
    }
    public void testTraceNoArg() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "Hello, World", Math.PI);
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testTraceBadPattern() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "Hello, {.", Math.PI);
        assertEquals("Hello, {.", capture.getMessage());
    }
    public void testTraceMissingArg() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "Hello, {0}World", new Object[0]);
        assertEquals("Hello, {0}World", capture.getMessage());
    }
    public void testTraceString() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "Hello, {0}", "World");
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testTraceNull() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "Hello, {0}", (Object) null);
        assertEquals("Hello, null", capture.getMessage());
    }
    public void testTraceInt() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        int val = 42;
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testTraceByte() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        byte val = 42;
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testTraceShort() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        short val = 42;
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testTraceLong() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        long val = 42;
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testTraceChar() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        char val = 'C';
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration C", capture.getMessage());
    }
    public void testTraceBoolean() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        boolean val = true;
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration true", capture.getMessage());
    }
    public void testTraceFloat() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        float val = 3.14f;
        NumberFormat format = NumberFormat.getInstance();
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration "+ format.format(val), capture.getMessage());
    }
    public void testTraceDouble() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        double val = 3.14;
        NumberFormat format = NumberFormat.getInstance();
        LogMF.trace(logger, "Iteration {0}", val);
        assertEquals("Iteration "+ format.format(val), capture.getMessage());
    }
    public void testTraceTwoArg() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "{1}, {0}.", "World", "Hello");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testTraceThreeArg() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "{1}{2} {0}.", "World", "Hello", ",");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testTraceFourArg() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        LogMF.trace(logger, "{1}{2} {0}{3}", "World", "Hello", ",", ".");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testTraceArrayArg() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        Object[] args = new Object[] { "World", "Hello", ",", "." };
        LogMF.trace(logger, "{1}{2} {0}{3}", args);
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testTraceNullArrayArg() {
        LogCapture capture = new LogCapture(TRACE);
        logger.setLevel(TRACE);
        Object[] args = null;
        LogMF.trace(logger, "{1}{2} {0}{3}", args);
        assertEquals("{1}{2} {0}{3}", capture.getMessage());
    }
    public void testDebugNullPattern() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, null, Math.PI);
        assertEquals(null, capture.getMessage());
    }
    public void testDebugNoArg() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "Hello, World", Math.PI);
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testDebugBadPattern() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "Hello, {.", Math.PI);
        assertEquals("Hello, {.", capture.getMessage());
    }
    public void testDebugMissingArg() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "Hello, {0}World", new Object[0]);
        assertEquals("Hello, {0}World", capture.getMessage());
    }
    public void testDebugString() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "Hello, {0}", "World");
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testDebugNull() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "Hello, {0}", (Object) null);
        assertEquals("Hello, null", capture.getMessage());
    }
    public void testDebugInt() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        int val = 42;
        LogMF.debug(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testDebugByte() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        byte val = 42;
        LogMF.debug(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testDebugShort() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        short val = 42;
        LogMF.debug(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testDebugLong() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        long val = 42;
        LogMF.debug(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testDebugChar() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        char val = 'C';
        LogMF.debug(logger, "Iteration {0}", val);
        assertEquals("Iteration C", capture.getMessage());
    }
    public void testDebugBoolean() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        boolean val = true;
        LogMF.debug(logger, "Iteration {0}", val);
        assertEquals("Iteration true", capture.getMessage());
    }
    public void testDebugFloat() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "Iteration {0}", (float) Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Float(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testDebugDouble() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "Iteration {0}", Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Double(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testDebugTwoArg() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "{1}, {0}.", "World", "Hello");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testDebugThreeArg() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "{1}{2} {0}.", "World", "Hello", ",");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testDebugFourArg() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "{1}{2} {0}{3}", "World", "Hello", ",", ".");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testDebugArrayArg() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        Object[] args = new Object[] { "World", "Hello", ",", "." };
        LogMF.debug(logger, "{1}{2} {0}{3}", args);
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testDebugDate() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        Date epoch = new Date(0);
        LogMF.debug(logger, "Iteration {0}", epoch);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { epoch });
        String expected2 = "Iteration " + DateFormat.getDateTimeInstance(
                                DateFormat.SHORT,
                                DateFormat.SHORT).format(epoch);
        String actual = capture.getMessage();
        if (System.getProperty("java.vendor").indexOf("Free") == -1) {
            assertEquals(expected, actual);
        }
        assertEquals(expected2, actual);
    }
    public void testDebugNullArrayArg() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        Object[] args = null;
        LogMF.debug(logger, "{1}{2} {0}{3}", args);
        assertEquals("{1}{2} {0}{3}", capture.getMessage());
    }
    public void testDebugPercent() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "{0, number, percent}", Math.PI);
        String expected = java.text.MessageFormat.format("{0, number, percent}",
                new Object[] { new Double(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testDebugFullPrecisionAndPercent() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "{0}{0, number, percent}", Math.PI);
        String expected = java.text.MessageFormat.format("{0}{0, number, percent}",
                new Object[] { new Double(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testDebugQuoted() {
        LogCapture capture = new LogCapture(Level.DEBUG);
        LogMF.debug(logger, "'{0}'", "World");
        assertEquals("{0}", capture.getMessage());
    }
    public void testInfoNullPattern() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, null, Math.PI);
        assertNull(capture.getMessage());
    }
    public void testInfoNoArg() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Hello, World", Math.PI);
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testInfoBadPattern() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Hello, {.", Math.PI);
        assertEquals("Hello, {.", capture.getMessage());
    }
    public void testInfoMissingArg() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Hello, {0}World", new Object[0]);
        assertEquals("Hello, {0}World", capture.getMessage());
    }
    public void testInfoString() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Hello, {0}", "World");
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testInfoNull() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Hello, {0}", (Object) null);
        assertEquals("Hello, null", capture.getMessage());
    }
    public void testInfoInt() {
        LogCapture capture = new LogCapture(Level.INFO);
        int val = 42;
        LogMF.info(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testInfoByte() {
        LogCapture capture = new LogCapture(Level.INFO);
        byte val = 42;
        LogMF.info(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testInfoShort() {
        LogCapture capture = new LogCapture(Level.INFO);
        short val = 42;
        LogMF.info(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testInfoLong() {
        LogCapture capture = new LogCapture(Level.INFO);
        long val = 42;
        LogMF.info(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testInfoChar() {
        LogCapture capture = new LogCapture(Level.INFO);
        char val = 'C';
        LogMF.info(logger, "Iteration {0}", val);
        assertEquals("Iteration C", capture.getMessage());
    }
    public void testInfoBoolean() {
        LogCapture capture = new LogCapture(Level.INFO);
        boolean val = true;
        LogMF.info(logger, "Iteration {0}", val);
        assertEquals("Iteration true", capture.getMessage());
    }
    public void testInfoFloat() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Iteration {0}", (float) Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Float(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testInfoDouble() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Iteration {0}", Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Double(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testInfoTwoArg() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "{1}, {0}.", "World", "Hello");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testInfoThreeArg() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "{1}{2} {0}.", "World", "Hello", ",");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testInfoFourArg() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "{1}{2} {0}{3}", "World", "Hello", ",", ".");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testInfoArrayArg() {
        LogCapture capture = new LogCapture(Level.INFO);
        Object[] args = new Object[] { "World", "Hello", ",", "." };
        LogMF.info(logger, "{1}{2} {0}{3}", args);
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testWarnNullPattern() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, null, Math.PI);
        assertNull(capture.getMessage());
    }
    public void testWarnNoArg() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "Hello, World", Math.PI);
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testWarnBadPattern() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "Hello, {.", Math.PI);
        assertEquals("Hello, {.", capture.getMessage());
    }
    public void testWarnMissingArg() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "Hello, {0}World", new Object[0]);
        assertEquals("Hello, {0}World", capture.getMessage());
    }
    public void testWarnString() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "Hello, {0}", "World");
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testWarnNull() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "Hello, {0}", (Object) null);
        assertEquals("Hello, null", capture.getMessage());
    }
    public void testWarnInt() {
        LogCapture capture = new LogCapture(Level.WARN);
        int val = 42;
        LogMF.warn(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testWarnByte() {
        LogCapture capture = new LogCapture(Level.WARN);
        byte val = 42;
        LogMF.warn(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testWarnShort() {
        LogCapture capture = new LogCapture(Level.WARN);
        short val = 42;
        LogMF.warn(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testWarnLong() {
        LogCapture capture = new LogCapture(Level.WARN);
        long val = 42;
        LogMF.warn(logger, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testWarnChar() {
        LogCapture capture = new LogCapture(Level.WARN);
        char val = 'C';
        LogMF.warn(logger, "Iteration {0}", val);
        assertEquals("Iteration C", capture.getMessage());
    }
    public void testWarnBoolean() {
        LogCapture capture = new LogCapture(Level.WARN);
        boolean val = true;
        LogMF.warn(logger, "Iteration {0}", val);
        assertEquals("Iteration true", capture.getMessage());
    }
    public void testWarnFloat() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "Iteration {0}", (float) Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Float(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testWarnDouble() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "Iteration {0}", Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Double(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testWarnTwoArg() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "{1}, {0}.", "World", "Hello");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testWarnThreeArg() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "{1}{2} {0}.", "World", "Hello", ",");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testWarnFourArg() {
        LogCapture capture = new LogCapture(Level.WARN);
        LogMF.warn(logger, "{1}{2} {0}{3}", "World", "Hello", ",", ".");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testWarnArrayArg() {
        LogCapture capture = new LogCapture(Level.WARN);
        Object[] args = new Object[] { "World", "Hello", ",", "." };
        LogMF.warn(logger, "{1}{2} {0}{3}", args);
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testLogNullPattern() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, null, Math.PI);
        assertNull(capture.getMessage());
    }
    public void testLogNoArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "Hello, World", Math.PI);
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testLogBadPattern() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "Hello, {.", Math.PI);
        assertEquals("Hello, {.", capture.getMessage());
    }
    public void testLogMissingArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "Hello, {0}World", new Object[0]);
        assertEquals("Hello, {0}World", capture.getMessage());
    }
    public void testLogString() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "Hello, {0}", "World");
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testLogNull() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "Hello, {0}", (Object) null);
        assertEquals("Hello, null", capture.getMessage());
    }
    public void testLogInt() {
        LogCapture capture = new LogCapture(Level.ERROR);
        int val = 42;
        LogMF.log(logger, Level.ERROR, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogByte() {
        LogCapture capture = new LogCapture(Level.ERROR);
        byte val = 42;
        LogMF.log(logger, Level.ERROR, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogShort() {
        LogCapture capture = new LogCapture(Level.ERROR);
        short val = 42;
        LogMF.log(logger, Level.ERROR, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogLong() {
        LogCapture capture = new LogCapture(Level.ERROR);
        long val = 42;
        LogMF.log(logger, Level.ERROR, "Iteration {0}", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogChar() {
        LogCapture capture = new LogCapture(Level.ERROR);
        char val = 'C';
        LogMF.log(logger, Level.ERROR, "Iteration {0}", val);
        assertEquals("Iteration C", capture.getMessage());
    }
    public void testLogBoolean() {
        LogCapture capture = new LogCapture(Level.ERROR);
        boolean val = true;
        LogMF.log(logger, Level.ERROR, "Iteration {0}", val);
        assertEquals("Iteration true", capture.getMessage());
    }
    public void testLogFloat() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "Iteration {0}", (float) Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Float(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testLogDouble() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "Iteration {0}", Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Double(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testLogTwoArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "{1}, {0}.", "World", "Hello");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testLogThreeArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "{1}{2} {0}.", "World", "Hello", ",");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testLogFourArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.log(logger, Level.ERROR, "{1}{2} {0}{3}", "World", "Hello", ",", ".");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testLogArrayArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        Object[] args = new Object[] { "World", "Hello", ",", "." };
        LogMF.log(logger, Level.ERROR, "{1}{2} {0}{3}", args);
        assertEquals("Hello, World.", capture.getMessage());
    }
    private static final String BUNDLE_NAME =
            "org.apache.log4j.TestLogMFPatterns";
    public void testLogrbNullBundle() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, null, "Iteration0", Math.PI);
        assertEquals("Iteration0", capture.getMessage());
    }
    public void testLogrbNullKey() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, null, Math.PI);
        assertNull(capture.getMessage());
    }
    public void testLogrbNoArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Hello1", Math.PI);
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testLogrbBadPattern() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Malformed", Math.PI);
        assertEquals("Hello, {.", capture.getMessage());
    }
    public void testLogrbMissingArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Hello2", new Object[0]);
        assertEquals("Hello, {0}World", capture.getMessage());
    }
    public void testLogrbString() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Hello3", "World");
        assertEquals("Hello, World", capture.getMessage());
    }
    public void testLogrbNull() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Hello3", (Object) null);
        assertEquals("Hello, null", capture.getMessage());
    }
    public void testLogrbInt() {
        LogCapture capture = new LogCapture(Level.ERROR);
        int val = 42;
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogrbByte() {
        LogCapture capture = new LogCapture(Level.ERROR);
        byte val = 42;
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogrbShort() {
        LogCapture capture = new LogCapture(Level.ERROR);
        short val = 42;
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogrbLong() {
        LogCapture capture = new LogCapture(Level.ERROR);
        long val = 42;
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", val);
        assertEquals("Iteration 42", capture.getMessage());
    }
    public void testLogrbChar() {
        LogCapture capture = new LogCapture(Level.ERROR);
        char val = 'C';
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", val);
        assertEquals("Iteration C", capture.getMessage());
    }
    public void testLogrbBoolean() {
        LogCapture capture = new LogCapture(Level.ERROR);
        boolean val = true;
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", val);
        assertEquals("Iteration true", capture.getMessage());
    }
    public void testLogrbFloat() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", (float) Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Float(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testLogrbDouble() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR, BUNDLE_NAME, "Iteration0", Math.PI);
        String expected = MessageFormat.format("Iteration {0}",
                new Object[] { new Double(Math.PI) });
        assertEquals(expected, capture.getMessage());
    }
    public void testLogrbTwoArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR,
                BUNDLE_NAME, "Hello4", "World", "Hello");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testLogrbThreeArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR,
                BUNDLE_NAME, "Hello5", "World", "Hello", ",");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testLogrbFourArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        LogMF.logrb(logger, Level.ERROR,
                BUNDLE_NAME, "Hello6", "World", "Hello", ",", ".");
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testLogrbArrayArg() {
        LogCapture capture = new LogCapture(Level.ERROR);
        Object[] args = new Object[] { "World", "Hello", ",", "." };
        LogMF.logrb(logger, Level.ERROR,
                BUNDLE_NAME, "Hello6", args);
        assertEquals("Hello, World.", capture.getMessage());
    }
    public void testInfo1ParamBrace9() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "Hello, {9}{0}", "World");
        assertEquals("Hello, {9}World", capture.getMessage());
    }
    public void testInfo2ParamBrace9() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "{1}, {9}{0}", "World", "Hello");
        assertEquals("Hello, {9}World", capture.getMessage());
    }
    public void testInfo10ParamBrace9() {
        LogCapture capture = new LogCapture(Level.INFO);
        LogMF.info(logger, "{1}, {9}{0}",
                new Object[] { "World", "Hello", null, null, null,
                                null, null, null, null, "New " });
        assertEquals("Hello, New World", capture.getMessage());
    }
    public void testInfo1ParamBraceSlashColon() {
        LogCapture capture = new LogCapture(Level.INFO);
        String pattern = "Hello, {/}{0}{:}";
        LogMF.info(logger, pattern, "World");
        assertEquals(pattern, capture.getMessage());
    }
}
