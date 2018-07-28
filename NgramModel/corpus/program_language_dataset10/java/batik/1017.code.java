package org.apache.batik.gvt.font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import java.util.List;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.text.TextPaintInfo;
public class Glyph {
    private String unicode;
    private Vector names;
    private String orientation;
    private String arabicForm;
    private String lang;
    private Point2D horizOrigin;
    private Point2D vertOrigin;
    private float horizAdvX;
    private float vertAdvY;
    private int glyphCode;
    private AffineTransform transform;
    private Point2D.Float position;
    private GVTGlyphMetrics metrics;
    private Shape outline; 
    private Rectangle2D bounds; 
    private TextPaintInfo tpi;
    private TextPaintInfo cacheTPI;
    private Shape dShape;
    private GraphicsNode glyphChildrenNode;
    public Glyph(String unicode, List names,
                 String orientation, String arabicForm, String lang,
                 Point2D horizOrigin, Point2D vertOrigin, float horizAdvX,
                 float vertAdvY, int glyphCode,
                 TextPaintInfo tpi,
                 Shape dShape, GraphicsNode glyphChildrenNode) {
        if (unicode == null) {
            throw new IllegalArgumentException();
        }
        if (horizOrigin == null) {
            throw new IllegalArgumentException();
        }
        if (vertOrigin == null) {
            throw new IllegalArgumentException();
        }
        this.unicode = unicode;
        this.names = new Vector( names );
        this.orientation = orientation;
        this.arabicForm = arabicForm;
        this.lang = lang;
        this.horizOrigin = horizOrigin;
        this.vertOrigin = vertOrigin;
        this.horizAdvX = horizAdvX;
        this.vertAdvY = vertAdvY;
        this.glyphCode = glyphCode;
        this.position = new Point2D.Float(0,0);
        this.outline = null;
        this.bounds = null;
        this.tpi = tpi;
        this.dShape = dShape;
        this.glyphChildrenNode = glyphChildrenNode;
    }
    public String getUnicode() {
        return unicode;
    }
    public Vector getNames() {
        return names;
    }
    public String getOrientation() {
        return orientation;
    }
    public String getArabicForm() {
        return arabicForm;
    }
    public String getLang() {
        return lang;
    }
    public Point2D getHorizOrigin() {
        return horizOrigin;
    }
    public Point2D getVertOrigin() {
        return vertOrigin;
    }
    public float getHorizAdvX() {
        return horizAdvX;
    }
    public float getVertAdvY() {
        return vertAdvY;
    }
    public int getGlyphCode() {
        return glyphCode;
    }
    public AffineTransform getTransform() {
        return transform;
    }
    public void setTransform(AffineTransform transform) {
        this.transform = transform;
        outline = null;
        bounds = null;
    }
    public Point2D getPosition() {
        return position;
    }
    public void setPosition(Point2D position) {
        this.position.x = (float)position.getX();
        this.position.y = (float)position.getY();
        outline = null;
        bounds = null;
    }
    public GVTGlyphMetrics getGlyphMetrics() {
        if (metrics == null) {
            Rectangle2D gb = getGeometryBounds();
            metrics = new GVTGlyphMetrics
                (getHorizAdvX(), getVertAdvY(),
                 new Rectangle2D.Double(gb.getX()-position.getX(),
                                        gb.getY()-position.getY(),
                                        gb.getWidth(),gb.getHeight()),
                 GlyphMetrics.COMPONENT);
        }
        return metrics;
    }
    public GVTGlyphMetrics getGlyphMetrics(float hkern, float vkern) {
        return new GVTGlyphMetrics(getHorizAdvX() - hkern,
                                   getVertAdvY() - vkern,
                                   getGeometryBounds(),
                                   GlyphMetrics.COMPONENT);
    }
    public Rectangle2D getGeometryBounds() {
        return getOutline().getBounds2D();
    }
    public Rectangle2D getBounds2D() {
        if ((bounds != null) &&
            TextPaintInfo.equivilent(tpi, cacheTPI))
            return bounds;
        AffineTransform tr =
            AffineTransform.getTranslateInstance(position.getX(),
                                                 position.getY());
        if (transform != null) {
            tr.concatenate(transform);
        }
        Rectangle2D bounds = null;
        if ((dShape != null) && (tpi != null)) {
            if (tpi.fillPaint != null)
                bounds = tr.createTransformedShape(dShape).getBounds2D();
            if ((tpi.strokeStroke != null) && (tpi.strokePaint != null)) {
                Shape s = tpi.strokeStroke.createStrokedShape(dShape);
                Rectangle2D r = tr.createTransformedShape(s).getBounds2D();
                if (bounds == null) bounds = r;
                else                bounds.add( r );
            }
        }
        if (glyphChildrenNode != null) {
            Rectangle2D r = glyphChildrenNode.getTransformedBounds(tr);
            if (bounds == null) bounds = r;
            else                bounds.add( r );
        }
        if (bounds == null)
            bounds = new Rectangle2D.Double
                (position.getX(), position.getY(), 0, 0);
        cacheTPI = new TextPaintInfo(tpi);
        return bounds;
    }
    public Shape getOutline() {
        if (outline == null) {
            AffineTransform tr =
                AffineTransform.getTranslateInstance(position.getX(),
                                                     position.getY());
            if (transform != null) {
                tr.concatenate(transform);
            }
            Shape glyphChildrenOutline = null;
            if (glyphChildrenNode != null) {
                glyphChildrenOutline = glyphChildrenNode.getOutline();
            }
            GeneralPath glyphOutline = null;
            if (dShape != null && glyphChildrenOutline != null) {
                glyphOutline = new GeneralPath(dShape);
                glyphOutline.append(glyphChildrenOutline, false);
            } else if (dShape != null && glyphChildrenOutline == null) {
                glyphOutline = new GeneralPath(dShape);
            } else if (dShape == null && glyphChildrenOutline != null) {
                glyphOutline = new GeneralPath(glyphChildrenOutline);
            } else {
                glyphOutline = new GeneralPath();
            }
            outline = tr.createTransformedShape(glyphOutline);
        }
        return outline;
    }
    public void draw(Graphics2D graphics2D) {
        AffineTransform tr =
            AffineTransform.getTranslateInstance(position.getX(),
                                                 position.getY());
        if (transform != null) {
            tr.concatenate(transform);
        }
        if ((dShape != null) && (tpi != null)) {
            Shape tShape = tr.createTransformedShape(dShape);
            if (tpi.fillPaint != null) {
                graphics2D.setPaint(tpi.fillPaint);
                graphics2D.fill(tShape);
            }
            if (tpi.strokeStroke != null && tpi.strokePaint != null) {
                graphics2D.setStroke(tpi.strokeStroke);
                graphics2D.setPaint(tpi.strokePaint);
                graphics2D.draw(tShape);
            }
        }
        if (glyphChildrenNode != null) {
            glyphChildrenNode.setTransform(tr);
            glyphChildrenNode.paint(graphics2D);
        }
    }
}
