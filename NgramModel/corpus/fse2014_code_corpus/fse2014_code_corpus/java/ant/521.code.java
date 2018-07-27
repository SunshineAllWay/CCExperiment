package org.apache.tools.ant.taskdefs.optional.perforce;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
public abstract class P4HandlerAdapter  implements P4Handler {
    String p4input = "";
    private PumpStreamHandler myHandler = null;
    public void setOutput(String p4Input) {
        this.p4input = p4Input;
    }
    public abstract void process(String line);
    public void start() throws BuildException {
        if (p4input != null && p4input.length() > 0) {
            myHandler = new PumpStreamHandler(new P4OutputStream(this), new P4OutputStream(this),
                new ByteArrayInputStream(p4input.getBytes()));
        } else {
            myHandler = new PumpStreamHandler(new P4OutputStream(this), new P4OutputStream(this));
        }
        myHandler.setProcessInputStream(os);
        myHandler.setProcessErrorStream(es);
        myHandler.setProcessOutputStream(is);
        myHandler.start();
    }
    public void stop() {
        if (myHandler != null) {
            myHandler.stop();
        }
    }
    OutputStream os;    
    InputStream is;     
    InputStream es;     
    public void setProcessInputStream(OutputStream os) throws IOException {
        this.os = os;
    }
    public void setProcessErrorStream(InputStream is) throws IOException {
        this.es = is;
    }
    public void setProcessOutputStream(InputStream is) throws IOException {
        this.is = is;
    }
}
