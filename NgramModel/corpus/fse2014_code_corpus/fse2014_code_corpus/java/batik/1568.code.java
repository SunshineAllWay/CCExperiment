package org.apache.batik.swing;
import java.awt.EventQueue;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
public class NullSetSVGDocumentTest extends JSVGMemoryLeakTest {
    public NullSetSVGDocumentTest() {
    }
    public static final String TEST_NON_NULL_URI
        = "file:samples/anne.svg";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION 
        = "JSVGCanvasHandler.entry.key.error.description";
    public static final String ERROR_IMAGE_NOT_CLEARED 
        = "NullSetSVGDocumentTest.message.error.image.not.cleared";
    public static final String ERROR_ON_SET 
        = "NullSetSVGDocumentTest.message.error.on.set";
    public String getName() { return getId(); }
    public JSVGCanvasHandler createHandler() {
        return new JSVGCanvasHandler(this, this) {
                public JSVGCanvas createCanvas() { 
                    return new JSVGCanvas() {
                            protected void installSVGDocument(SVGDocument doc){
                                super.installSVGDocument(doc);
                                if (doc != null) return;
                                handler.scriptDone();
                            }
                        };
                }
            };
    }
    public Runnable getRunnable(final JSVGCanvas canvas) {
        return new Runnable () {
                public void run() {
                    canvas.setSVGDocument(null);
                }};
            }
    public boolean canvasInit(JSVGCanvas canvas) {
        setTheCanvas(canvas);
        theFrame  = handler.getFrame();
        canvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        canvas.setURI(TEST_NON_NULL_URI);
        registerObjectDesc(canvas, "JSVGCanvas");
        registerObjectDesc(handler.getFrame(), "JFrame");
        return true; 
    }
    public void canvasRendered(JSVGCanvas canvas) {
        super.canvasRendered(canvas);
        try {
            EventQueue.invokeAndWait(getRunnable(canvas));
        } catch (Throwable t) {
            t.printStackTrace();
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace));
            DefaultTestReport report = new DefaultTestReport(this);
            report.setErrorCode(ERROR_ON_SET);
            report.setDescription(new TestReport.Entry[] { 
                new TestReport.Entry
                (fmt(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 fmt(ERROR_ON_SET, new Object[]{ trace.toString()}))
            });
            report.setPassed(false);
            failReport = report;
        }
    }
    public boolean canvasUpdated(JSVGCanvas canvas) {
        return true;
    }
    public void canvasDone(JSVGCanvas canvas) {
        synchronized (this) {
            checkObjects(new String[] { "SVGDoc", "GVT", "updateManager" });
            if (canvas.getOffScreen() == null)
                return;
            System.err.println(">>>>>>> Canvas not cleared");
            DefaultTestReport report = new DefaultTestReport(this);
            report.setErrorCode(ERROR_IMAGE_NOT_CLEARED);
            report.setDescription(new TestReport.Entry[] { 
                new TestReport.Entry
                (fmt(ENTRY_KEY_ERROR_DESCRIPTION, null),
                 fmt(ERROR_IMAGE_NOT_CLEARED, null))});
            report.setPassed(false);
            failReport = report;
            return;
        }
    }
}
