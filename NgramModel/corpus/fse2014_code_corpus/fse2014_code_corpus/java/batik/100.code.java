package org.apache.batik.apps.svgbrowser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import org.apache.batik.util.resources.ResourceManager;
public class StatusBar extends JPanel {
    protected static final String RESOURCES =
        "org.apache.batik.apps.svgbrowser.resources.StatusBarMessages";
    protected static ResourceBundle bundle;
    protected static ResourceManager rManager;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        rManager = new ResourceManager(bundle);
    }
    protected JLabel xPosition;
    protected JLabel yPosition;
    protected JLabel zoom;
    protected JLabel message;
    protected String mainMessage;
    protected String temporaryMessage;
    protected DisplayThread displayThread;
    public StatusBar() {
        super(new BorderLayout(5, 5));
        JPanel p = new JPanel(new BorderLayout(0, 0));
        add("West", p);
        xPosition = new JLabel();
        BevelBorder bb;
        bb = new BevelBorder(BevelBorder.LOWERED,
                             getBackground().brighter().brighter(),
                             getBackground(),
                             getBackground().darker().darker(),
                             getBackground());
        xPosition.setBorder(bb);
        xPosition.setPreferredSize(new Dimension(110, 16));
        p.add("West", xPosition);
        yPosition = new JLabel();
        yPosition.setBorder(bb);
        yPosition.setPreferredSize(new Dimension(110, 16));
        p.add("Center", yPosition);
        zoom = new JLabel();
        zoom.setBorder(bb);
        zoom.setPreferredSize(new Dimension(70, 16));
        p.add("East", zoom);
        p = new JPanel(new BorderLayout(0, 0));
        message = new JLabel();
        message.setBorder(bb);
        p.add(message);
        add(p);
        setMainMessage(rManager.getString("Panel.default_message"));
    }
    public void setXPosition(float x) {
        xPosition.setText("x: " + x);
    }
    public void setWidth(float w) {
        xPosition.setText(rManager.getString("Position.width_letters") +
                          " " + w);
    }
    public void setYPosition(float y) {
        yPosition.setText("y: " + y);
    }
    public void setHeight(float h) {
        yPosition.setText(rManager.getString("Position.height_letters") +
                          " " + h);
    }
    public void setZoom(float f) {
        f = (f > 0) ? f : -f;
        if (f == 1) {
            zoom.setText("1:1");
        } else if (f >= 1) {
            String s = Float.toString(f);
            if (s.length() > 6) {
                s = s.substring(0, 6);
            }
            zoom.setText("1:" + s);
        } else {
            String s = Float.toString(1 / f);
            if (s.length() > 6) {
                s = s.substring(0, 6);
            }
            zoom.setText(s + ":1");
        }
    }
    public void setMessage(String s) {
        setPreferredSize(new Dimension(0, getPreferredSize().height));
        if (displayThread != null) {
            displayThread.finish();
        }
        temporaryMessage = s;
        Thread old = displayThread;
        displayThread = new DisplayThread(old);
        displayThread.start();
    }
    public void setMainMessage(String s) {
        mainMessage = s;
        message.setText(mainMessage = s);
        if (displayThread != null) {
            displayThread.finish();
            displayThread = null;
        }
        setPreferredSize(new Dimension(0, getPreferredSize().height));
    }
    protected class DisplayThread extends Thread {
        static final long DEFAULT_DURATION = 5000;
        long duration;
        Thread toJoin;
        public DisplayThread() {
            this(DEFAULT_DURATION, null);
        }
        public DisplayThread(long duration) {
            this(duration, null);
        }
        public DisplayThread(Thread toJoin) {
            this(DEFAULT_DURATION, toJoin);
        }
        public DisplayThread(long duration, Thread toJoin) {
            this.duration = duration;
            this.toJoin   = toJoin;
            setPriority(Thread.MIN_PRIORITY);
        }
        public synchronized void finish() {
            this.duration = 0;
            this.notifyAll();
        }
        public void run() {
            synchronized (this) {
                if (toJoin != null) {
                    while (toJoin.isAlive()) {
                        try { toJoin.join(); }
                        catch (InterruptedException ie) { }
                    }
                    toJoin = null;
                }
                message.setText(temporaryMessage);
                long lTime = System.currentTimeMillis();
                while (duration > 0) {
                    try {
                        wait(duration);
                    } catch(InterruptedException e) { }
                    long cTime = System.currentTimeMillis();
                    duration -= (cTime-lTime);
                    lTime = cTime;
                }
                message.setText(mainMessage);
            }
        }
    }
}
