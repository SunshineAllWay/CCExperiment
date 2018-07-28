package org.apache.batik.svggen;
import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupTable;
public class Lookup implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        Image image = Toolkit.getDefaultToolkit().createImage("test-resources/org/apache/batik/svggen/resources/vangogh.png");
        MediaTracker tracker = new MediaTracker(new Button(""));
        tracker.addImage(image, 0);
        try{
            tracker.waitForAll();
        }catch(InterruptedException e){
            tracker.removeImage(image);
            image = null;
        }finally {
            if(image != null)
                tracker.removeImage(image);
            if(tracker.isErrorAny())
                image = null;
            if(image != null){
                if(image.getWidth(null)<0 ||
                   image.getHeight(null)<0)
                    image = null;
            }
        }
        if(image == null){
            throw new Error("Could not load image");
        }
        BufferedImage bi = new BufferedImage(image.getWidth(null),
                                             image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D ig = bi.createGraphics();
        ig.drawImage(image, 0, 0, null);
        byte[] lookup = new byte[256];
        for(int i=0; i<256; i++)
            lookup[i] = (byte)(255 - i);
        LookupTable table = new ByteLookupTable(0, lookup);
        java.awt.image.LookupOp inverter = new java.awt.image.LookupOp(table, null);
        g.setPaint(Color.black);
        g.drawString("Normal / Inverted", 10, 20);
        g.drawImage(image, 10, 30, null);
        g.drawImage(bi, inverter, 10 + bi.getWidth() + 10, 30);
    }
}
