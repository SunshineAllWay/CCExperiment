package org.apache.cassandra.utils;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Mx4jTool
{
    private static final Logger logger = LoggerFactory.getLogger(Mx4jTool.class);
    public static boolean maybeLoad()
    {
        try
        {
            logger.debug("Will try to load mx4j now, if it's in the classpath");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName processorName = new ObjectName("Server:name=XSLTProcessor");
            Class<?> httpAdaptorClass = Class.forName("mx4j.tools.adaptor.http.HttpAdaptor");
            Object httpAdaptor = httpAdaptorClass.newInstance();
            httpAdaptorClass.getMethod("setHost", String.class).invoke(httpAdaptor, getAddress());
            httpAdaptorClass.getMethod("setPort", Integer.TYPE).invoke(httpAdaptor, getPort());
            ObjectName httpName = new ObjectName("system:name=http");
            mbs.registerMBean(httpAdaptor, httpName);
            Class<?> xsltProcessorClass = Class.forName("mx4j.tools.adaptor.http.XSLTProcessor");
            Object xsltProcessor = xsltProcessorClass.newInstance();
            httpAdaptorClass.getMethod("setProcessor", Class.forName("mx4j.tools.adaptor.http.ProcessorMBean")).
                    invoke(httpAdaptor, xsltProcessor);
            mbs.registerMBean(xsltProcessor, processorName);
            httpAdaptorClass.getMethod("start").invoke(httpAdaptor);
            logger.info("mx4j successfuly loaded");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            logger.info("Will not load MX4J, mx4j-tools.jar is not in the classpath");
        }
        catch(Exception e)
        {
            logger.warn("Could not start register mbean in JMX", e);
        }
        return false;
    }
    private static String getAddress()
    {
        return System.getProperty("mx4jaddress", FBUtilities.getLocalAddress().getHostAddress());
    }
    private static int getPort()
    {
        int port = 8081;
        String sPort = System.getProperty("mx4jport");
        if (sPort != null && !sPort.equals(""))
        {
            port = Integer.parseInt(sPort);
        }
        return port;
    }
}
