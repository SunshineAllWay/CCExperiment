import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
public final class TransformApplet extends Applet {
    TransformerFactory tf;
    TransformDelegate transformThread;
    public class MessageFrame extends Frame {
        public Frame frame;
        public class ButtonHandler implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        }
        public MessageFrame(String title, String message) {
            super(title);
            frame = this; 
            setSize(320,200);
            Panel center = new Panel();
            center.add(new Label(message));
            Panel bottom = new Panel();
            Button okButton = new Button("   OK   ");
            okButton.addActionListener(new ButtonHandler());
            bottom.add(okButton);
            add(center, BorderLayout.CENTER);
            add(bottom,BorderLayout.SOUTH);
            setVisible(true);
        }
    }
    public class AppletErrorListener implements ErrorListener {
        public void displayMessage(TransformerException e) {
            MessageFrame z = new MessageFrame("XSL transformation alert",
                                              e.getMessageAndLocation());
        }
        public void error(TransformerException e) {
            displayMessage(e);
        }
        public void fatalError(TransformerException e) {
            displayMessage(e);
        }
        public void warning(TransformerException e) {
                    displayMessage(e);
        }
    }
    public synchronized String transform(Object arg1, Object arg2) {
        final String stylesheetURL = (String)arg1;
        final String documentURL = (String)arg2;
        transformThread.setStylesheetURL(stylesheetURL);
        transformThread.setDocumentURL(documentURL);
        transformThread.setWaiting(false);
        transformThread.wakeUp();
        try{
            wait();
        } catch (InterruptedException e){}
        return transformThread.getOutput();
    }
    public void start() {
        transform(getParameter("stylesheet-name"),
                  getParameter("input-document"));
    }
    public void destroy() {
        transformThread.destroy();
    }
    public void init() {
        tf = TransformerFactory.newInstance();
        try {
            tf.setAttribute("use-classpath", Boolean.TRUE);
        } catch (IllegalArgumentException iae) {
            System.err.println("Could not set XSLTC-specific TransformerFactory"
                               + " attributes.  Transformation failed.");
        }
        transformThread = new TransformDelegate(true);
        Thread t = new Thread(transformThread);
        t.setName("transformThread");
        t.start();
    }
    public String getOutput(){
        return transformThread.getOutput();
    }
    public synchronized void wakeUp() {
        notifyAll();
    }
    class TransformDelegate implements Runnable {
        private boolean isRunning, isWaiting;
        private String stylesheetURL, documentURL;
        private String outPut;
        public TransformDelegate(boolean arg) {
            isRunning = arg;
            isWaiting = true;
        }
        public synchronized void run() {
            while(isRunning){
                while(isWaiting){
                    try {
                        wait();
                    } catch (InterruptedException e){}
                }
                transform();
                isWaiting = true;
                TransformApplet.this.wakeUp();
            }
        }
        public void setStylesheetURL(String arg){
            stylesheetURL = arg;
        }
        public void setDocumentURL(String arg) {
            documentURL = arg;
        }
        public String getStylesheetURL(){
            return stylesheetURL;
        }
        public String getDocumentURL() {
            return documentURL;
        }
        public void setWaiting(boolean arg) {
            isWaiting = arg;
        }
        public void destroy() {
            isRunning = false;
        }
        public synchronized void wakeUp() {
            notifyAll();
        }
        public String getOutput(){
            return outPut;
        }
        public void transform(){
            String xslURL = getStylesheetURL();
            String docURL = getDocumentURL();
            StringWriter sout = new StringWriter();
            PrintWriter out = new PrintWriter(sout);
            try {
                if (xslURL == null || docURL == null) {
                    out.println("<h1>Transformation error</h1>");
                    out.println("The parameters <b><tt>stylesheetURL</tt></b> "+
                                "and <b><tt>source</tt></b> must be specified");
                } else {
                    Transformer t = tf.newTransformer(new StreamSource(xslURL));
                    t.setErrorListener(new AppletErrorListener());
                    final long start = System.currentTimeMillis();
                    t.transform(new StreamSource(docURL),
                                new StreamResult(out));
                    final long done = System.currentTimeMillis() - start;
                    out.println("<!-- transformed by XSLTC in " + done
                                + "msecs -->");
                }
                out.close();
                System.err.println("Transformation complete!");
                System.err.println(sout.toString());
                outPut = sout.toString();
                sout.close();
            } catch (RuntimeException e) {
                out.println("<h1>RTE</h1>");
                out.close();
                outPut = sout.toString();
            } catch (Exception e) {
                out.println("<h1>exception</h1>");
                out.println(e.toString());
                out.close();
                outPut = sout.toString();
            }
        }
    }
}
