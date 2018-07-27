package org.apache.batik.svggen;
import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
public class Rescale implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        Image image = Toolkit.getDefaultToolkit().createImage("test-resources/org/apache/batik/svggen/resources/vangogh.jpg");
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
        java.awt.image.RescaleOp brighten = new java.awt.image.RescaleOp(1.5f, 0, null);
        java.awt.image.RescaleOp darken = new java.awt.image.RescaleOp(.6f, 0, null);
        g.setPaint(Color.black);
        g.drawString("Brighter / Normal / Darker", 10, 20);
        g.drawImage(bi, brighten, 10, 30);
        g.drawImage(image, 10 + bi.getWidth() + 10, 30, null);
        g.drawImage(bi, darken, 10 + 2*(bi.getWidth() + 10), 30);
        g.translate(0, bi.getHeight() + 30 + 20);
        g.drawString("Rescale Red / Green / Blue", 10, 20);
        java.awt.image.RescaleOp redStress = new java.awt.image.RescaleOp(new float[]{ 2.0f, 1.0f, 1.0f },
                                            new float[]{ 0, 0, 0 }, null);
        java.awt.image.RescaleOp greenStress = new java.awt.image.RescaleOp(new float[]{ 1.0f, 2.0f, 1.0f },
                                              new float[]{ 0, 0, 0 }, null);
        java.awt.image.RescaleOp blueStress = new java.awt.image.RescaleOp(new float[]{ 1.0f, 1.0f, 2.0f },
                                             new float[]{ 0, 0, 0 }, null);
        g.drawImage(bi, redStress, 10, 30);
        g.drawImage(bi, greenStress, 10 + bi.getWidth() + 10, 30);
        g.drawImage(bi, blueStress, 10 + 2*(bi.getWidth() + 10), 30);
    }
}
