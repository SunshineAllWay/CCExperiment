package org.apache.log4j.net;
import junit.framework.TestCase;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;
public class SocketAppenderTest extends TestCase {
    public SocketAppenderTest(final String testName) {
	    super(testName);
    }
    protected void setUp() {
        DOMConfigurator.configure("input/xml/SocketAppenderTestConfig.xml");
        logger = Logger.getLogger(SocketAppenderTest.class);
        secondary = (LastOnlyAppender) Logger.getLogger(
                "org.apache.log4j.net.SocketAppenderTestDummy").getAppender("lastOnly");
    }
    protected void tearDown() {
    }
    public void testFallbackErrorHandlerWhenStarting() {
        String msg = "testFallbackErrorHandlerWhenStarting";
        logger.debug(msg);
        assertEquals("SocketAppender with FallbackErrorHandler", msg, secondary.getLastMessage());
    }
    private static Logger logger;
    private static LastOnlyAppender secondary;
    static public class LastOnlyAppender extends AppenderSkeleton {
        protected void append(LoggingEvent event) {
            this.lastEvent = event;
        }
        public boolean requiresLayout() {
            return false;
        }
        public void close() {
            this.closed = true;
        }
        public String getLastMessage() {
            if (this.lastEvent != null)
                return this.lastEvent.getMessage().toString();
            else
                return "";
        }
        private LoggingEvent lastEvent;
    };
}