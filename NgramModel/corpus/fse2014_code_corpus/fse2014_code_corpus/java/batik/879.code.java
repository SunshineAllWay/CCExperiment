package org.apache.batik.ext.awt.image.rendered;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
public class PadRed extends AbstractRed {
    static final boolean DEBUG=false;
    PadMode padMode;
    RenderingHints hints;
    public PadRed(CachableRed    src,
                  Rectangle      bounds,
                  PadMode        padMode,
                  RenderingHints hints) {
        super(src,bounds,src.getColorModel(),
              fixSampleModel(src, bounds),
              bounds.x, bounds.y,
              null);
        this.padMode = padMode;
        if (DEBUG) {
            System.out.println("Src: " + src + " Bounds: " + bounds +
                               " Off: " +
                               src.getTileGridXOffset() + ", " +
                               src.getTileGridYOffset());
        }
        this.hints = hints;
    }
    public WritableRaster copyData(WritableRaster wr) {
        CachableRed src = (CachableRed)getSources().get(0);
        Rectangle srcR = src.getBounds();
        Rectangle wrR  = wr.getBounds();
        if (wrR.intersects(srcR)) {
            Rectangle r = wrR.intersection(srcR);
            WritableRaster srcWR;
            srcWR = wr.createWritableChild(r.x, r.y, r.width, r.height,
                                           r.x, r.y, null);
            src.copyData(srcWR);
        }
        if (padMode == PadMode.ZERO_PAD) {
            handleZero(wr);
        } else if (padMode == PadMode.REPLICATE) {
            handleReplicate(wr);
        } else if (padMode == PadMode.WRAP) {
            handleWrap(wr);
        }
        return wr;
    }
    protected static class ZeroRecter {
        WritableRaster wr;
        int bands;
        static int [] zeros=null;
        public ZeroRecter(WritableRaster wr) {
            this.wr = wr;
            this.bands = wr.getSampleModel().getNumBands();
        }
        public void zeroRect(Rectangle r) {
            synchronized (this) {
                if ((zeros == null) || (zeros.length <r.width*bands))
                    zeros = new int[r.width*bands];
            }
            for (int y=0; y<r.height; y++) {
                wr.setPixels(r.x, r.y+y, r.width, 1, zeros);
            }
        }
        public static ZeroRecter getZeroRecter(WritableRaster wr) {
            if (GraphicsUtil.is_INT_PACK_Data(wr.getSampleModel(), false))
                return new ZeroRecter_INT_PACK(wr);
            else
                return new ZeroRecter(wr);
        }
        public static void zeroRect(WritableRaster wr) {
            ZeroRecter zr = getZeroRecter(wr);
            zr.zeroRect(wr.getBounds());
        }
    }
    protected static class ZeroRecter_INT_PACK extends ZeroRecter {
        final int base;
        final int scanStride;
        final int[] pixels;
        final int[] zeros;
        final int x0, y0;
        public ZeroRecter_INT_PACK(WritableRaster wr) {
            super(wr);
            SinglePixelPackedSampleModel sppsm;
            sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
            scanStride = sppsm.getScanlineStride();
            DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
            x0 = wr.getMinY();
            y0 = wr.getMinX();
            base = (db.getOffset() +
                    sppsm.getOffset(x0-wr.getSampleModelTranslateX(),
                                    y0-wr.getSampleModelTranslateY()));
            pixels = db.getBankData()[0];
            if (wr.getWidth() > 10)
                zeros = new int[wr.getWidth()];
            else
                zeros = null;
        }
        public void zeroRect(Rectangle r) {
            final int rbase = base+(r.x-x0) + (r.y-y0)*scanStride;
            if (r.width > 10) {
                for (int y=0; y<r.height; y++) {
                    int sp = rbase + y*scanStride;
                    System.arraycopy(zeros, 0, pixels, sp, r.width);
                }
            } else {
                int sp = rbase;
                int end = sp +r.width;
                int adj = scanStride-r.width;
                for (int y=0; y<r.height; y++) {
                    while (sp < end)
                        pixels[sp++] = 0;
                    sp  += adj;
                    end += scanStride;
                }
            }
        }
    }
    protected void handleZero(WritableRaster wr) {
        CachableRed src  = (CachableRed)getSources().get(0);
        Rectangle   srcR = src.getBounds();
        Rectangle   wrR  = wr.getBounds();
        ZeroRecter zr = ZeroRecter.getZeroRecter(wr);
        Rectangle ar = new Rectangle(wrR.x, wrR.y, wrR.width, wrR.height);
        Rectangle dr = new Rectangle(wrR.x, wrR.y, wrR.width, wrR.height);
        if (DEBUG) {
            System.out.println("WrR: " + wrR + " srcR: " + srcR);
        }
        if (ar.x < srcR.x) {
            int w = srcR.x-ar.x;
            if (w > ar.width) w=ar.width;
            dr.width = w;
            zr.zeroRect(dr);
            ar.x+=w;
            ar.width-=w;
        }
        if (DEBUG) {
            System.out.println("WrR: [" +
                               ar.x + "," + ar.y + "," +
                               ar.width + "," + ar.height +
                               "] s rcR: " + srcR);
        }
        if (ar.y < srcR.y) {
            int h = srcR.y-ar.y;
            if (h > ar.height) h=ar.height;
            dr.x      = ar.x;
            dr.y      = ar.y;
            dr.width  = ar.width;
            dr.height = h;
            zr.zeroRect(dr);
            ar.y     +=h;
            ar.height-=h;
        }
        if (DEBUG) {
            System.out.println("WrR: [" +
                               ar.x + "," + ar.y + "," +
                               ar.width + "," + ar.height +
                               "] srcR: " + srcR);
        }
        if (ar.y+ar.height > srcR.y+srcR.height) {
            int h = (ar.y+ar.height) - (srcR.y+srcR.height);
            if (h > ar.height) h=ar.height;
            int y0 = ar.y+ar.height-h; 
            dr.x      = ar.x;
            dr.y      = y0;
            dr.width  = ar.width;
            dr.height = h;
            zr.zeroRect(dr);
            ar.height -= h;
        }
        if (DEBUG) {
            System.out.println("WrR: [" +
                               ar.x + "," + ar.y + "," +
                               ar.width + "," + ar.height +
                               "] srcR: " + srcR);
        }
        if (ar.x+ar.width > srcR.x+srcR.width) {
            int w = (ar.x+ar.width) - (srcR.x+srcR.width);
            if (w > ar.width) w=ar.width;
            int x0 = ar.x+ar.width-w; 
            dr.x      = x0;
            dr.y      = ar.y;
            dr.width  = w;
            dr.height = ar.height;
            zr.zeroRect(dr);
            ar.width-=w;
        }
    }
    protected void handleReplicate(WritableRaster wr) {
        CachableRed src  = (CachableRed)getSources().get(0);
        Rectangle   srcR = src.getBounds();
        Rectangle   wrR  = wr.getBounds();
        int x      = wrR.x;
        int y      = wrR.y;
        int width  = wrR.width;
        int height = wrR.height;
        Rectangle   r;
        {
            int minX = (srcR.x > x) ? srcR.x : x;
            int maxX = (((srcR.x+srcR.width-1) < (x+width-1)) ?
                        ( srcR.x+srcR.width-1) : (x+width-1));
            int minY = (srcR.y > y) ? srcR.y : y;
            int maxY = (((srcR.y+srcR.height-1) < (y+height-1)) ?
                        ( srcR.y+srcR.height-1) : (y+height-1));
            int x0 = minX;
            int w = maxX-minX+1;
            int y0 = minY;
            int h = maxY-minY+1;
            if (w <0 ) { x0 = 0; w = 0; }
            if (h <0 ) { y0 = 0; h = 0; }
            r = new Rectangle(x0, y0, w, h);
        }
        if (y < srcR.y) {
            int repW = r.width;
            int repX = r.x;
            int wrX  = r.x;
            int wrY  = y;
            if (x+width-1 <= srcR.x) {
                repW = 1;
                repX = srcR.x;
                wrX  = x+width-1;
            } else if (x >= srcR.x+srcR.width) {
                repW = 1;
                repX = srcR.x+srcR.width-1;
                wrX  = x;
            }
            WritableRaster wr1 = wr.createWritableChild(wrX, wrY,
                                                        repW, 1,
                                                        repX, srcR.y, null);
            src.copyData(wr1);
            wrY++;
            int endY = srcR.y;
            if (y+height < endY) endY = y+height;
            if (wrY < endY) {
                int [] pixels = wr.getPixels(wrX, wrY-1,
                                             repW, 1, (int [])null);
                while (wrY < srcR.y) {
                    wr.setPixels(wrX, wrY, repW, 1, pixels);
                    wrY++;
                }
            }
        }
        if ((y+height) > (srcR.y+srcR.height)) {
            int repW = r.width;
            int repX = r.x;
            int repY = srcR.y+srcR.height-1;
            int wrX  = r.x;
            int wrY  = srcR.y+srcR.height;
            if (wrY < y) wrY = y;
            if (x+width <= srcR.x) {
                repW = 1;
                repX = srcR.x;
                wrX  = x+width-1;
            } else if (x >= srcR.x+srcR.width) {
                repW = 1;
                repX = srcR.x+srcR.width-1;
                wrX  = x;
            }
            if (DEBUG) {
                System.out.println("wr: "  + wr.getBounds());
                System.out.println("req: [" + wrX + ", " + wrY + ", " +
                                   repW + ", 1]");
            }
            WritableRaster wr1 = wr.createWritableChild(wrX, wrY,
                                                        repW, 1,
                                                        repX, repY, null);
            src.copyData(wr1);
            wrY++;
            int endY = y+height;
            if (wrY < endY) {
                int [] pixels = wr.getPixels(wrX, wrY-1,
                                             repW, 1, (int [])null);
                while (wrY < endY) {
                    wr.setPixels(wrX, wrY, repW, 1, pixels);
                    wrY++;
                }
            }
        }
        if (x < srcR.x) {
            int wrX = srcR.x;
            if (x+width <= srcR.x) {
                wrX = x+width-1;
            }
            int xLoc = x;
            int [] pixels = wr.getPixels(wrX, y, 1, height, (int [])null);
            while (xLoc < wrX) {
                wr.setPixels(xLoc, y, 1, height, pixels);
                xLoc++;
            }
        }
        if (x+width > srcR.x+srcR.width) {
            int wrX = srcR.x+srcR.width-1;
            if (x >= srcR.x+srcR.width) {
                wrX = x;
            }
            int xLoc = wrX+1;
            int endX = x+width-1;
            int [] pixels = wr.getPixels(wrX, y, 1, height, (int [])null);
            while (xLoc < endX) {
                wr.setPixels(xLoc, y, 1, height, pixels);
                xLoc++;
            }
        }
    }
    protected void handleWrap(WritableRaster wr) {
        handleZero(wr);
    }
    protected static SampleModel fixSampleModel(CachableRed src,
                                                Rectangle   bounds) {
        int defSz = AbstractTiledRed.getDefaultTileSize();
        SampleModel sm = src.getSampleModel();
        int w = sm.getWidth();
        if (w < defSz) w = defSz;
        if (w > bounds.width)  w = bounds.width;
        int h = sm.getHeight();
        if (h < defSz) h = defSz;
        if (h > bounds.height) h = bounds.height;
        return sm.createCompatibleSampleModel(w, h);
    }
}
