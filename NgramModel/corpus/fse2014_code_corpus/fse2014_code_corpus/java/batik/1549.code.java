package org.apache.batik.svggen;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
public class JPainterComponent extends JComponent {
    protected Painter painter;
    public void paint(Graphics _g){
        Graphics2D g = (Graphics2D)_g;
        BufferedImage buf = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        painter.paint(buf.createGraphics());
        g.drawImage(buf, 0, 0, null);
    }
    public JPainterComponent(Painter painter){
        this.painter = painter;
    }
}
