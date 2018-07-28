package org.apache.batik.ext.awt;
import java.awt.Color;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import org.apache.batik.ext.awt.image.GraphicsUtil;
abstract class MultipleGradientPaintContext implements PaintContext {
    protected static final boolean DEBUG = false;
    protected ColorModel dataModel;
    protected ColorModel model;
    private static ColorModel lrgbmodel_NA = new DirectColorModel
        (ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
         24, 0xff0000, 0xFF00, 0xFF, 0x0,
         false, DataBuffer.TYPE_INT);
    private static ColorModel srgbmodel_NA = new DirectColorModel
        (ColorSpace.getInstance(ColorSpace.CS_sRGB),
         24, 0xff0000, 0xFF00, 0xFF, 0x0,
         false, DataBuffer.TYPE_INT);
    private static ColorModel lrgbmodel_A = new DirectColorModel
        (ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
         32, 0xff0000, 0xFF00, 0xFF, 0xFF000000,
         false, DataBuffer.TYPE_INT);
    private static ColorModel srgbmodel_A = new DirectColorModel
        (ColorSpace.getInstance(ColorSpace.CS_sRGB),
         32, 0xff0000, 0xFF00, 0xFF, 0xFF000000,
         false, DataBuffer.TYPE_INT);
    protected static ColorModel cachedModel;
    protected static WeakReference cached;
    protected WritableRaster saved;
    protected MultipleGradientPaint.CycleMethodEnum cycleMethod;
    protected MultipleGradientPaint.ColorSpaceEnum colorSpace;
    protected float a00, a01, a10, a11, a02, a12;
    protected boolean isSimpleLookup = true;
    protected boolean hasDiscontinuity = false;
    protected int fastGradientArraySize;
    protected int[] gradient;
    protected int[][] gradients;
    protected int gradientAverage;
    protected int gradientUnderflow;
    protected int gradientOverflow;
    protected int gradientsLength;
    protected float[] normalizedIntervals;
    protected float[] fractions;
    private int transparencyTest;
    private static final int[] SRGBtoLinearRGB = new int[256];
    private static final int[] LinearRGBtoSRGB = new int[256];
    static{
        for (int k = 0; k < 256; k++) {
            SRGBtoLinearRGB[k] = convertSRGBtoLinearRGB(k);
            LinearRGBtoSRGB[k] = convertLinearRGBtoSRGB(k);
        }
    }
    protected static final int GRADIENT_SIZE = 256;
    protected static final int GRADIENT_SIZE_INDEX = GRADIENT_SIZE -1;
    private static final int MAX_GRADIENT_ARRAY_SIZE = 5000;
    protected MultipleGradientPaintContext(ColorModel cm,
                                        Rectangle deviceBounds,
                                        Rectangle2D userBounds,
                                        AffineTransform t,
                                        RenderingHints hints,
                                        float[] fractions,
                                        Color[] colors,
                                        MultipleGradientPaint.CycleMethodEnum
                                        cycleMethod,
                                        MultipleGradientPaint.ColorSpaceEnum
                                        colorSpace)
        throws NoninvertibleTransformException
    {
        boolean fixFirst = false;
        boolean fixLast = false;
        int len = fractions.length;
        if (fractions[0] != 0f) {
            fixFirst = true;
            len++;
        }
        if (fractions[fractions.length - 1] != 1.0f) {
            fixLast = true;
            len++;
        }
        for (int i=0; i<fractions.length-1; i++)
            if (fractions[i] == fractions[i+1])
                len--;
        this.fractions      = new float[len];
        Color [] loColors   = new Color[len-1];
        Color [] hiColors   = new Color[len-1];
        normalizedIntervals = new float[len-1];
        gradientUnderflow = colors[0].getRGB();
        gradientOverflow  = colors[colors.length-1].getRGB();
        int idx = 0;
        if (fixFirst) {
            this.fractions[0] = 0;
            loColors[0] = colors[0];
            hiColors[0] = colors[0];
            normalizedIntervals[0] = fractions[0];
            idx++;
        }
        for (int i=0; i<fractions.length-1; i++) {
            if (fractions[i] == fractions[i+1]) {
                if (!colors[i].equals(colors[i+1])) {
                    hasDiscontinuity = true;
                }
                continue;
            }
            this.fractions[idx] = fractions[i];
            loColors[idx] = colors[i];
            hiColors[idx] = colors[i+1];
            normalizedIntervals[idx] = fractions[i+1]-fractions[i];
            idx++;
        }
        this.fractions[idx] = fractions[fractions.length-1];
        if (fixLast) {
            loColors[idx] = hiColors[idx] = colors[colors.length-1];
            normalizedIntervals[idx] = 1-fractions[fractions.length-1];
            idx++;
            this.fractions[idx] = 1;
        }
        AffineTransform tInv = t.createInverse();
        double[] m = new double[6];
        tInv.getMatrix(m);
        a00 = (float)m[0];
        a10 = (float)m[1];
        a01 = (float)m[2];
        a11 = (float)m[3];
        a02 = (float)m[4];
        a12 = (float)m[5];
        this.cycleMethod = cycleMethod;
        this.colorSpace = colorSpace;
        if (cm.getColorSpace() == lrgbmodel_A.getColorSpace())
            dataModel = lrgbmodel_A;
        else if (cm.getColorSpace() == srgbmodel_A.getColorSpace())
            dataModel = srgbmodel_A;
        else
            throw new IllegalArgumentException
                ("Unsupported ColorSpace for interpolation");
        calculateGradientFractions(loColors, hiColors);
        model = GraphicsUtil.coerceColorModel(dataModel,
                                              cm.isAlphaPremultiplied());
    }
    protected final void calculateGradientFractions
        (Color []loColors, Color []hiColors) {
        if (colorSpace == LinearGradientPaint.LINEAR_RGB) {
            int[] workTbl = SRGBtoLinearRGB; 
            for (int i = 0; i < loColors.length; i++) {
                loColors[i] = interpolateColor( workTbl, loColors[ i ] );
                hiColors[i] = interpolateColor( workTbl, hiColors[ i ] );
            }
        }
        transparencyTest = 0xff000000;
        if (cycleMethod == MultipleGradientPaint.NO_CYCLE) {
            transparencyTest &= gradientUnderflow;
            transparencyTest &= gradientOverflow;
        }
        gradients = new int[fractions.length - 1][];
        gradientsLength = gradients.length;
        int n = normalizedIntervals.length;
        float Imin = 1;
        float[] workTbl = normalizedIntervals;   
        for(int i = 0; i < n; i++) {
            Imin = (Imin > workTbl[i]) ? workTbl[i] : Imin;
        }
        int estimatedSize = 0;
        if (Imin == 0) {
            estimatedSize = Integer.MAX_VALUE;
            hasDiscontinuity = true;
        } else {
            for (int i = 0; i < workTbl.length; i++) {
                estimatedSize += (workTbl[i]/Imin) * GRADIENT_SIZE;
            }
        }
        if (estimatedSize > MAX_GRADIENT_ARRAY_SIZE) {
            calculateMultipleArrayGradient(loColors, hiColors);
            if ((cycleMethod == MultipleGradientPaint.REPEAT) &&
                (gradients[0][0] !=
                 gradients[gradients.length-1][GRADIENT_SIZE_INDEX]))
                hasDiscontinuity = true;
        } else {
            calculateSingleArrayGradient(loColors, hiColors, Imin);
            if ((cycleMethod == MultipleGradientPaint.REPEAT) &&
                (gradient[0] != gradient[fastGradientArraySize]))
                hasDiscontinuity = true;
        }
        if((transparencyTest >>> 24) == 0xff) {
            if (dataModel.getColorSpace() == lrgbmodel_NA.getColorSpace())
                dataModel = lrgbmodel_NA;
            else if (dataModel.getColorSpace() == srgbmodel_NA.getColorSpace())
                dataModel = srgbmodel_NA;
            model = dataModel;
        }
    }
    private static Color interpolateColor( int[] workTbl, Color inColor ) {
        int oldColor = inColor.getRGB();
        int newColorValue =
                  (( workTbl[ (oldColor >> 24 ) & 0xff ] & 0xff ) << 24 ) |
                  (( workTbl[ (oldColor >> 16 ) & 0xff ] & 0xff ) << 16 ) |
                  (( workTbl[ (oldColor >>  8 ) & 0xff ] & 0xff ) <<  8 ) |
                  (( workTbl[ (oldColor       ) & 0xff ] & 0xff ));
        return new Color( newColorValue, true );
    }
    private void calculateSingleArrayGradient
        (Color [] loColors, Color [] hiColors, float Imin) {
        isSimpleLookup = true;
        int gradientsTot = 1; 
        int aveA = 0x008000;
        int aveR = 0x008000;
        int aveG = 0x008000;
        int aveB = 0x008000;
        for(int i=0; i < gradients.length; i++){
            int nGradients = (int)((normalizedIntervals[i]/Imin)*255f);
            gradientsTot += nGradients;
            gradients[i] = new int[nGradients];
            int rgb1 = loColors[i].getRGB();
            int rgb2 = hiColors[i].getRGB();
            interpolate(rgb1, rgb2, gradients[i]);
            int argb = gradients[i][GRADIENT_SIZE/2];
            float norm = normalizedIntervals[i];
            aveA += (int)(((argb>> 8)&0xFF0000)*norm);
            aveR += (int)(((argb    )&0xFF0000)*norm);
            aveG += (int)(((argb<< 8)&0xFF0000)*norm);
            aveB += (int)(((argb<<16)&0xFF0000)*norm);
            transparencyTest &=  rgb1 & rgb2;
        }
        gradientAverage = (((aveA & 0xFF0000)<< 8) |
                           ((aveR & 0xFF0000)    ) |
                           ((aveG & 0xFF0000)>> 8) |
                           ((aveB & 0xFF0000)>>16));
        gradient = new int[gradientsTot];
        int curOffset = 0;
        for(int i = 0; i < gradients.length; i++){
            System.arraycopy(gradients[i], 0, gradient,
                             curOffset, gradients[i].length);
            curOffset += gradients[i].length;
        }
        gradient[gradient.length-1] = hiColors[hiColors.length-1].getRGB();
        if (colorSpace == LinearGradientPaint.LINEAR_RGB) {
            if (dataModel.getColorSpace() ==
                ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
                for (int i = 0; i < gradient.length; i++) {
                    gradient[i] =
                        convertEntireColorLinearRGBtoSRGB(gradient[i]);
                }
                gradientAverage =
                    convertEntireColorLinearRGBtoSRGB(gradientAverage);
            }
        } else {
            if (dataModel.getColorSpace() ==
                ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)) {
                for (int i = 0; i < gradient.length; i++) {
                    gradient[i] =
                        convertEntireColorSRGBtoLinearRGB(gradient[i]);
                }
                gradientAverage =
                    convertEntireColorSRGBtoLinearRGB(gradientAverage);
            }
        }
        fastGradientArraySize = gradient.length - 1;
    }
    private void calculateMultipleArrayGradient
        (Color [] loColors, Color [] hiColors) {
        isSimpleLookup = false;
        int rgb1; 
        int rgb2;
        int aveA = 0x008000;
        int aveR = 0x008000;
        int aveG = 0x008000;
        int aveB = 0x008000;
        for(int i=0; i < gradients.length; i++){
            if (normalizedIntervals[i] == 0)
                continue;
            gradients[i] = new int[GRADIENT_SIZE];
            rgb1 = loColors[i].getRGB();
            rgb2 = hiColors[i].getRGB();
            interpolate(rgb1, rgb2, gradients[i]);
            int argb = gradients[i][GRADIENT_SIZE/2];
            float norm = normalizedIntervals[i];
            aveA += (int)(((argb>> 8)&0xFF0000)*norm);
            aveR += (int)(((argb    )&0xFF0000)*norm);
            aveG += (int)(((argb<< 8)&0xFF0000)*norm);
            aveB += (int)(((argb<<16)&0xFF0000)*norm);
            transparencyTest &= rgb1;
            transparencyTest &= rgb2;
        }
        gradientAverage = (((aveA & 0xFF0000)<< 8) |
                           ((aveR & 0xFF0000)    ) |
                           ((aveG & 0xFF0000)>> 8) |
                           ((aveB & 0xFF0000)>>16));
        if (colorSpace == LinearGradientPaint.LINEAR_RGB) {
            if (dataModel.getColorSpace() ==
                ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
                for (int j = 0; j < gradients.length; j++) {
                    for (int i = 0; i < gradients[j].length; i++) {
                        gradients[j][i] =
                            convertEntireColorLinearRGBtoSRGB(gradients[j][i]);
                    }
                }
                gradientAverage =
                    convertEntireColorLinearRGBtoSRGB(gradientAverage);
            }
        } else {
            if (dataModel.getColorSpace() ==
                ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)) {
                for (int j = 0; j < gradients.length; j++) {
                    for (int i = 0; i < gradients[j].length; i++) {
                        gradients[j][i] =
                            convertEntireColorSRGBtoLinearRGB(gradients[j][i]);
                    }
                }
                gradientAverage =
                    convertEntireColorSRGBtoLinearRGB(gradientAverage);
            }
        }
    }
    private void interpolate(int rgb1, int rgb2, int[] output) {
        int nSteps = output.length;
        float stepSize = 1/(float)nSteps;
        int a1 = (rgb1 >> 24) & 0xff;
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >>  8) & 0xff;
        int b1 = (rgb1      ) & 0xff;
        int da = ((rgb2 >> 24) & 0xff) - a1;
        int dr = ((rgb2 >> 16) & 0xff) - r1;
        int dg = ((rgb2 >>  8) & 0xff) - g1;
        int db = ((rgb2      ) & 0xff) - b1;
        float tempA = 2.0f * da * stepSize;
        float tempR = 2.0f * dr * stepSize;
        float tempG = 2.0f * dg * stepSize;
        float tempB = 2.0f * db * stepSize;
        output[ 0 ] = rgb1;             
        nSteps--;                       
        output[ nSteps ] = rgb2;        
        for (int i = 1; i < nSteps; i++) {
            float fI = i;
            output[i] =
                (( a1 + ((((int) ( fI * tempA )) +1) >> 1 ) & 0xff ) << 24) |
                (( r1 + ((((int) ( fI * tempR )) +1) >> 1 ) & 0xff ) << 16) |
                (( g1 + ((((int) ( fI * tempG )) +1) >> 1 ) & 0xff ) <<  8) |
                (( b1 + ((((int) ( fI * tempB )) +1) >> 1 ) & 0xff )      );
        }
    }
    private static int convertEntireColorLinearRGBtoSRGB(int rgb) {
        int a1 = (rgb >> 24) & 0xff;
        int r1 = (rgb >> 16) & 0xff;
        int g1 = (rgb >>  8) & 0xff;
        int b1 =  rgb        & 0xff;
        int[] workTbl = LinearRGBtoSRGB; 
        r1 =  workTbl[r1];
        g1 =  workTbl[g1];
        b1 =  workTbl[b1];
        return ((a1 << 24) |
                (r1 << 16) |
                (g1 << 8) |
                b1);
    }
    private static int convertEntireColorSRGBtoLinearRGB(int rgb) {
        int a1 = (rgb >> 24) & 0xff;
        int r1 = (rgb >> 16) & 0xff;
        int g1 = (rgb >>  8) & 0xff;
        int b1 =  rgb        & 0xff;
        int[] workTbl = SRGBtoLinearRGB; 
        r1 =  workTbl[r1];
        g1 =  workTbl[g1];
        b1 =  workTbl[b1];
        return ((a1 << 24) |
                (r1 << 16) |
                (g1 << 8) |
                b1);
    }
    protected final int indexIntoGradientsArrays(float position) {
        if (cycleMethod == MultipleGradientPaint.NO_CYCLE) {
            if (position >= 1) { 
                return gradientOverflow;
            }
            else if (position <= 0) { 
                return gradientUnderflow;
            }
        }
        else if (cycleMethod == MultipleGradientPaint.REPEAT) {
            position = position - (int)position;
            if (position < 0) {
                position = position + 1; 
            }
            int w=0, c1=0, c2=0;
            if (isSimpleLookup) {
              position *= gradient.length;
              int idx1 = (int)(position);
              if (idx1+1 < gradient.length)
                return gradient[idx1];
              w = (int)((position-idx1)*(1<<16));
              c1 = gradient[idx1];
              c2 = gradient[0];
            } else {
              for (int i = 0; i < gradientsLength; i++) {
                if (position < fractions[i+1]) { 
                  float delta = position - fractions[i];
                  delta = ((delta / normalizedIntervals[i]) * GRADIENT_SIZE);
                  int index = (int)delta;
                  if ((index+1<gradients[i].length) ||
                      (i+1 < gradientsLength))
                    return gradients[i][index];
                  w  = (int)((delta-index)*(1<<16));
                  c1 = gradients[i][index];
                  c2 = gradients[0][0];
                  break;
                }
              }
            }
            return
              ((((  (  (c1>>  8)           &0xFF0000)+
                    ((((c2>>>24)     )-((c1>>>24)     ))*w))&0xFF0000)<< 8) |
               (((  (  (c1     )           &0xFF0000)+
                    ((((c2>> 16)&0xFF)-((c1>> 16)&0xFF))*w))&0xFF0000)    ) |
               (((  (  (c1<<  8)           &0xFF0000)+
                    ((((c2>>  8)&0xFF)-((c1>>  8)&0xFF))*w))&0xFF0000)>> 8) |
               (((  (  (c1<< 16)           &0xFF0000)+
                    ((((c2     )&0xFF)-((c1     )&0xFF))*w))&0xFF0000)>>16));
        }
        else {  
            if (position < 0) {
                position = -position; 
            }
            int part = (int)position; 
            position = position - part; 
            if ((part & 0x00000001) == 1) { 
                position = 1 - position; 
            }
        }
        if (isSimpleLookup) { 
            return gradient[(int)(position * fastGradientArraySize)];
        }
        else { 
            for (int i = 0; i < gradientsLength; i++) {
                if (position < fractions[i+1]) { 
                    float delta = position - fractions[i];
                    int index = (int)((delta / normalizedIntervals[i])
                                      * (GRADIENT_SIZE_INDEX));
                    return gradients[i][index];
                }
            }
        }
        return gradientOverflow;
    }
    protected final int indexGradientAntiAlias(float position, float sz) {
        if (cycleMethod == MultipleGradientPaint.NO_CYCLE) {
            if (DEBUG) System.out.println("NO_CYCLE");
            float p1 = position-(sz/2);
            float p2 = position+(sz/2);
            if (p1 >= 1)
                return gradientOverflow;
            if (p2 <= 0)
                return gradientUnderflow;
            int interior;
            float top_weight=0, bottom_weight=0, frac;
            if (p2 >= 1) {
                top_weight = (p2-1)/sz;
                if (p1 <= 0) {
                    bottom_weight = -p1/sz;
                    frac=1;
                    interior = gradientAverage;
                } else {
                    frac=1-p1;
                    interior = getAntiAlias(p1, true, 1, false, 1-p1, 1);
                }
            } else if (p1 <= 0) {
                bottom_weight = -p1/sz;
                frac = p2;
                interior = getAntiAlias(0, true, p2, false, p2, 1);
            } else
                return getAntiAlias(p1, true, p2, false, sz, 1);
            int norm = (int)((1<<16)*frac/sz);
            int pA = (((interior>>>20)&0xFF0)*norm)>>16;
            int pR = (((interior>> 12)&0xFF0)*norm)>>16;
            int pG = (((interior>>  4)&0xFF0)*norm)>>16;
            int pB = (((interior<<  4)&0xFF0)*norm)>>16;
            if (bottom_weight != 0) {
                int bPix = gradientUnderflow;
                norm = (int)((1<<16)*bottom_weight);
                pA += (((bPix>>>20) & 0xFF0)*norm)>>16;
                pR += (((bPix>> 12) & 0xFF0)*norm)>>16;
                pG += (((bPix>>  4) & 0xFF0)*norm)>>16;
                pB += (((bPix<<  4) & 0xFF0)*norm)>>16;
            }
            if (top_weight != 0) {
                int tPix = gradientOverflow;
                norm = (int)((1<<16)*top_weight);
                pA += (((tPix>>>20) & 0xFF0)*norm)>>16;
                pR += (((tPix>> 12) & 0xFF0)*norm)>>16;
                pG += (((tPix>>  4) & 0xFF0)*norm)>>16;
                pB += (((tPix<<  4) & 0xFF0)*norm)>>16;
            }
            return (((pA&0xFF0)<<20)  |
                    ((pR&0xFF0)<<12)  |
                    ((pG&0xFF0)<< 4)  |
                    ((pB&0xFF0)>> 4));
        }
        int intSz = (int)sz;
        float weight = 1.0f;
        if (intSz != 0) {
            sz -= intSz;
            weight = sz/(intSz+sz);
            if (weight < 0.1)
                return gradientAverage;
        }
        if (sz > 0.99)
            return gradientAverage;
        float p1 = position-(sz/2);
        float p2 = position+(sz/2);
        if (DEBUG) System.out.println("P1: " + p1 + " P2: " + p2);
        boolean p1_up=true;
        boolean p2_up=false;
        if (cycleMethod == MultipleGradientPaint.REPEAT) {
            if (DEBUG) System.out.println("REPEAT");
            p1=p1-(int)p1;
            p2=p2-(int)p2;
            if (p1 <0) p1 += 1;
            if (p2 <0) p2 += 1;
        }
        else {  
            if (DEBUG) System.out.println("REFLECT");
            if (p2 < 0) {
                p1 = -p1; p1_up = !p1_up;
                p2 = -p2; p2_up = !p2_up;
            } else if (p1 < 0) {
                p1 = -p1; p1_up = !p1_up;
            }
            int part1, part2;
            part1 = (int)p1;   
            p1   = p1 - part1; 
            part2 = (int)p2;   
            p2   = p2 - part2; 
            if ((part1 & 0x01) == 1) {
                p1 = 1-p1;
                p1_up = !p1_up;
            }
            if ((part2 & 0x01) == 1) {
                p2 = 1-p2;
                p2_up = !p2_up;
            }
            if ((p1 > p2) && !p1_up && p2_up) {
                float t = p1;
                p1 = p2;
                p2 = t;
                p1_up = true;
                p2_up = false;
            }
        }
        return getAntiAlias(p1, p1_up, p2, p2_up, sz, weight);
    }
    private final int getAntiAlias(float p1, boolean p1_up,
                                   float p2, boolean p2_up,
                                   float sz, float weight) {
        int ach=0, rch=0, gch=0, bch=0;
        if (isSimpleLookup) {
            p1 *= fastGradientArraySize;
            p2 *= fastGradientArraySize;
            int idx1 = (int)p1;
            int idx2 = (int)p2;
            int i, pix;
            if (p1_up && !p2_up && (idx1 <= idx2)) {
                if (idx1 == idx2)
                    return gradient[idx1];
                for (i=idx1+1; i<idx2; i++) {
                    pix  = gradient[i];
                    ach += ((pix>>>20)&0xFF0);
                    rch += ((pix>>>12)&0xFF0);
                    gch += ((pix>>> 4)&0xFF0);
                    bch += ((pix<<  4)&0xFF0);
                }
            } else {
                int iStart;
                int iEnd;
                if (p1_up) {
                    iStart = idx1+1;
                    iEnd = fastGradientArraySize;
                } else {
                    iStart = 0;
                    iEnd = idx1;
                }
                for ( i = iStart; i < iEnd; i++) {
                        pix  = gradient[i];
                        ach += ((pix>>>20)&0xFF0);
                        rch += ((pix>>>12)&0xFF0);
                        gch += ((pix>>> 4)&0xFF0);
                        bch += ((pix<<  4)&0xFF0);
                }
                if (p2_up) {
                    iStart = idx2 + 1;
                    iEnd = fastGradientArraySize;
                } else {
                    iStart = 0;
                    iEnd = idx2;
                }
                for (i= iStart; i < iEnd; i++) {
                        pix  = gradient[i];
                        ach += ((pix>>>20)&0xFF0);
                        rch += ((pix>>>12)&0xFF0);
                        gch += ((pix>>> 4)&0xFF0);
                        bch += ((pix<<  4)&0xFF0);
                }
            }
            int norm, isz;
            isz = (int)((1<<16)/(sz*fastGradientArraySize));
            ach = (ach*isz)>>16;
            rch = (rch*isz)>>16;
            gch = (gch*isz)>>16;
            bch = (bch*isz)>>16;
            if (p1_up) norm = (int)((1-(p1-idx1))*isz);
            else       norm = (int)(   (p1-idx1) *isz);
            pix = gradient[idx1];
            ach += (((pix>>>20)&0xFF0) *norm)>>16;
            rch += (((pix>>>12)&0xFF0) *norm)>>16;
            gch += (((pix>>> 4)&0xFF0) *norm)>>16;
            bch += (((pix<<  4)&0xFF0) *norm)>>16;
            if (p2_up) norm = (int)((1-(p2-idx2))*isz);
            else       norm = (int)(   (p2-idx2) *isz);
            pix = gradient[idx2];
            ach += (((pix>>>20)&0xFF0) *norm)>>16;
            rch += (((pix>>>12)&0xFF0) *norm)>>16;
            gch += (((pix>>> 4)&0xFF0) *norm)>>16;
            bch += (((pix<<  4)&0xFF0) *norm)>>16;
            ach = (ach+0x08)>>4;
            rch = (rch+0x08)>>4;
            gch = (gch+0x08)>>4;
            bch = (bch+0x08)>>4;
        } else {
            int idx1=0, idx2=0;
            int i1=-1, i2=-1;
            float f1=0, f2=0;
            for (int i = 0; i < gradientsLength; i++) {
                if ((p1 < fractions[i+1]) && (i1 == -1)) {
                    i1 = i;
                    f1 = p1 - fractions[i];
                    f1 = ((f1/normalizedIntervals[i])
                             *GRADIENT_SIZE_INDEX);
                    idx1 = (int)f1;
                    if (i2 != -1) break;
                }
                if ((p2 < fractions[i+1]) && (i2 == -1)) {
                    i2 = i;
                    f2 = p2 - fractions[i];
                    f2 = ((f2/normalizedIntervals[i])
                             *GRADIENT_SIZE_INDEX);
                    idx2 = (int)f2;
                    if (i1 != -1) break;
                }
            }
            if (i1 == -1) {
                i1 = gradients.length - 1;
                f1 = idx1 = GRADIENT_SIZE_INDEX;
            }
            if (i2 == -1) {
                i2 = gradients.length - 1;
                f2 = idx2 = GRADIENT_SIZE_INDEX;
            }
            if (DEBUG) System.out.println("I1: " + i1 + " Idx1: " + idx1 +
                                          " I2: " + i2 + " Idx2: " + idx2);
            if ((i1 == i2) && (idx1 <= idx2) && p1_up && !p2_up)
                return gradients[i1][(idx1+idx2+1)>>1];
            int pix, norm;
            int base = (int)((1<<16)/sz);
            if ((i1 < i2) && p1_up && !p2_up) {
                norm = (int)((base
                              *normalizedIntervals[i1]
                              *(GRADIENT_SIZE_INDEX-f1))
                             /GRADIENT_SIZE_INDEX);
                pix  = gradients[i1][(idx1+GRADIENT_SIZE)>>1];
                ach += (((pix>>>20)&0xFF0) *norm)>>16;
                rch += (((pix>>>12)&0xFF0) *norm)>>16;
                gch += (((pix>>> 4)&0xFF0) *norm)>>16;
                bch += (((pix<<  4)&0xFF0) *norm)>>16;
                for (int i=i1+1; i<i2; i++) {
                    norm = (int)(base*normalizedIntervals[i]);
                    pix  = gradients[i][GRADIENT_SIZE>>1];
                    ach += (((pix>>>20)&0xFF0) *norm)>>16;
                    rch += (((pix>>>12)&0xFF0) *norm)>>16;
                    gch += (((pix>>> 4)&0xFF0) *norm)>>16;
                    bch += (((pix<<  4)&0xFF0) *norm)>>16;
                }
                norm = (int)((base*normalizedIntervals[i2]*f2)
                             /GRADIENT_SIZE_INDEX);
                pix  = gradients[i2][(idx2+1)>>1];
                ach += (((pix>>>20)&0xFF0) *norm)>>16;
                rch += (((pix>>>12)&0xFF0) *norm)>>16;
                gch += (((pix>>> 4)&0xFF0) *norm)>>16;
                bch += (((pix<<  4)&0xFF0) *norm)>>16;
            } else {
                if (p1_up) {
                    norm = (int)((base
                                  *normalizedIntervals[i1]
                                  *(GRADIENT_SIZE_INDEX-f1))
                                 /GRADIENT_SIZE_INDEX);
                    pix  = gradients[i1][(idx1+GRADIENT_SIZE)>>1];
                } else {
                    norm = (int)((base*normalizedIntervals[i1]*f1)
                                 /GRADIENT_SIZE_INDEX);
                    pix  = gradients[i1][(idx1+1)>>1];
                }
                ach += (((pix>>>20)&0xFF0) *norm)>>16;
                rch += (((pix>>>12)&0xFF0) *norm)>>16;
                gch += (((pix>>> 4)&0xFF0) *norm)>>16;
                bch += (((pix<<  4)&0xFF0) *norm)>>16;
                if (p2_up) {
                    norm = (int)((base
                                  *normalizedIntervals[i2]
                                  *(GRADIENT_SIZE_INDEX-f2))
                                 /GRADIENT_SIZE_INDEX);
                    pix  =  gradients[i2][(idx2+GRADIENT_SIZE)>>1];
                } else {
                    norm = (int)((base*normalizedIntervals[i2]*f2)
                                 /GRADIENT_SIZE_INDEX);
                    pix  = gradients[i2][(idx2+1)>>1];
                }
                ach += (((pix>>>20)&0xFF0) *norm)>>16;
                rch += (((pix>>>12)&0xFF0) *norm)>>16;
                gch += (((pix>>> 4)&0xFF0) *norm)>>16;
                bch += (((pix<<  4)&0xFF0) *norm)>>16;
                int iStart;
                int iEnd;
                if (p1_up) {
                    iStart = i1+1;
                    iEnd = gradientsLength;
                } else {
                    iStart = 0;
                    iEnd = i1;
                }
                for (int i=iStart; i < iEnd ; i++) {
                    norm = (int)(base*normalizedIntervals[i]);
                    pix  = gradients[i][GRADIENT_SIZE>>1];
                    ach += (((pix>>>20)&0xFF0) *norm)>>16;
                    rch += (((pix>>>12)&0xFF0) *norm)>>16;
                    gch += (((pix>>> 4)&0xFF0) *norm)>>16;
                    bch += (((pix<<  4)&0xFF0) *norm)>>16;
                }
                if (p2_up) {
                    iStart = i2+1;
                    iEnd = gradientsLength;
                } else {
                    iStart = 0;
                    iEnd = i2;
                }
                for (int i=iStart; i < iEnd ; i++) {
                    norm = (int)(base*normalizedIntervals[i]);
                    pix  = gradients[i][GRADIENT_SIZE>>1];
                    ach += (((pix>>>20)&0xFF0) *norm)>>16;
                    rch += (((pix>>>12)&0xFF0) *norm)>>16;
                    gch += (((pix>>> 4)&0xFF0) *norm)>>16;
                    bch += (((pix<<  4)&0xFF0) *norm)>>16;
                }
            }
            ach = (ach+0x08)>>4;
            rch = (rch+0x08)>>4;
            gch = (gch+0x08)>>4;
            bch = (bch+0x08)>>4;
            if (DEBUG) System.out.println("Pix: [" + ach + ", " + rch +
                                          ", " + gch + ", " + bch + ']' );
        }
        if (weight != 1) {
            int aveW = (int)((1<<16)*(1-weight));
            int aveA = ((gradientAverage>>>24) & 0xFF)*aveW;
            int aveR = ((gradientAverage>> 16) & 0xFF)*aveW;
            int aveG = ((gradientAverage>>  8) & 0xFF)*aveW;
            int aveB = ((gradientAverage     ) & 0xFF)*aveW;
            int iw = (int)(weight*(1<<16));
            ach = ((ach*iw)+aveA)>>16;
            rch = ((rch*iw)+aveR)>>16;
            gch = ((gch*iw)+aveG)>>16;
            bch = ((bch*iw)+aveB)>>16;
        }
        return ((ach<<24) | (rch<<16) | (gch<<8) | bch);
    }
    private static int convertSRGBtoLinearRGB(int color) {
        float output;
        float input = color/255.0f;
        if (input <= 0.04045f) {
            output = input/12.92f;
        } else {
            output = (float) Math.pow((input + 0.055) / 1.055, 2.4);
        }
        int o = Math.round(output * 255.0f);
        return o;
    }
    private static int convertLinearRGBtoSRGB(int color) {
        float output;
        float input = color/255.0f;
        if (input <= 0.0031308f) {
            output = input * 12.92f;
        } else {
            output = (1.055f * ((float) Math.pow(input, (1.0 / 2.4)))) - 0.055f;
        }
        int o = Math.round(output * 255.0f);
        return o;
    }
    public final Raster getRaster(int x, int y, int w, int h) {
        if (w == 0 || h == 0) {
            return null;
        }
        WritableRaster raster = saved;
        if (raster == null || raster.getWidth() < w || raster.getHeight() < h)
            {
                raster = getCachedRaster(dataModel, w, h);
                saved = raster;
                raster = raster.createWritableChild
                    (raster.getMinX(), raster.getMinY(), w, h, 0, 0, null);
            }
        DataBufferInt rasterDB = (DataBufferInt)raster.getDataBuffer();
        int[] pixels = rasterDB.getBankData()[0];
        int off = rasterDB.getOffset();
        int scanlineStride = ((SinglePixelPackedSampleModel)
                              raster.getSampleModel()).getScanlineStride();
        int adjust = scanlineStride - w;
        fillRaster(pixels, off, adjust, x, y, w, h); 
        GraphicsUtil.coerceData(raster, dataModel,
                                model.isAlphaPremultiplied());
        return raster;
    }
    protected abstract void fillRaster(int[] pixels, int off, int adjust,
                                       int x, int y, int w, int h);
    protected static final
    synchronized WritableRaster getCachedRaster
        (ColorModel cm, int w, int h) {
        if (cm == cachedModel) {
            if (cached != null) {
                WritableRaster ras = (WritableRaster) cached.get();
                if (ras != null &&
                    ras.getWidth() >= w &&
                    ras.getHeight() >= h)
                    {
                        cached = null;
                        return ras;
                    }
            }
        }
        if (w<32) w=32;
        if (h<32) h=32;
        return cm.createCompatibleWritableRaster(w, h);
    }
    protected static final
    synchronized void putCachedRaster(ColorModel cm,
                                             WritableRaster ras) {
        if (cached != null) {
            WritableRaster cras = (WritableRaster) cached.get();
            if (cras != null) {
                int cw = cras.getWidth();
                int ch = cras.getHeight();
                int iw = ras.getWidth();
                int ih = ras.getHeight();
                if (cw >= iw && ch >= ih) {
                    return;
                }
                if (cw * ch >= iw * ih) {
                    return;
                }
            }
        }
        cachedModel = cm;
        cached = new WeakReference(ras);
    }
    public final void dispose() {
        if (saved != null) {
            putCachedRaster(model, saved);
            saved = null;
        }
    }
    public final ColorModel getColorModel() {
        return model;
    }
}
