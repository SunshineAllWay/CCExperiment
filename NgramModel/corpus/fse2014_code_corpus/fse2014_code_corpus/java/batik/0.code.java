package org.apache.batik.apps.jsvg;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;
public class JSVG extends JFrame{
    static int windowCount=0;
    public JSVG(String url) {
        super(url);
        JSVGCanvas canvas = new JSVGCanvas(new SVGUserAgentGUIAdapter(this),
                                           true, true) {
                public void setMySize(Dimension d) {
                    setPreferredSize(d);
                    invalidate();
                    JSVG.this.pack();
                }
            };
        getContentPane().add(canvas, BorderLayout.CENTER);
        canvas.setURI(url);
        setVisible(true);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    windowCount--;
                    if (windowCount == 0)
                        System.exit(0);
                }
            });
        windowCount++;
    }
    public static void main(String[] args) {
        for (int i=0; i<args.length; i++) {
            new JSVG(args[i]);
        }
    }
}
