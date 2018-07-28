package org.apache.batik.ext.awt.g2d;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.Map;
public abstract class AbstractGraphics2D extends Graphics2D implements Cloneable {
    protected GraphicContext gc;
    protected boolean textAsShapes = false;
    public AbstractGraphics2D(boolean textAsShapes) {
        this.textAsShapes = textAsShapes;
    }
    public AbstractGraphics2D(AbstractGraphics2D g) {
        this.gc = (GraphicContext)g.gc.clone();
        this.gc.validateTransformStack();
        this.textAsShapes = g.textAsShapes;
    }
    public void translate(int x, int y){
        gc.translate(x, y);
    }
    public Color getColor(){
        return gc.getColor();
    }
    public void setColor(Color c){
        gc.setColor(c);
    }
    public void setPaintMode(){
        gc.setComposite(AlphaComposite.SrcOver);
    }
    public Font getFont(){
        return gc.getFont();
    }
    public void setFont(Font font){
        gc.setFont(font);
    }
    public Rectangle getClipBounds(){
        return gc.getClipBounds();
    }
    public void clipRect(int x, int y, int width, int height){
        gc.clipRect(x, y, width, height);
    }
    public void setClip(int x, int y, int width, int height){
        gc.setClip(x, y, width, height);
    }
    public Shape getClip(){
        return gc.getClip();
    }
    public void setClip(Shape clip){
        gc.setClip(clip);
    }
    public void drawLine(int x1, int y1, int x2, int y2){
        Line2D line = new Line2D.Float(x1, y1, x2, y2);
        draw(line);
    }
    public void fillRect(int x, int y, int width, int height){
        Rectangle rect = new Rectangle(x, y, width, height);
        fill(rect);
    }
    public void drawRect(int x, int y, int width, int height){
        Rectangle rect = new Rectangle(x, y, width, height);
        draw(rect);
    }
    public void clearRect(int x, int y, int width, int height){
        Paint paint = gc.getPaint();
        gc.setColor(gc.getBackground());
        fillRect(x, y, width, height);
        gc.setPaint(paint);
    }
    public void drawRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight){
        RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight);
        draw(rect);
    }
    public void fillRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight){
        RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight);
        fill(rect);
    }
    public void drawOval(int x, int y, int width, int height){
        Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
        draw(oval);
    }
    public void fillOval(int x, int y, int width, int height){
        Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
        fill(oval);
    }
    public void drawArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle){
        Arc2D arc = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
        draw(arc);
    }
    public void fillArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle){
        Arc2D arc = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
        fill(arc);
    }
    public void drawPolyline(int[] xPoints, int[] yPoints,
                             int nPoints){
        if(nPoints > 0){
            GeneralPath path = new GeneralPath();
            path.moveTo(xPoints[0], yPoints[0]);
            for(int i=1; i<nPoints; i++)
                path.lineTo(xPoints[i], yPoints[i]);
            draw(path);
        }
    }
    public void drawPolygon(int[] xPoints, int[] yPoints,
                            int nPoints){
        Polygon polygon = new Polygon(xPoints, yPoints, nPoints);
        draw(polygon);
    }
    public void fillPolygon(int[] xPoints, int[] yPoints,
                            int nPoints){
        Polygon polygon = new Polygon(xPoints, yPoints, nPoints);
        fill(polygon);
    }
    public void drawString(String str, int x, int y){
        drawString(str, (float)x, (float)y);
    }
    public void drawString(AttributedCharacterIterator iterator,
                           int x, int y){
        drawString(iterator, (float)x, (float)y);
    }
    public boolean drawImage(Image img, int x, int y,
                             Color bgcolor,
                             ImageObserver observer){
        return drawImage(img, x, y, img.getWidth(null), img.getHeight(null),
                         bgcolor, observer);
    }
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             Color bgcolor,
                             ImageObserver observer){
        Paint paint = gc.getPaint();
        gc.setPaint(bgcolor);
        fillRect(x, y, width, height);
        gc.setPaint(paint);
        drawImage(img, x, y, width, height, observer);
        return true;
    }
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer){
        BufferedImage src = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = src.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        src = src.getSubimage(sx1, sy1, sx2-sx1, sy2-sy1);
        return drawImage(src, dx1, dy1, dx2-dx1, dy2-dy1, observer);
    }
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor,
                             ImageObserver observer){
        Paint paint = gc.getPaint();
        gc.setPaint(bgcolor);
        fillRect(dx1, dy1, dx2-dx1, dy2-dy1);
        gc.setPaint(paint);
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }
    public boolean drawImage(Image img,
                             AffineTransform xform,
                             ImageObserver obs){
        boolean retVal = true;
        if(xform.getDeterminant() != 0){
            AffineTransform inverseTransform = null;
            try{
                inverseTransform = xform.createInverse();
            }   catch(NoninvertibleTransformException e){
                throw new Error( e.getMessage() );
            }
            gc.transform(xform);
            retVal = drawImage(img, 0, 0, null);
            gc.transform(inverseTransform);
        }
        else{
            AffineTransform savTransform = new AffineTransform(gc.getTransform());
            gc.transform(xform);
            retVal = drawImage(img, 0, 0, null);
            gc.setTransform(savTransform);
        }
        return retVal;
    }
    public void drawImage(BufferedImage img,
                          BufferedImageOp op,
                          int x,
                          int y){
        img = op.filter(img, null);
        drawImage(img, x, y, null);
    }
    public void drawGlyphVector(GlyphVector g, float x, float y){
        Shape glyphOutline = g.getOutline(x, y);
        fill(glyphOutline);
    }
    public boolean hit(Rectangle rect,
                       Shape s,
                       boolean onStroke){
        if (onStroke) {
            s = gc.getStroke().createStrokedShape(s);
        }
        s = gc.getTransform().createTransformedShape(s);
        return s.intersects(rect);
    }
    public void setComposite(Composite comp){
        gc.setComposite(comp);
    }
    public void setPaint(Paint paint) {
        gc.setPaint(paint);
    }
    public void setStroke(Stroke s){
        gc.setStroke(s);
    }
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue){
        gc.setRenderingHint(hintKey, hintValue);
    }
    public Object getRenderingHint(RenderingHints.Key hintKey){
        return gc.getRenderingHint(hintKey);
    }
    public void setRenderingHints(Map hints){
        gc.setRenderingHints(hints);
    }
    public void addRenderingHints(Map hints){
        gc.addRenderingHints(hints);
    }
    public RenderingHints getRenderingHints(){
        return gc.getRenderingHints();
    }
    public void translate(double tx, double ty){
        gc.translate(tx, ty);
    }
    public void rotate(double theta){
        gc.rotate(theta);
    }
    public void rotate(double theta, double x, double y){
        gc.rotate(theta, x, y);
    }
    public void scale(double sx, double sy){
        gc.scale(sx, sy);
    }
    public void shear(double shx, double shy){
        gc.shear(shx, shy);
    }
    public void transform(AffineTransform Tx){
        gc.transform(Tx);
    }
    public void setTransform(AffineTransform Tx){
        gc.setTransform(Tx);
    }
    public AffineTransform getTransform(){
        return gc.getTransform();
    }
    public Paint getPaint(){
        return gc.getPaint();
    }
    public Composite getComposite(){
        return gc.getComposite();
    }
    public void setBackground(Color color){
        gc.setBackground(color);
    }
    public Color getBackground(){
        return gc.getBackground();
    }
    public Stroke getStroke(){
        return gc.getStroke();
    }
    public void clip(Shape s){
        gc.clip(s);
    }
    public FontRenderContext getFontRenderContext(){
        return gc.getFontRenderContext();
    }
    public GraphicContext getGraphicContext() {
        return gc;
    }
}
