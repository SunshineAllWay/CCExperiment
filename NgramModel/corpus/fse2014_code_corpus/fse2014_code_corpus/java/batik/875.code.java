package org.apache.batik.ext.awt.image.rendered;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class IndexImage{
    private static class Counter {
        final int val;
        int count=1;
        Counter(int val) {  this.val = val; }
        boolean add(int val) {
            if (this.val != val)
                return false;
            count++;
            return true;
        }
        int[] getRgb( int[] rgb ){
            rgb[ Cube.RED ] = (val&0xFF0000)>>16;
            rgb[ Cube.GRN ] = (val&0x00FF00)>>8;
            rgb[ Cube.BLU ] = (val&0x0000FF);
            return rgb;
        }
   }
    private static class Cube {
        static final byte[] RGB_BLACK= new byte[]{ 0, 0, 0 };
        int[] min = {0, 0, 0}, max={255,255,255};
        boolean done = false;
        final Counter[][] colors;
        int count=0;
        static final int RED = 0;
        static final int GRN = 1;
        static final int BLU = 2;
        Cube( Counter[][] colors, int count) {
            this.colors = colors;
            this.count = count;
        }
        public boolean isDone() { return done; }
        private boolean contains( int[] val ){
            int vRed = val[ RED ]; 
            int vGrn = val[ GRN ];
            int vBlu = val[ BLU ];
            return (
                ( ( min[ RED ] <= vRed ) && ( vRed <= max[ RED ]))&&
                ( ( min[ GRN ] <= vGrn ) && ( vGrn <= max[ GRN ]))&&
                ( ( min[ BLU ] <= vBlu ) && ( vBlu <= max[ BLU ])));
        }
        Cube split() {
            int dr = max[ RED ]-min[ RED ]+1;
            int dg = max[ GRN ]-min[ GRN ]+1;
            int db = max[ BLU ]-min[ BLU ]+1;
            int c0, c1, splitChannel;
            if (dr >= dg) {
                if (dr >= db) { splitChannel = RED; c0=GRN; c1=BLU; }
                else          { splitChannel = BLU; c0=RED; c1=GRN; }
            } else if (dg >= db) {
                splitChannel = GRN;
                c0=RED;
                c1=BLU;
            } else {
                splitChannel = BLU;
                c0=GRN;
                c1=RED;
            }
            Cube ret;
            ret = splitChannel(splitChannel, c0, c1);
            if (ret != null ) return ret;
            ret = splitChannel(c0, splitChannel, c1);
            if (ret != null ) return ret;
            ret = splitChannel(c1, splitChannel, c0);
            if (ret != null) return ret;
            done = true;
            return null;
        }
        private void normalize( int splitChannel, int[] counts ){
            if ( count == 0 ){
                return;
            }
            int iMin = min[ splitChannel ];
            int iMax = max[ splitChannel ];
            int loBound = -1;
            int hiBound = -1;
            for( int i = iMin; i <= iMax; i++ ){
                if ( counts[ i ] == 0 ){
                    continue;
                }
                loBound = i;
                break;
            }
            for( int i= iMax; i >= iMin; i-- ){
                if ( counts[ i ] == 0 ){
                    continue;
                }
                hiBound = i;
                break;
            }
            boolean flagChangedLo = (loBound != -1 ) && ( iMin != loBound );
            boolean flagChangedHi = (hiBound != -1 ) && ( iMax != hiBound );
            if ( flagChangedLo ){
                min[ splitChannel ]= loBound;
            }
            if ( flagChangedHi ){
                max[ splitChannel ]= hiBound;
            }
        }
        Cube splitChannel(int splitChannel, int c0, int c1) {
            if (min[splitChannel] == max[splitChannel]) {
                return null;
            }
            if ( count == 0 ){
                return null;
            }
            int half = count/2;
            int[] counts = computeCounts( splitChannel, c0, c1 );
            int tcount=0;
            int lastAdd=-1;
            int splitLo=min[splitChannel];
            int splitHi=max[splitChannel];
            for (int i=min[splitChannel]; i<=max[splitChannel]; i++) {
                int c = counts[i];
                if (c == 0) {
                    if ((tcount == 0) && (i < max[splitChannel]))
                        min[splitChannel] = i+1;
                    continue;
                }
                if (tcount+c < half) {
                    lastAdd = i;
                    tcount+=c;
                    continue;
                }
                if ((half-tcount) <= ((tcount+c)-half)) {
                    if (lastAdd == -1) {
                        if (c == count) {
                            max[splitChannel] = i;
                            return null; 
                        } else {
                            splitLo = i;
                            splitHi = i+1;
                            tcount += c;    
                            break;
                        }
                    }
                    splitLo = lastAdd;
                    splitHi = i;
                } else {
                    if (i == max[splitChannel]) {
                        if ( c == count) {
                            return null; 
                        } else {
                            splitLo = lastAdd;
                            splitHi = i;
                            break;
                        }
                    }
                    tcount += c;
                    splitLo = i;
                    splitHi = i+1;
                }
                break;
            }
            Cube ret = new Cube(colors, tcount);
            count = count-tcount;
            ret.min[splitChannel] = min[splitChannel];
            ret.max[splitChannel] = splitLo;
            min[splitChannel] = splitHi;
            ret.min[c0] = min[c0];
            ret.max[c0] = max[c0];
            ret.min[c1] = min[c1];
            ret.max[c1] = max[c1];
            normalize( splitChannel, counts );
            ret.normalize( splitChannel, counts );
            return ret;
        }
        private int[] computeCounts( int splitChannel, int c0, int c1) {
            int splitSh4 = (2-splitChannel)*4;
            int c0Sh4    = (2-c0)*4;
            int c1Sh4    = (2-c1)*4;
            int half = count/2;
            int[] counts = new int[256];
            int tcount = 0;
            int minR=min[0], minG=min[1], minB=min[2];
            int maxR=max[0], maxG=max[1], maxB=max[2];
            int[] minIdx = { minR >> 4, minG >> 4, minB >> 4 };
            int[] maxIdx = { maxR >> 4, maxG >> 4, maxB >> 4 };
            int [] vals = {0, 0, 0};
            for (int i=minIdx[splitChannel]; i<=maxIdx[splitChannel]; i++) {
                int idx1 = i<<splitSh4;
                for (int j=minIdx[c0]; j <=maxIdx[c0]; j++) {
                    int idx2 = idx1 | (j<<c0Sh4);
                    for (int k=minIdx[c1]; k<=maxIdx[c1]; k++) {
                        int idx = idx2 | (k<<c1Sh4);
                        Counter[] v = colors[idx];
                        for( int iColor = 0; iColor < v.length; iColor++ ){
                            Counter c = v[ iColor ];
                            vals = c.getRgb( vals );
                            if ( contains( vals )){
                                counts[ vals[splitChannel] ] += c.count;
                                tcount += c.count;
                            }
                        }
                    }
                }
            }
            return counts;
        }
        public String toString() {
            return "Cube: [" +
                    min[ RED ] + '-' + max[ RED ] + "] [" +
                    min[ GRN ] + '-' + max[ GRN ] + "] [" +
                    min[ BLU ] + '-' + max[ BLU ] + "] n:" + count;
        }
        public int averageColor() {
            if (count == 0) {
                return 0;
            }
            byte[] rgb = averageColorRGB( null );
            return (( rgb[ RED ] << 16 ) & 0x00FF0000)
                 | (( rgb[ GRN ] <<  8 ) & 0x0000FF00)
                 | (( rgb[ BLU ]       ) & 0x000000FF);
        }
        public byte[] averageColorRGB( byte[] rgb ) {
            if (count == 0) return RGB_BLACK;
            float red=0, grn=0, blu=0;
            int minR=min[0], minG=min[1], minB=min[2];
            int maxR=max[0], maxG=max[1], maxB=max[2];
            int [] minIdx = {minR>>4, minG>>4, minB>>4};
            int [] maxIdx = {maxR>>4, maxG>>4, maxB>>4};
            int[] vals = new int[3];
            for (int i=minIdx[0]; i<=maxIdx[0]; i++) {
                int idx1 = i<<8;
                for (int j=minIdx[1]; j<=maxIdx[1]; j++) {
                    int idx2 = idx1 | (j<<4);
                    for (int k=minIdx[2]; k<=maxIdx[2]; k++) {
                        int idx = idx2 | k;
                        Counter[] v = colors[idx];
                        for( int iColor = 0; iColor < v.length; iColor++ ){
                            Counter c = v[ iColor ];
                            vals = c.getRgb( vals );
                            if ( contains( vals ) ) {
                                float weight = (c.count/(float)count);
                                red += (vals[0]*weight);
                                grn += (vals[1]*weight);
                                blu += (vals[2]*weight);
                            }
                        }
                    }
                }
            }
            byte[] result = (rgb == null) ? new byte[3] : rgb;
            result[ RED ] = (byte)(red + 0.5f);
            result[ GRN ] = (byte)(grn + 0.5f);
            result[ BLU ] = (byte)(blu + 0.5f);
            return result;
        }
    }
    static byte[][] computeRGB( int nCubes, Cube[] cubes ){
        byte[] r = new byte[nCubes];
        byte[] g = new byte[nCubes];
        byte[] b = new byte[nCubes];
        byte[] rgb = new byte[3];
        for (int i=0; i<nCubes; i++) {
            rgb = cubes[i].averageColorRGB( rgb );
            r[i] = rgb[ Cube.RED ];
            g[i] = rgb[ Cube.GRN ];
            b[i] = rgb[ Cube.BLU ];
        }
        byte[][] result = new byte[3][];
        result[ Cube.RED ] = r;
        result[ Cube.GRN ] = g;
        result[ Cube.BLU ] = b;
        return result;
    }
    static void logRGB( byte[] r, byte[] g, byte[] b ){
        StringBuffer buff = new StringBuffer( 100 );
        int nColors = r.length;
        for( int i= 0; i < nColors; i++ ) {
            String rgbStr= "(" + (r[i]+128) + ',' + (g[i] +128 ) + ',' + (b[i] + 128) + ")," ;
            buff.append( rgbStr );
        }
        System.out.println("RGB:" + nColors + buff );
    }
    static List[] createColorList( BufferedImage bi ){
        int w= bi.getWidth();
        int h= bi.getHeight();
        List[] colors = new ArrayList[1<<12];
        for(int i_w=0; i_w<w; i_w++){
            for(int i_h=0; i_h<h; i_h++){
                int rgb=(bi.getRGB(i_w,i_h) & 0x00FFFFFF);  
                int idx = (((rgb&0xF00000)>>> 12) |
                           ((rgb&0x00F000)>>>  8) |
                           ((rgb&0x0000F0)>>>  4));
                List v = colors[idx];
                if (v == null) {
                    v = new ArrayList();
                    v.add(new Counter(rgb));
                    colors[idx] = v;
                } else {
                    Iterator i = v.iterator();
                    while (true) {
                        if (i.hasNext()) {
                            if (((Counter)i.next()).add(rgb)) break;
                        } else {
                            v.add(new Counter(rgb));
                            break;
                        }
                    }
                }
            }
        }
        return colors;
    }
    static Counter[][] convertColorList( List[] colors ){
        final Counter[] EMPTY_COUNTER = new Counter[0];
        Counter[][] colorTbl= new Counter[ 1<< 12 ][];
        for( int i= 0; i < colors.length; i++ ){
            List cl = colors[ i ];
            if ( cl == null ){
                colorTbl[ i ] = EMPTY_COUNTER;
                continue;
            }
            int nSlots = cl.size();
            colorTbl[i] = (Counter[])cl.toArray( new Counter[ nSlots ] );
            colors[ i ] = null;
        }
        return colorTbl;
    }
    public static BufferedImage getIndexedImage( BufferedImage bi, int nColors) {
        int w=bi.getWidth();
        int h=bi.getHeight();
        List[] colors = createColorList( bi );
        Counter[][] colorTbl = convertColorList( colors );
        colors = null;
        int nCubes=1;
        int fCube=0;
        Cube [] cubes = new Cube[nColors];
        cubes[0] = new Cube(colorTbl, w*h);
        while (nCubes < nColors) {
            while (cubes[fCube].isDone()) {
                fCube++;
                if (fCube == nCubes) break;
            }
            if (fCube == nCubes) {
                break;
            }
            Cube c = cubes[fCube];
            Cube nc = c.split();
            if (nc != null) {
                if (nc.count > c.count) {
                    Cube tmp = c; c= nc; nc = tmp;
                }
                int j = fCube;
                int cnt = c.count;
                for (int i=fCube+1; i<nCubes; i++) {
                    if (cubes[i].count < cnt)
                        break;
                    cubes[j++] = cubes[i];
                }
                cubes[j++] = c;
                cnt = nc.count;
                while (j<nCubes) {
                    if (cubes[j].count < cnt)
                        break;
                    j++;
                }
                for (int i=nCubes; i>j; i--)
                    cubes[i] = cubes[i-1];
                cubes[j++] = nc;
                nCubes++;
            }
        }
        byte[][] rgbTbl = computeRGB( nCubes, cubes );
        IndexColorModel icm= new IndexColorModel( 8, nCubes, rgbTbl[0], rgbTbl[1], rgbTbl[2] );
        BufferedImage indexed =new BufferedImage
            (w, h, BufferedImage.TYPE_BYTE_INDEXED, icm);
        Graphics2D g2d=indexed.createGraphics();
        g2d.setRenderingHint
            (RenderingHints.KEY_DITHERING,
             RenderingHints.VALUE_DITHER_ENABLE);
        g2d.drawImage(bi, 0, 0, null);
        g2d.dispose();
        int bits;
        for (bits=1; bits <=8; bits++) {
            if ((1<<bits) >= nCubes) break;
        }
        if (bits > 4) {
            return indexed;
        }
        if (bits ==3) bits = 4;
        ColorModel cm = new IndexColorModel(bits,nCubes, 
                                            rgbTbl[0], rgbTbl[1], rgbTbl[2] );
        SampleModel sm;
        sm = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h, bits);
        WritableRaster ras = Raster.createWritableRaster( sm, new Point(0,0));
        bi = indexed;
        indexed = new BufferedImage(cm, ras, bi.isAlphaPremultiplied(), null);
        GraphicsUtil.copyData(bi, indexed);
        return indexed;
    }
}
