package org.apache.batik.test;
import java.util.Vector;
public abstract class PerformanceTest extends AbstractTest {
    protected double referenceScore = -1;
    protected double allowedScoreDeviation = 0.1;
    protected double lastScore = -1;
    public double getLastScore() {
        return lastScore;
    }
    public double getReferenceScore() {
        return referenceScore;
    }
    public void setReferenceScore(double referenceScore) {
        this.referenceScore = referenceScore;
    }
    public double getAllowedScoreDeviation() {
        return allowedScoreDeviation;
    }
    public void setAllowedScoreDeviation(double allowedScoreDeviation) {
        this.allowedScoreDeviation = allowedScoreDeviation;
    }
    public final TestReport run() {
        return super.run();
    }
    public final boolean runImplBasic() throws Exception {
        return false;
    }
    public final TestReport runImpl() throws Exception {
        int iter = 50;
        double refUnit = 0;
        long refStart = 0;
        long refEnd = 0;
        long opEnd = 0;
        long opStart = 0;
        double opLength = 0;
        runRef();
        runOp();
        double[] scores = new double[iter];
        for (int i=0; i<iter; i++) {
            if ( i%2 == 0) {
                refStart = System.currentTimeMillis();
                runRef();
                refEnd = System.currentTimeMillis();
                runOp();
                opEnd = System.currentTimeMillis();
                refUnit = refEnd - refStart;
                opLength = opEnd - refEnd;
            } else {
                opStart = System.currentTimeMillis();
                runOp();
                opEnd = System.currentTimeMillis();
                runRef();
                refEnd = System.currentTimeMillis();
                refUnit = refEnd - opEnd;
                opLength = opEnd - opStart;
            }
            scores[i] = opLength / refUnit;
            System.err.println(".");
            System.gc();
        }
        System.err.println();
        sort(scores);
        double score = 0;
        int trim = 5;
        for (int i=trim; i<scores.length-trim; i++) {
            score += scores[i];
        }
        score /= (iter - 2*trim);
        this.lastScore = score;
        if (referenceScore == -1) {
            TestReport report = reportError("no.reference.score.set");
            report.addDescriptionEntry("computed.score", "" + score);
            return report;
        } else {
            double scoreMin = referenceScore*(1-allowedScoreDeviation);
            double scoreMax = referenceScore*(1+allowedScoreDeviation);
            if (score > scoreMax) {
                TestReport report = reportError("performance.regression");
                report.addDescriptionEntry("reference.score", "" + referenceScore);
                report.addDescriptionEntry("computed.score", "" + score);
                report.addDescriptionEntry("score.deviation", "" + 100*((score-referenceScore)/referenceScore));
                return report;
            } else if (score < scoreMin) {
                TestReport report = reportError("unexpected.performance.improvement");
                report.addDescriptionEntry("reference.score", "" + referenceScore);
                report.addDescriptionEntry("computed.score", "" + score);
                report.addDescriptionEntry("score.deviation", "" + 100*((score-referenceScore)/referenceScore));
                return report;
            } else {
                return reportSuccess();
            }
        }
    }
    protected void sort(double[] a) throws Exception {
        for (int i = a.length - 1; i>=0; i--) {
            boolean swapped = false;
            for (int j = 0; j<i; j++) {
                if (a[j] > a[j+1]) {
                    double d = a[j];
                    a[j] = a[j+1];
                    a[j+1] = d;
                    swapped = true;
                }
            }
            if (!swapped)
                return;
        }
    }
    protected void runRef() {
        Vector v = new Vector();
        for (int i=0; i<10000; i++) {
            v.addElement("" + i);
        }
        for (int i=0; i<10000; i++) {
            if (v.contains("" + i)) {
                v.remove("" + i);
            }
        }
    }
    protected abstract void runOp() throws Exception;
}
