package org.apache.tools.ant.taskdefs.optional;
import java.io.PrintWriter;
import java.util.TooManyListenersException;
import javax.xml.transform.Transformer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.apache.xalan.trace.PrintTraceListener;
import org.apache.xalan.transformer.TransformerImpl;
public class Xalan2TraceSupport implements XSLTTraceSupport {
    public void configureTrace(Transformer t,
                               XSLTProcess.TraceConfiguration conf) {
        if (t instanceof TransformerImpl && conf != null) {
            PrintWriter w = new PrintWriter(conf.getOutputStream(), false);
            PrintTraceListener tl = new PrintTraceListener(w);
            tl.m_traceElements = conf.getElements();
            tl.m_traceExtension = conf.getExtension();
            tl.m_traceGeneration = conf.getGeneration();
            tl.m_traceSelection = conf.getSelection();
            tl.m_traceTemplates = conf.getTemplates();
            try {
                ((TransformerImpl) t).getTraceManager().addTraceListener(tl);
            } catch (TooManyListenersException tml) {
                throw new BuildException(tml);
            }
        }
    }
}
