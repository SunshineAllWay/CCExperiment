package org.apache.batik.svggen;
import java.awt.Graphics2D;
import java.awt.Color;
import java.net.URL;
import java.io.File;
import org.apache.batik.test.DefaultTestSuite;
import org.apache.batik.test.Test;
import org.apache.batik.test.TestReportValidator;
import org.apache.batik.test.TestReport;
public class SVGAccuracyTestValidator extends DefaultTestSuite {
    public SVGAccuracyTestValidator(){
        addTest(new NullPainter());
        addTest(new PainterWithException());
        addTest(new NullReferenceURL());
        addTest(new InexistantReferenceURL());
        addTest(new DiffWithReferenceImage());
        addTest(new SameAsReferenceImage());
    }
    static class NullPainter extends TestReportValidator {
        public TestReport runImpl() throws Exception {
            Painter painter = null;
            URL refURL = new URL("http",
                                 "dummyHost",
                                 "dummyFile.svg");
            Test t 
                = new SVGAccuracyTest(painter, refURL);
            setConfig(t,
                      false,
                      SVGAccuracyTest.ERROR_CANNOT_GENERATE_SVG);
            return super.runImpl();
        }
    }
    static class PainterWithException extends TestReportValidator 
        implements Painter {
        public void paint(Graphics2D g){
            g.setComposite(null); 
            g.fillRect(0, 0, 20, 20);
        }
        public TestReport runImpl() throws Exception {
            Painter painter = this;
            URL refURL = new URL("http",
                                 "dummyHost",
                                 "dummyFile.svg");
            Test t = new SVGAccuracyTest(painter, refURL);
            setConfig(t,
                      false,
                      SVGAccuracyTest.ERROR_CANNOT_GENERATE_SVG);
            return super.runImpl();
        }
    }
    static class ValidPainterTest extends TestReportValidator 
        implements Painter{
        public void paint(Graphics2D g){
            g.setPaint(Color.red);
            g.fillRect(0, 0, 40, 40);
        }
    }
    static class NullReferenceURL extends ValidPainterTest {
        public TestReport runImpl() throws Exception {
            Test t = new SVGAccuracyTest(this, null);
            setConfig(t,
                      false,
                      SVGAccuracyTest.ERROR_CANNOT_OPEN_REFERENCE_SVG_FILE);
            return super.runImpl();
        }
    }
    static class InexistantReferenceURL extends ValidPainterTest {
        public TestReport runImpl() throws Exception {
            Test t = new SVGAccuracyTest(this,
                                         new URL("http",
                                                 "dummyHost",
                                                 "dummyFile.svg"));
            setConfig(t,
                      false,
                      SVGAccuracyTest.ERROR_CANNOT_OPEN_REFERENCE_SVG_FILE);
            return super.runImpl();
        }
    }
    static class DiffWithReferenceImage extends ValidPainterTest {
        public TestReport runImpl() throws Exception {
            File tmpFile = File.createTempFile("EmptySVGReference",
                                               null);
            tmpFile.deleteOnExit();
            Test t = new SVGAccuracyTest(this,
                                         tmpFile.toURL());
            setConfig(t,
                      false,
                      SVGAccuracyTest.ERROR_GENERATED_SVG_INACCURATE);
            return super.runImpl();
        }
    }
    static class SameAsReferenceImage extends ValidPainterTest {
        public TestReport runImpl() throws Exception {
            File tmpFile = File.createTempFile("SVGReference",
                                               null);
            tmpFile.deleteOnExit();
            SVGAccuracyTest t = new SVGAccuracyTest(this,
                                                    tmpFile.toURL());
            t.setSaveSVG(tmpFile);
            setConfig(t,
                      false,
                      SVGAccuracyTest.ERROR_GENERATED_SVG_INACCURATE);
            super.runImpl();
            setConfig(t,
                      true,
                      null);
            return super.runImpl();
        }
    }
}
