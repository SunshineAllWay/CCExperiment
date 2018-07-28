package org.apache.batik.test;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
public class PerformanceTestValidator extends AbstractTest {
    public TestReport runImpl() throws Exception {
        SimplePerformanceTest p = new SimplePerformanceTest();
        TestReport r = p.run();
        assertTrue(!r.hasPassed());
        assertTrue(r.getErrorCode().equals("no.reference.score.set"));
        p.setReferenceScore(p.getLastScore());
        p.run();
        p.setReferenceScore(p.getLastScore());
        p.run();
        double score = p.getLastScore();        
        p.setReferenceScore(score);
        r = p.run();
        if (!r.hasPassed()) {
            TestReport result = reportError("unexpected.performance.test.failure");
            result.addDescriptionEntry("error.code", r.getErrorCode());
            result.addDescriptionEntry("expected.score", "" + score);
            result.addDescriptionEntry("actual.score", "" + p.getLastScore());
            result.addDescriptionEntry("regression.percentage", "" + 100*(score - p.getLastScore())/p.getLastScore());
            return result;
        }
        p.setReferenceScore(score*0.5);
        r = p.run();
        assertTrue(!r.hasPassed());
        if (!r.getErrorCode().equals("performance.regression")) {
            TestReport result = reportError("unexpected.performance.test.error.code");
            result.addDescriptionEntry("expected.code", "performance.regression");
            result.addDescriptionEntry("actual.code", r.getErrorCode());
            result.addDescriptionEntry("expected.score", "" + score);
            result.addDescriptionEntry("actual.score", "" + p.getLastScore());
            result.addDescriptionEntry("regression.percentage", "" + 100*(score - p.getLastScore())/p.getLastScore());
            return result;
        }
        p.setReferenceScore(score*2);
        r = p.run();
        assertTrue(!r.hasPassed());
        if (!r.getErrorCode().equals("unexpected.performance.improvement")) {
            TestReport result = reportError("unexpected.performance.test.error.code");
            result.addDescriptionEntry("expected.code", "unexpected.performance.improvement");
            result.addDescriptionEntry("actual.code", r.getErrorCode());
            result.addDescriptionEntry("expected.score", "" + score);
            result.addDescriptionEntry("actual.score", "" + p.getLastScore());
            result.addDescriptionEntry("regression.percentage", "" + 100*(score - p.getLastScore())/p.getLastScore());
            return result;
        }
        return reportSuccess();
    }
    static class SimplePerformanceTest extends PerformanceTest {
        public void runOp() {
            BufferedImage buf = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buf.createGraphics();
            AffineTransform txf = new AffineTransform();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setPaint(new Color(30, 100, 200));
            for (int j=0; j<20; j++) {
                txf.setToIdentity();
                txf.translate(-100, -100);
                txf.rotate(j*Math.PI/100);
                txf.translate(100, 100);
                g.setTransform(txf);
                g.drawRect(30, 30, 140, 140);
            }
        }
    }
}
