package org.apache.log4j.jmx;
import org.apache.log4j.Logger;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.lang.reflect.InvocationTargetException;
import java.io.InterruptedIOException;
public class Agent {
  static Logger log = Logger.getLogger(Agent.class);
  public Agent() {
  }
  private static Object createServer() {
      Object newInstance = null;
      try {
        newInstance = Class.forName(
                "com.sun.jdmk.comm.HtmlAdapterServer").newInstance();
      } catch (ClassNotFoundException ex) {
          throw new RuntimeException(ex.toString());
      } catch (InstantiationException ex) {
          throw new RuntimeException(ex.toString());
      } catch (IllegalAccessException ex) {
          throw new RuntimeException(ex.toString());
      }
      return newInstance;
  }
  private static void startServer(final Object server) {
      try {
          server.getClass().getMethod("start", new Class[0]).
                  invoke(server, new Object[0]);
      } catch(InvocationTargetException ex) {
          Throwable cause = ex.getTargetException();
          if (cause instanceof RuntimeException) {
              throw (RuntimeException) cause;
          } else if (cause != null) {
              if (cause instanceof InterruptedException
                      || cause instanceof InterruptedIOException) {
                  Thread.currentThread().interrupt();
              }
              throw new RuntimeException(cause.toString());
          } else {
              throw new RuntimeException();
          }
      } catch(NoSuchMethodException ex) {
          throw new RuntimeException(ex.toString());
      } catch(IllegalAccessException ex) {
        throw new RuntimeException(ex.toString());
    }
  }
  public void start() {
    MBeanServer server = MBeanServerFactory.createMBeanServer();
    Object html = createServer();
    try {
      log.info("Registering HtmlAdaptorServer instance.");
      server.registerMBean(html, new ObjectName("Adaptor:name=html,port=8082"));
      log.info("Registering HierarchyDynamicMBean instance.");
      HierarchyDynamicMBean hdm = new HierarchyDynamicMBean();
      server.registerMBean(hdm, new ObjectName("log4j:hiearchy=default"));
    } catch(JMException e) {
      log.error("Problem while registering MBeans instances.", e);
      return;
    } catch(RuntimeException e) {
      log.error("Problem while registering MBeans instances.", e);
      return;
    }
    startServer(html);
  }
}
