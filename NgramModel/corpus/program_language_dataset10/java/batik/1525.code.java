package org.apache.batik.svggen;
import java.awt.*;
import java.awt.geom.*;
public class BasicShapes implements Painter {
    public void paint(Graphics2D g){
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.black);
        g.drawString("Rectangle", 10, 20);
        Rectangle rect = new Rectangle(10, 30, 50, 40);
        g.draw(rect);
        g.translate(0, 90);
        g.drawString("RoundRectangle", 10, 20);
        RoundRectangle2D roundRect = new RoundRectangle2D.Double(10, 30, 50, 40, 10, 10);
        g.draw(roundRect);
        g.translate(0, 90);
        g.drawString("Circle", 10, 20);
        Ellipse2D circle = new Ellipse2D.Float(10, 30, 50, 50);
        g.draw(circle);
        g.translate(0, 90);
        g.drawString("CubicCurve2D", 10, 20);
        CubicCurve2D curve = new CubicCurve2D.Float(10, 55, 22.5f, 00, 38.5f, 110, 60, 55);
        g.draw(curve);
        g.translate(150, -270);
        g.drawString("Polygon", 10, 20);
        Polygon polygon = new Polygon(new int[] { 30, 50, 10 },
                                      new int[] { 30, 60, 60 },
                                      3);
        g.draw(polygon);
        g.translate(0, 90);
        g.drawString("GeneralPath", 10, 20);
        GeneralPath path = new GeneralPath();
        path.moveTo(30, 30);
        path.quadTo(30, 50, 50, 60);
        path.quadTo(30, 50, 10, 60);
        path.quadTo(30, 50, 30, 30);
        path.closePath();
        g.draw(path);
        g.translate(0, 90);
        g.drawString("Area", 10, 20);
        Area area = new Area(new Rectangle(10, 30, 50, 50));
        area.subtract(new Area(new Ellipse2D.Double(12, 32, 46, 46)));
        g.fill(area);
        g.translate(0, 90);
        g.drawString("QuadCurve2D", 10, 20);
        QuadCurve2D quad = new QuadCurve2D.Float(10, 55, 35, 105, 60, 55);
        g.draw(quad);
        g.translate(-75, 70);
        g.drawString("Line2D", 10, 20);
        g.draw(new Line2D.Float(10, 30, 60, 30));
    }
}
