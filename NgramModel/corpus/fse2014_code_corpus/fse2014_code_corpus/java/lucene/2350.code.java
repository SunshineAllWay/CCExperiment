package org.apache.solr.handler.admin;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.handler.RequestHandlerUtils;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
public class ThreadDumpHandler extends RequestHandlerBase
{
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws IOException 
  {    
    SimpleOrderedMap<Object> system = new SimpleOrderedMap<Object>();
    rsp.add( "system", system );
    ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
    SimpleOrderedMap<Object> nl = new SimpleOrderedMap<Object>();
    nl.add( "current",tmbean.getThreadCount() );
    nl.add( "peak", tmbean.getPeakThreadCount() );
    nl.add( "daemon", tmbean.getDaemonThreadCount() );
    system.add( "threadCount", nl );
    ThreadInfo[] tinfos;
    long[] tids = tmbean.findMonitorDeadlockedThreads();
    if (tids != null) {
      tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
      NamedList<SimpleOrderedMap<Object>> lst = new NamedList<SimpleOrderedMap<Object>>();
      for (ThreadInfo ti : tinfos) {
        lst.add( "thread", getThreadInfo( ti, tmbean ) );
      }
      system.add( "deadlocks", lst );
    }
    tids = tmbean.getAllThreadIds();
    tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
    NamedList<SimpleOrderedMap<Object>> lst = new NamedList<SimpleOrderedMap<Object>>();
    for (ThreadInfo ti : tinfos) {
      lst.add( "thread", getThreadInfo( ti, tmbean ) );
    }
    system.add( "threadDump", lst );
    rsp.setHttpCaching(false);
  }
  private static SimpleOrderedMap<Object> getThreadInfo( ThreadInfo ti, ThreadMXBean tmbean ) throws IOException 
  {
    SimpleOrderedMap<Object> info = new SimpleOrderedMap<Object>();
    long tid = ti.getThreadId();
    info.add( "id", tid );
    info.add( "name", ti.getThreadName() );
    info.add( "state", ti.getThreadState().toString() );
    if (ti.getLockName() != null) {
      info.add( "lock", ti.getLockName() );
    }
    if (ti.isSuspended()) {
      info.add( "suspended", true );
    }
    if (ti.isInNative()) {
      info.add( "native", true );
    }
    if (tmbean.isThreadCpuTimeSupported()) {
      info.add( "cpuTime", formatNanos(tmbean.getThreadCpuTime(tid)) );
      info.add( "userTime", formatNanos(tmbean.getThreadUserTime(tid)) );
    }
    if (ti.getLockOwnerName() != null) {
      SimpleOrderedMap<Object> owner = new SimpleOrderedMap<Object>();
      owner.add( "name", ti.getLockOwnerName() );
      owner.add( "id", ti.getLockOwnerId() );
    }
    int i=0;
    String[] trace = new String[ti.getStackTrace().length];
    for( StackTraceElement ste : ti.getStackTrace()) {
      trace[i++] = ste.toString();
    }
    info.add( "stackTrace", trace );
    return info;
  }
  private static String formatNanos(long ns) {
    return String.format("%.4fms", ns / (double) 1000000);
  }
  @Override
  public String getDescription() {
    return "Thread Dump";
  }
  @Override
  public String getVersion() {
      return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: ThreadDumpHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/admin/ThreadDumpHandler.java $";
  }
}
