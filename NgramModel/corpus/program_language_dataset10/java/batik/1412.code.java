package org.apache.batik.util.gui;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;
public class MemoryMonitor extends JFrame implements ActionMap {
    protected static final String RESOURCE =
        "org.apache.batik.util.gui.resources.MemoryMonitorMessages";
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCE, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected Map listeners = new HashMap();
    protected Panel panel;
    public MemoryMonitor() {
        this(1000);
    }
    public MemoryMonitor(long time) {
        super(resources.getString("Frame.title"));
        listeners.put("CollectButtonAction", new CollectButtonAction());
        listeners.put("CloseButtonAction", new CloseButtonAction());
        panel = new Panel(time);
        getContentPane().add(panel);
        panel.setBorder(BorderFactory.createTitledBorder
                        (BorderFactory.createEtchedBorder(),
                         resources.getString("Frame.border_title")));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonFactory bf = new ButtonFactory(bundle, this);
        p.add(bf.createJButton("CollectButton"));
        p.add(bf.createJButton("CloseButton"));
        getContentPane().add( p, BorderLayout.SOUTH );
        pack();
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                RepaintThread t = panel.getRepaintThread();
                if (!t.isAlive()) {
                    t.start();
                } else {
                    t.safeResume();
                }
            }
            public void windowClosing(WindowEvent ev) {
                panel.getRepaintThread().safeSuspend();
            }
            public void windowDeiconified(WindowEvent e) {
                panel.getRepaintThread().safeResume();
            }
            public void windowIconified(WindowEvent e) {
                panel.getRepaintThread().safeSuspend();
            }
        });
    }
    public Action getAction(String key) throws MissingListenerException {
        return (Action)listeners.get(key);
    }
    protected class CollectButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            System.gc();
        }
    }
    protected class CloseButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            panel.getRepaintThread().safeSuspend();
            dispose();
        }
    }
    public static class Panel extends JPanel {
        protected RepaintThread repaintThread;
        public Panel() {
            this(1000);
        }
        public Panel(long time) {
            super(new GridBagLayout());
            ExtendedGridBagConstraints constraints
                = new ExtendedGridBagConstraints();
            constraints.insets = new Insets(5, 5, 5, 5);
            List l = new ArrayList();
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createLoweredBevelBorder());
            JComponent c = new Usage();
            p.add(c);
            constraints.weightx = 0.3;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.setGridBounds(0, 0, 1, 1);
            add(p, constraints);
            l.add(c);
            p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createLoweredBevelBorder());
            c = new MemoryMonitor.History();
            p.add(c);
            constraints.weightx = 0.7;
            constraints.setGridBounds(1, 0, 1, 1);
            add(p, constraints);
            l.add(c);
            repaintThread = new RepaintThread(time, l);
        }
        public RepaintThread getRepaintThread() {
            return repaintThread;
        }
    }
    public static class Usage extends JPanel implements MemoryChangeListener {
        public static final int PREFERRED_WIDTH = 90;
        public static final int PREFERRED_HEIGHT = 100;
        protected static final String UNITS
            = resources.getString("Usage.units");
        protected static final String TOTAL
            = resources.getString("Usage.total");
        protected static final String USED
            = resources.getString("Usage.used");
        protected static final boolean POSTFIX
            = resources.getBoolean("Usage.postfix");
        protected static final int FONT_SIZE = 9;
        protected static final int BLOCK_MARGIN = 10;
        protected static final int BLOCKS = 15;
        protected static final double BLOCK_WIDTH =
            PREFERRED_WIDTH-BLOCK_MARGIN*2;
        protected static final double BLOCK_HEIGHT =
            ((double)PREFERRED_HEIGHT-(3*FONT_SIZE)-BLOCKS) / BLOCKS;
        protected static final int[] BLOCK_TYPE =
            { 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2 };
        protected Color[] usedColors = {
            Color.red,
            new Color(255, 165, 0),
            Color.green
        };
        protected Color[] freeColors = {
            new Color(130, 0, 0),
            new Color(130, 90, 0),
            new Color(0, 130, 0)
        };
        protected Font font = new Font("SansSerif", Font.BOLD, FONT_SIZE);
        protected Color textColor = Color.green;
        protected long totalMemory;
        protected long freeMemory;
        public Usage() {
            this.setBackground(Color.black);
            setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        }
        public void memoryStateChanged(long total, long free) {
            totalMemory = total;
            freeMemory  = free;
        }
        public void setTextColor(Color c) {
            textColor = c;
        }
        public void setLowUsedMemoryColor(Color c) {
            usedColors[2] = c;
        }
        public void setMediumUsedMemoryColor(Color c) {
            usedColors[1] = c;
        }
        public void setHighUsedMemoryColor(Color c) {
            usedColors[0] = c;
        }
        public void setLowFreeMemoryColor(Color c) {
            freeColors[2] = c;
        }
        public void setMediumFreeMemoryColor(Color c) {
            freeColors[1] = c;
        }
        public void setHighFreeMemoryColor(Color c) {
            freeColors[0] = c;
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            Dimension dim = getSize();
            double sx = ((double)dim.width) / PREFERRED_WIDTH;
            double sy = ((double)dim.height) / PREFERRED_HEIGHT;
            g2d.transform(AffineTransform.getScaleInstance(sx, sy));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            int nfree = (int)Math.round(((double)BLOCKS)
                                        * freeMemory / totalMemory);
            for (int i = 0; i < nfree; i++) {
                Rectangle2D rect = new Rectangle2D.Double(10,
                                                   i*BLOCK_HEIGHT+i+FONT_SIZE+5,
                                                          BLOCK_WIDTH,
                                                          BLOCK_HEIGHT);
                g2d.setPaint(freeColors[BLOCK_TYPE[i]]);
                g2d.fill(rect);
            }
            for (int i = nfree; i < 15; i++) {
                Rectangle2D rect = new Rectangle2D.Double(10,
                                                   i*BLOCK_HEIGHT+i+FONT_SIZE+5,
                                                          BLOCK_WIDTH,
                                                          BLOCK_HEIGHT);
                g2d.setPaint(usedColors[BLOCK_TYPE[i]]);
                g2d.fill(rect);
            }
            g2d.setPaint(textColor);
            g2d.setFont(font);
            long total = totalMemory / 1024;
            long used  = (totalMemory - freeMemory) / 1024;
            String totalText;
            String usedText;
            if (POSTFIX) {
                totalText = total + UNITS + " " + TOTAL;
                usedText  = used + UNITS + " " + USED;
            } else {
                totalText = TOTAL + " " + total + UNITS;
                usedText  = USED + " " + used + UNITS;
            }
            g2d.drawString(totalText, 10, 10);
            g2d.drawString(usedText, 10, PREFERRED_HEIGHT-3);
        }
    }
    public static class History extends JPanel implements MemoryChangeListener {
        public static final int PREFERRED_WIDTH = 200;
        public static final int PREFERRED_HEIGHT = 100;
        protected static final Stroke GRID_LINES_STROKE = new BasicStroke(1);
        protected static final Stroke CURVE_STROKE =
            new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        protected static final Stroke BORDER_STROKE = new BasicStroke(2);
        protected Color gridLinesColor = new Color(0, 130, 0);
        protected Color curveColor = Color.yellow;
        protected Color borderColor = Color.green;
        protected List data = new LinkedList();
        protected int xShift = 0;
        protected long totalMemory;
        protected long freeMemory;
        protected GeneralPath path = new GeneralPath();
        public History() {
            this.setBackground(Color.black);
            setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        }
        public void memoryStateChanged(long total, long free) {
            totalMemory = total;
            freeMemory  = free;
            data.add(new Long(totalMemory - freeMemory));
            if (data.size() > 190) {
                data.remove(0);
                xShift = (xShift + 1) % 10;
            }
            Iterator it = data.iterator();
            GeneralPath p = new GeneralPath();
            long l = ((Long)it.next()).longValue();
            p.moveTo(5, ((float)(totalMemory - l) / totalMemory) * 80 + 10);
            int i = 6;
            while (it.hasNext()) {
                l = ((Long)it.next()).longValue();
                p.lineTo(i, ((float)(totalMemory - l) / totalMemory) * 80 + 10);
                i++;
            }
            path = p;
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension dim = getSize();
            double sx = ((double)dim.width) / PREFERRED_WIDTH;
            double sy = ((double)dim.height) / PREFERRED_HEIGHT;
            g2d.transform(AffineTransform.getScaleInstance(sx, sy));
            g2d.setPaint(gridLinesColor);
            g2d.setStroke(GRID_LINES_STROKE);
            for (int i = 1; i < 20; i++) {
                int lx = i * 10 + 5 - xShift;
                g2d.draw(new Line2D.Double(lx, 5, lx, PREFERRED_HEIGHT - 5));
            }
            for (int i = 1; i < 9; i++) {
                int ly = i * 10 + 5;
                g2d.draw(new Line2D.Double(5, ly, PREFERRED_WIDTH - 5, ly));
            }
            g2d.setPaint(curveColor);
            g2d.setStroke(CURVE_STROKE);
            g2d.draw(path);
            g2d.setStroke(BORDER_STROKE);
            g2d.setPaint(borderColor);
            g2d.draw(new Rectangle2D.Double(5,
                                            5,
                                            PREFERRED_WIDTH - 10,
                                            PREFERRED_HEIGHT - 10));
        }
    }
    public interface MemoryChangeListener {
        void memoryStateChanged(long total, long free);
    }
    public static class RepaintThread extends Thread {
        protected long timeout;
        protected List components;
        protected Runtime runtime = Runtime.getRuntime();
        protected boolean suspended;
        protected UpdateRunnable updateRunnable;
        public RepaintThread(long timeout, List components) {
            this.timeout = timeout;
            this.components = components;
            this.updateRunnable = createUpdateRunnable();
            setPriority(Thread.MIN_PRIORITY);
        }
        public void run() {
            for (;;) {
                try {
                    synchronized (updateRunnable) { 
                        if (!updateRunnable.inEventQueue)
                            EventQueue.invokeLater(updateRunnable);
                        updateRunnable.inEventQueue = true; 
                    }
                    sleep(timeout);
                    synchronized(this) {
                        while (suspended) {
                            wait();
                        }
                    }
                } catch (InterruptedException e) {}
            }
        }
        protected UpdateRunnable createUpdateRunnable() {
            return new UpdateRunnable();
        }
        protected class UpdateRunnable implements Runnable {
            public boolean inEventQueue = false;
            public void run() {
                long free  = runtime.freeMemory();
                long total = runtime.totalMemory();
                Iterator it = components.iterator();
                while (it.hasNext()) {
                    Component c = (Component)it.next();
                    ((MemoryChangeListener)c).memoryStateChanged(total, free);
                    c.repaint();
                }
                synchronized (this) { inEventQueue = false; }
            }
        }
        public synchronized void safeSuspend() {
            if (!suspended) {
                suspended = true;
            }
        }
        public synchronized void safeResume() {
            if (suspended) {
                suspended = false;
                notify();
            }
        }
    }
}
