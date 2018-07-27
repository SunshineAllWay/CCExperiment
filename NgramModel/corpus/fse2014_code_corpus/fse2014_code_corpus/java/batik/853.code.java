package org.apache.batik.ext.awt.image.renderable;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.TileRed;
public class TileRable8Bit
    extends    AbstractColorInterpolationRable
    implements TileRable{
    private Rectangle2D tileRegion;
    private Rectangle2D tiledRegion;
    private boolean overflow;
    public Rectangle2D getTileRegion(){
        return tileRegion;
    }
    public void setTileRegion(Rectangle2D tileRegion){
        if(tileRegion == null){
            throw new IllegalArgumentException();
        }
        touch();
        this.tileRegion = tileRegion;
    }
    public Rectangle2D getTiledRegion(){
        return tiledRegion;
    }
    public void setTiledRegion(Rectangle2D tiledRegion){
        if(tiledRegion == null){
            throw new IllegalArgumentException();
        }
        touch();
        this.tiledRegion = tiledRegion;
    }
    public boolean isOverflow(){
        return overflow;
    }
    public void setOverflow(boolean overflow){
        touch();
        this.overflow = overflow;
    }
    public TileRable8Bit(Filter source,
                         Rectangle2D tiledRegion,
                         Rectangle2D tileRegion,
                         boolean overflow){
        super(source);
        setTileRegion(tileRegion);
        setTiledRegion(tiledRegion);
        setOverflow(overflow);
    }
    public void setSource(Filter src){
        init(src);
    }
    public Filter getSource(){
        return (Filter)srcs.get(0);
    }
    public Rectangle2D getBounds2D(){
        return (Rectangle2D)tiledRegion.clone();
    }
    public RenderedImage createRendering(RenderContext rc){
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        AffineTransform at = rc.getTransform();
        double sx = at.getScaleX();
        double sy = at.getScaleY();
        double shx = at.getShearX();
        double shy = at.getShearY();
        double tx = at.getTranslateX();
        double ty = at.getTranslateY();
        double scaleX = Math.sqrt(sx*sx + shy*shy);
        double scaleY = Math.sqrt(sy*sy + shx*shx);
        Rectangle2D tiledRect = getBounds2D();
        Rectangle2D aoiRect;
        Shape       aoiShape  = rc.getAreaOfInterest();
        if (aoiShape == null)
            aoiRect = tiledRect;
        else {
            aoiRect = aoiShape.getBounds2D();
            if ( ! tiledRect.intersects(aoiRect) )
                return null;
            Rectangle2D.intersect(tiledRect, aoiRect, tiledRect);
        }
        Rectangle2D tileRect = tileRegion;
        int dw = (int)(Math.ceil(tileRect.getWidth() *scaleX));
        int dh = (int)(Math.ceil(tileRect.getHeight()*scaleY));
        double tileScaleX = dw/tileRect.getWidth();
        double tileScaleY = dh/tileRect.getHeight();
        int dx = (int)Math.floor(tileRect.getX()*tileScaleX);
        int dy = (int)Math.floor(tileRect.getY()*tileScaleY);
        double ttx = dx - (tileRect.getX()*tileScaleX);
        double tty = dy - (tileRect.getY()*tileScaleY);
        AffineTransform tileAt;
        tileAt = AffineTransform.getTranslateInstance(ttx, tty);
        tileAt.scale(tileScaleX, tileScaleY);
        Filter        source  = getSource();
        Rectangle2D srcRect;
        if (overflow)
            srcRect = source.getBounds2D();
        else
            srcRect = tileRect;
        RenderContext tileRc  = new RenderContext(tileAt, srcRect, rh);
        RenderedImage tileRed = source.createRendering(tileRc);
        if(tileRed == null)
            return null;
        Rectangle tiledArea = tileAt.createTransformedShape
            (aoiRect).getBounds();
        if ((tiledArea.width  == Integer.MAX_VALUE)||
            (tiledArea.height == Integer.MAX_VALUE)) {
            tiledArea = new Rectangle(Integer.MIN_VALUE/4,
                                      Integer.MIN_VALUE/4,
                                      Integer.MAX_VALUE/2,
                                      Integer.MAX_VALUE/2);
        }
        tileRed = convertSourceCS(tileRed);
        TileRed tiledRed = new TileRed(tileRed, tiledArea, dw, dh);
        AffineTransform shearAt =
            new AffineTransform(sx/scaleX, shy/scaleX,
                                shx/scaleY, sy/scaleY,
                                tx, ty);
        shearAt.scale(scaleX/tileScaleX, scaleY/tileScaleY);
        shearAt.translate(-ttx, -tty);
        CachableRed cr = tiledRed;
        if(!shearAt.isIdentity())
            cr = new AffineRed(tiledRed, shearAt, rh);
        return cr;
    }
    public Rectangle2D getActualTileBounds(Rectangle2D tiledRect){
        Rectangle2D tileRect = (Rectangle2D)tileRegion.clone();
        if ((tileRect.getWidth()   <= 0)
            || (tileRect.getHeight()  <= 0)
            || (tiledRect.getWidth()  <= 0)
            || (tiledRect.getHeight() <= 0))
            return null;
        double tileWidth = tileRect.getWidth();
        double tileHeight = tileRect.getHeight();
        double tiledWidth = tiledRect.getWidth();
        double tiledHeight = tiledRect.getHeight();
        double w = Math.min(tileWidth, tiledWidth);
        double h = Math.min(tileHeight, tiledHeight);
        Rectangle2D realTileRect
            = new Rectangle2D.Double(tileRect.getX(),
                                     tileRect.getY(),
                                     w, h);
        return realTileRect;
    }
    public RenderedImage createTile(RenderContext rc){
        AffineTransform usr2dev = rc.getTransform();
        RenderingHints rcHints = rc.getRenderingHints();
        RenderingHints hints = new RenderingHints(null);
        if(rcHints != null){
            hints.add(rcHints);
        }
        Rectangle2D tiledRect = getBounds2D();
        Shape       aoiShape  = rc.getAreaOfInterest();
        Rectangle2D aoiRect   = aoiShape.getBounds2D();
        if ( ! tiledRect.intersects(aoiRect) )
            return null;
        Rectangle2D.intersect(tiledRect, aoiRect, tiledRect);
        Rectangle2D tileRect = (Rectangle2D)tileRegion.clone();
        if ((tileRect.getWidth()   <= 0)
            || (tileRect.getHeight()  <= 0)
            || (tiledRect.getWidth()  <= 0)
            || (tiledRect.getHeight() <= 0))
            return null;
        double tileX = tileRect.getX();
        double tileY = tileRect.getY();
        double tileWidth = tileRect.getWidth();
        double tileHeight = tileRect.getHeight();
        double tiledX = tiledRect.getX();
        double tiledY = tiledRect.getY();
        double tiledWidth = tiledRect.getWidth();
        double tiledHeight = tiledRect.getHeight();
        double w = Math.min(tileWidth, tiledWidth);
        double h = Math.min(tileHeight, tiledHeight);
        double dx = (tiledX - tileX)%tileWidth;
        double dy = (tiledY - tileY)%tileHeight;
        if(dx > 0){
            dx = tileWidth - dx;
        }
        else{
            dx *= -1;
        }
        if(dy > 0){
            dy = tileHeight - dy;
        }
        else{
            dy *= -1;
        }
        double scaleX = usr2dev.getScaleX();
        double scaleY = usr2dev.getScaleY();
        double tdx = Math.floor(scaleX*dx);
        double tdy = Math.floor(scaleY*dy);
        dx = tdx/scaleX;
        dy = tdy/scaleY;
        Rectangle2D.Double A = new Rectangle2D.Double
            (tileX + tileWidth - dx, tileY + tileHeight - dy, dx, dy);
        Rectangle2D.Double B = new Rectangle2D.Double
            (tileX, tileY + tileHeight - dy, w - dx, dy);
        Rectangle2D.Double C = new Rectangle2D.Double
            (tileX + tileWidth - dx, tileY, dx, h - dy);
        Rectangle2D.Double D = new Rectangle2D.Double
            (tileX, tileY, w - dx, h - dy);
        Rectangle2D realTileRect
            = new Rectangle2D.Double(tiledRect.getX(),
                                     tiledRect.getY(),
                                     w, h);
        RenderedImage ARed = null, BRed = null, CRed = null, DRed = null;
        Filter source = getSource();
        if (A.getWidth() > 0 && A.getHeight() > 0){
            Rectangle devA = usr2dev.createTransformedShape(A).getBounds();
            if(devA.width > 0 && devA.height > 0){
                AffineTransform ATxf = new AffineTransform(usr2dev);
                ATxf.translate(-A.x + tiledX,
                               -A.y + tiledY);
                Shape aoi = A;
                if(overflow){
                    aoi = new Rectangle2D.Double(A.x,
                                                 A.y,
                                                 tiledWidth,
                                                 tiledHeight);
                }
                hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST,
                          aoi);
                RenderContext arc
                    = new RenderContext(ATxf, aoi, hints);
                ARed = source.createRendering(arc);
            }
        }
        if(B.getWidth() > 0 && B.getHeight() > 0){
            Rectangle devB = usr2dev.createTransformedShape(B).getBounds();
            if(devB.width > 0 && devB.height > 0){
                AffineTransform BTxf = new AffineTransform(usr2dev);
                BTxf.translate(-B.x + (tiledX + dx),
                               -B.y + tiledY);
                Shape aoi = B;
                if(overflow){
                    aoi = new Rectangle2D.Double(B.x - tiledWidth + w - dx,
                                                 B.y,
                                                 tiledWidth,
                                                 tiledHeight);
                }
                hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST,
                          aoi);
                RenderContext brc
                    = new RenderContext(BTxf, aoi, hints);
                BRed = source.createRendering(brc);
            }
        }
        if(C.getWidth() > 0 && C.getHeight() > 0){
            Rectangle devC = usr2dev.createTransformedShape(C).getBounds();
            if(devC.width > 0 && devC.height > 0){
                AffineTransform CTxf = new AffineTransform(usr2dev);
                CTxf.translate(-C.x + tiledX,
                               -C.y + (tiledY + dy));
                Shape aoi = C;
                if(overflow){
                    aoi = new Rectangle2D.Double(C.x,
                                                 C.y - tileHeight + h - dy,
                                                 tiledWidth,
                                                 tiledHeight);
                }
                hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST,
                          aoi);
                RenderContext crc
                    = new RenderContext(CTxf, aoi, hints);
                CRed = source.createRendering(crc);
            }
        }
        if(D.getWidth() > 0 && D.getHeight() > 0){
            Rectangle devD = usr2dev.createTransformedShape(D).getBounds();
            if(devD.width > 0 && devD.height > 0){
                AffineTransform DTxf = new AffineTransform(usr2dev);
                DTxf.translate(-D.x + (tiledX + dx),
                               -D.y + (tiledY + dy));
                Shape aoi = D;
                if(overflow){
                    aoi = new Rectangle2D.Double(D.x - tileWidth + w - dx,
                                                 D.y - tileHeight + h - dy,
                                                 tiledWidth,
                                                 tiledHeight);
                }
                hints.put(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST,
                          aoi);
                RenderContext drc
                    = new RenderContext(DTxf, aoi, hints);
                DRed = source.createRendering(drc);
            }
        }
        final Rectangle realTileRectDev
            = usr2dev.createTransformedShape(realTileRect).getBounds();
        if(realTileRectDev.width == 0 || realTileRectDev.height == 0){
            return null;
        }
        BufferedImage realTileBI
            = new BufferedImage(realTileRectDev.width,
                                realTileRectDev.height,
                                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = GraphicsUtil.createGraphics(realTileBI,
                                                   rc.getRenderingHints());
        g.translate(-realTileRectDev.x,
                    -realTileRectDev.y);
        AffineTransform redTxf = new AffineTransform();
        Point2D.Double redVec = new Point2D.Double();
        RenderedImage refRed = null;
        if(ARed != null){
            g.drawRenderedImage(ARed, redTxf);
            refRed = ARed;
        }
        if(BRed != null){
            if(refRed == null){
                refRed = BRed;
            }
            redVec.x = dx;
            redVec.y = 0;
            usr2dev.deltaTransform(redVec, redVec);
            redVec.x = Math.floor(redVec.x) - (BRed.getMinX() - refRed.getMinX());
            redVec.y = Math.floor(redVec.y) - (BRed.getMinY() - refRed.getMinY());
            g.drawRenderedImage(BRed, redTxf);
        }
        if(CRed != null){
            if(refRed == null){
                refRed = CRed;
            }
            redVec.x = 0;
            redVec.y = dy;
            usr2dev.deltaTransform(redVec, redVec);
            redVec.x = Math.floor(redVec.x) - (CRed.getMinX() - refRed.getMinX());
            redVec.y = Math.floor(redVec.y) - (CRed.getMinY() - refRed.getMinY());
            g.drawRenderedImage(CRed, redTxf);
        }
        if(DRed != null){
            if(refRed == null){
                refRed = DRed;
            }
            redVec.x = dx;
            redVec.y = dy;
            usr2dev.deltaTransform(redVec, redVec);
            redVec.x = Math.floor(redVec.x) - (DRed.getMinX() - refRed.getMinX());
            redVec.y = Math.floor(redVec.y) - (DRed.getMinY() - refRed.getMinY());
            g.drawRenderedImage(DRed, redTxf);
        }
        CachableRed realTile;
        realTile = new BufferedImageCachableRed(realTileBI,
                                                realTileRectDev.x,
                                                realTileRectDev.y);
        return realTile;
    }
}
