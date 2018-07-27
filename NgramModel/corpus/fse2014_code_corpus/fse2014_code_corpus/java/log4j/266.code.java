package org.apache.log4j;
import junit.framework.TestCase;
import java.util.Vector;
import org.apache.log4j.spi.LoggingEvent;
public class AsyncAppenderTestCase extends TestCase {
  public AsyncAppenderTestCase(String name) {
    super(name);
  }
  public void setUp() {
  }
  public void tearDown() {
    LogManager.shutdown();
  }
  public void closeTest() throws Exception {    
    Logger root = Logger.getRootLogger();
    VectorAppender vectorAppender = new VectorAppender();
    AsyncAppender asyncAppender = new AsyncAppender();
    asyncAppender.setName("async-CloseTest");
    asyncAppender.addAppender(vectorAppender);
    root.addAppender(asyncAppender); 
    root.debug("m1");
    asyncAppender.close();
    root.debug("m2");
    Vector v = vectorAppender.getVector();
    assertEquals(v.size(), 1);
  }
  public void test2() {
    Logger root = Logger.getRootLogger();
    VectorAppender vectorAppender = new VectorAppender();
    AsyncAppender asyncAppender = new AsyncAppender();
    asyncAppender.setName("async-test2");
    asyncAppender.addAppender(vectorAppender);
    root.addAppender(asyncAppender); 
    root.debug("m1");
    asyncAppender.close();
    root.debug("m2");
    Vector v = vectorAppender.getVector();
    assertEquals(v.size(), 1);
    assertTrue(vectorAppender.isClosed());
  }
  public void test3() {
    int LEN = 200;
    Logger root = Logger.getRootLogger();
    VectorAppender vectorAppender = new VectorAppender();
    AsyncAppender asyncAppender = new AsyncAppender();
    asyncAppender.setName("async-test3");
    asyncAppender.addAppender(vectorAppender);
    root.addAppender(asyncAppender); 
    for(int i = 0; i < LEN; i++) {
      root.debug("message"+i);
    }
    System.out.println("Done loop.");
    System.out.flush();
    asyncAppender.close();
    root.debug("m2");
    Vector v = vectorAppender.getVector();
    assertEquals(v.size(), LEN);
    assertTrue(vectorAppender.isClosed());
  }
    private static class NullPointerAppender extends AppenderSkeleton {
          public NullPointerAppender() {
          }
          public void append(org.apache.log4j.spi.LoggingEvent event) {
              throw new NullPointerException();
          }
          public void close() {
          }
          public boolean requiresLayout() {
            return false;
          }
    }
    public void testBadAppender() throws Exception {
        Appender nullPointerAppender = new NullPointerAppender();
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.addAppender(nullPointerAppender);
        asyncAppender.setBufferSize(5);
        asyncAppender.activateOptions();
        Logger root = Logger.getRootLogger();
        root.addAppender(nullPointerAppender);
        try {
           root.info("Message");
           Thread.sleep(10);
           root.info("Message");
           fail("Should have thrown exception");
        } catch(NullPointerException ex) {
        }
    }
    public void testLocationInfoTrue() {
        BlockableVectorAppender blockableAppender = new BlockableVectorAppender();
        AsyncAppender async = new AsyncAppender();
        async.addAppender(blockableAppender);
        async.setBufferSize(5);
        async.setLocationInfo(true);
        async.setBlocking(false);
        async.activateOptions();
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(async);
        Greeter greeter = new Greeter(rootLogger, 100);
        synchronized(blockableAppender.getMonitor()) {
            greeter.run();
            rootLogger.error("That's all folks.");
        }
        async.close();
        Vector events = blockableAppender.getVector();
        LoggingEvent initialEvent = (LoggingEvent) events.get(0);
        LoggingEvent discardEvent = (LoggingEvent) events.get(events.size() - 1);
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%C:%L %m%n");
        layout.activateOptions();
        String initialStr = layout.format(initialEvent);
        assertEquals(AsyncAppenderTestCase.class.getName(),
                initialStr.substring(0, AsyncAppenderTestCase.class.getName().length()));
        String discardStr = layout.format(discardEvent);
        assertEquals("?:? ", discardStr.substring(0, 4));
    }
    public void testLocationInfoFalse() {
        BlockableVectorAppender blockableAppender = new BlockableVectorAppender();
        AsyncAppender async = new AsyncAppender();
        async.addAppender(blockableAppender);
        async.setBufferSize(5);
        async.setLocationInfo(false);
        async.setBlocking(false);
        async.activateOptions();
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(async);
        Greeter greeter = new Greeter(rootLogger, 100);
        synchronized(blockableAppender.getMonitor()) {
            greeter.run();
            rootLogger.error("That's all folks.");
        }
        async.close();
        Vector events = blockableAppender.getVector();
        LoggingEvent initialEvent = (LoggingEvent) events.get(0);
        LoggingEvent discardEvent = (LoggingEvent) events.get(events.size() - 1);
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%C:%L %m%n");
        layout.activateOptions();
        String initialStr = layout.format(initialEvent);
        assertEquals("?:? ", initialStr.substring(0, 4));
        String discardStr = layout.format(discardEvent);
        assertEquals("?:? ", discardStr.substring(0, 4));
    }
    private static final class Greeter implements Runnable {
      private final Logger logger;
      private final int repetitions;
      public Greeter(final Logger logger, final int repetitions) {
        if (logger == null) {
          throw new IllegalArgumentException("logger");
        }
        this.logger = logger;
        this.repetitions = repetitions;
      }
      public void run() {
        try {
          for (int i = 0; i < repetitions; i++) {
            logger.info("Hello, World");
            Thread.sleep(1);
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    }
    private static final class BlockableVectorAppender extends VectorAppender {
      private final Object monitor = new Object();
      public BlockableVectorAppender() {
        super();
      }
      public void append(final LoggingEvent event) {
        synchronized (monitor) {
          super.append(event);
            if (event.getLevel() == Level.FATAL) {
                Logger logger = Logger.getLogger(event.getLoggerName());
                logger.error(event.getMessage().toString());
                logger.warn(event.getMessage().toString());
                logger.info(event.getMessage().toString());
                logger.debug(event.getMessage().toString());
            }
        }
      }
      public Object getMonitor() {
        return monitor;
      }
    }
    public void testMutableMessage() {
        BlockableVectorAppender blockableAppender = new BlockableVectorAppender();
        AsyncAppender async = new AsyncAppender();
        async.addAppender(blockableAppender);
        async.setBufferSize(5);
        async.setLocationInfo(false);
        async.activateOptions();
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(async);
        StringBuffer buf = new StringBuffer("Hello");
        synchronized(blockableAppender.getMonitor()) {
            rootLogger.info(buf);
            buf.append(", World.");
        }
        async.close();
        Vector events = blockableAppender.getVector();
        LoggingEvent event = (LoggingEvent) events.get(0);
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%m");
        layout.activateOptions();
        String msg = layout.format(event);
        assertEquals("Hello", msg);
    }
}
