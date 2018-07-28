package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import javax.xml.transform.Transformer;
public interface XSLTTraceSupport {
    void configureTrace(Transformer t, XSLTProcess.TraceConfiguration conf);
}
