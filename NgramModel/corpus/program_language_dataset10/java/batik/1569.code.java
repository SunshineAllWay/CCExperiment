package org.apache.batik.swing;
public class NullURITest extends NullSetSVGDocumentTest {
    public String getName() { return getId(); }
    public Runnable getRunnable(final JSVGCanvas canvas) {
        return new Runnable () {
                public void run() {
                    canvas.setURI(null);
                }};
    }
}
