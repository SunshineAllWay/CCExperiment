package org.apache.batik.ext.awt;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
final class RadialGradientPaintContext extends MultipleGradientPaintContext {
    private boolean isSimpleFocus = false;
    private boolean isNonCyclic = false;
    private float radius;
    private float centerX, centerY, focusX, focusY;
    private float radiusSq;
    private float constA, constB;
    private float trivial;
    private static final int FIXED_POINT_IMPL = 1;
    private static final int DEFAULT_IMPL     = 2;
    private static final int ANTI_ALIAS_IMPL  = 3;
    private int fillMethod;
    private static final float SCALEBACK = 0.999f;
    public RadialGradientPaintContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform t,
                                      RenderingHints hints,
                                      float cx, float cy,
                                      float r,
                                      float fx, float fy,
                                      float[] fractions,
                                      Color[] colors,
                                      MultipleGradientPaint.CycleMethodEnum
                                      cycleMethod,
                                      MultipleGradientPaint.ColorSpaceEnum
                                      colorSpace)
        throws NoninvertibleTransformException
    {
        super(cm, deviceBounds, userBounds, t, hints, fractions, colors,
              cycleMethod, colorSpace);
        centerX = cx;
        centerY = cy;
        focusX = fx;
        focusY = fy;
        radius = r;
        this.isSimpleFocus = (focusX == centerX) && (focusY == centerY);
        this.isNonCyclic = (cycleMethod == RadialGradientPaint.NO_CYCLE);
        radiusSq = radius * radius;
        float dX = focusX - centerX;
        float dY = focusY - centerY;
        double dist = Math.sqrt((dX * dX) + (dY * dY));
        if (dist > radius* SCALEBACK) { 
          double angle = Math.atan2(dY, dX);
          focusX = (float)(SCALEBACK * radius * Math.cos(angle)) + centerX;
          focusY = (float)(SCALEBACK * radius * Math.sin(angle)) + centerY;
        }
        dX = focusX - centerX;
        trivial = (float)Math.sqrt(radiusSq - (dX * dX));
        constA = a02 - centerX;
        constB = a12 - centerY;
        Object colorRend = hints.get(RenderingHints.KEY_COLOR_RENDERING);
        Object rend      = hints.get(RenderingHints.KEY_RENDERING);
        fillMethod = 0;
        if ((rend      == RenderingHints.VALUE_RENDER_QUALITY) ||
            (colorRend == RenderingHints.VALUE_COLOR_RENDER_QUALITY)) {
            fillMethod = ANTI_ALIAS_IMPL;
        }
        if ((rend      == RenderingHints.VALUE_RENDER_SPEED) ||
            (colorRend == RenderingHints.VALUE_COLOR_RENDER_SPEED)) {
            fillMethod = DEFAULT_IMPL;
        }
        if (fillMethod == 0) {
            fillMethod = DEFAULT_IMPL;
            if (false) {
                if (hasDiscontinuity) {
                    fillMethod = ANTI_ALIAS_IMPL;
                } else {
                    fillMethod = DEFAULT_IMPL;
                }
            }
        }
        if ((fillMethod == DEFAULT_IMPL) &&
            (isSimpleFocus && isNonCyclic && isSimpleLookup)) {
            this.calculateFixedPointSqrtLookupTable();
            fillMethod = FIXED_POINT_IMPL;
        }
    }
    protected void fillRaster(int[] pixels, int off, int adjust,
                              int x, int y, int w, int h) {
        switch(fillMethod) {
        case FIXED_POINT_IMPL:
            fixedPointSimplestCaseNonCyclicFillRaster(pixels, off, adjust, x,
                                                      y, w, h);
            break;
        case ANTI_ALIAS_IMPL:
            antiAliasFillRaster(pixels, off, adjust, x, y, w, h);
            break;
        case DEFAULT_IMPL:
        default:
            cyclicCircularGradientFillRaster(pixels, off, adjust, x, y, w, h);
        }
    }
    private void fixedPointSimplestCaseNonCyclicFillRaster(int[] pixels,
                                                           int off,
                                                           int adjust,
                                                           int x, int y,
                                                           int w, int h) {
        float iSq=0;  
        final float indexFactor = fastGradientArraySize / radius;
        final float constX = (a00*x) + (a01*y) + constA;
        final float constY = (a10*x) + (a11*y) + constB;
        final float deltaX = indexFactor * a00; 
        final float deltaY = indexFactor * a10; 
        float dX, dY; 
        final int fixedArraySizeSq=
            (fastGradientArraySize * fastGradientArraySize);
        float g, gDelta, gDeltaDelta, temp; 
        int gIndex; 
        int iSqInt; 
        int end, j; 
        int indexer = off;
        temp        = ((deltaX * deltaX) + (deltaY * deltaY));
        gDeltaDelta = ((temp * 2));
        if (temp > fixedArraySizeSq) {
            final int val = gradientOverflow;
            for(j = 0; j < h; j++){ 
                for (end = indexer+w; indexer < end; indexer++)
                    pixels[indexer] = val;
                indexer += adjust;
            }
            return;
        }
        for(j = 0; j < h; j++){ 
            dX = indexFactor * ((a01*j) + constX);
            dY = indexFactor * ((a11*j) + constY);
            g = (((dY * dY) + (dX * dX)) );
            gDelta = (deltaY * dY + deltaX * dX) * 2 + temp;
            for (end = indexer+w; indexer < end; indexer++) {
                if (g >= fixedArraySizeSq) {
                    pixels[indexer] = gradientOverflow;
                }
                else {
                    iSq = (g * invSqStepFloat);
                    iSqInt = (int)iSq; 
                    iSq -= iSqInt;
                    gIndex = sqrtLutFixed[iSqInt];
                    gIndex += (int)(iSq * (sqrtLutFixed[iSqInt + 1]-gIndex));
                    pixels[indexer] = gradient[gIndex];
                }
                g += gDelta;
                gDelta += gDeltaDelta;
            }
            indexer += adjust;
        }
    }
    private float invSqStepFloat;
    private static final int MAX_PRECISION = 256;
    private int[] sqrtLutFixed = new int[MAX_PRECISION];
    private void calculateFixedPointSqrtLookupTable() {
        float sqStepFloat;
        sqStepFloat = (fastGradientArraySize * fastGradientArraySize)
                       / (MAX_PRECISION - 2.0f);
        int[] workTbl = sqrtLutFixed;      
        int i;
        for (i = 0; i < MAX_PRECISION - 1; i++) {
            workTbl[i] = (int)Math.sqrt(i*sqStepFloat);
        }
        workTbl[i] = workTbl[i-1];
        invSqStepFloat = 1.0f/sqStepFloat;
    }
    private void cyclicCircularGradientFillRaster(int[] pixels, int off,
                                                  int adjust,
                                                  int x, int y,
                                                  int w, int h) {
        final double constC =
            -(radiusSq) + (centerX * centerX) + (centerY * centerY);
        double A; 
        double B; 
        double C; 
        double slope; 
        double yintcpt; 
        double solutionX;
        double solutionY;
               final float constX = (a00*x) + (a01*y) + a02;
        final float constY = (a10*x) + (a11*y) + a12; 
               final float precalc2 = 2 * centerY;
        final float precalc3 =-2 * centerX;
        float X; 
        float Y; 
        float g;
        float det; 
        float currentToFocusSq;
        float intersectToFocusSq;
        float deltaXSq; 
        float deltaYSq; 
        int indexer = off; 
        int i, j; 
        int pixInc = w+adjust;
        for (j = 0; j < h; j++) { 
            X = (a01*j) + constX; 
            Y = (a11*j) + constY;
            for (i = 0; i < w; i++) {
                if (((X-focusX)>-0.000001f) &&
                    ((X-focusX)< 0.000001f)) {
                    solutionX = focusX;
                    solutionY = centerY;
                    solutionY += (Y > focusY)?trivial:-trivial;
                }
                else {
                    slope =   (Y - focusY) / (X - focusX);
                    yintcpt = Y - (slope * X); 
                    A = (slope * slope) + 1;
                    B =  precalc3 + (-2 * slope * (centerY - yintcpt));
                    C =  constC + (yintcpt* (yintcpt - precalc2));
                    det = (float)Math.sqrt((B * B) - ( 4 * A * C));
                    solutionX = -B;
                    solutionX += (X < focusX)?-det:det;
                    solutionX = solutionX / (2 * A);
                    solutionY = (slope * solutionX) + yintcpt;
                }
                deltaXSq = (float)solutionX - focusX;
                deltaXSq = deltaXSq * deltaXSq;
                deltaYSq = (float)solutionY - focusY;
                deltaYSq = deltaYSq * deltaYSq;
                intersectToFocusSq = deltaXSq + deltaYSq;
                deltaXSq = X - focusX;
                deltaXSq = deltaXSq * deltaXSq;
                deltaYSq = Y - focusY;
                deltaYSq = deltaYSq * deltaYSq;
                currentToFocusSq = deltaXSq + deltaYSq;
                g = (float)Math.sqrt(currentToFocusSq / intersectToFocusSq);
                pixels[indexer + i] = indexIntoGradientsArrays(g);
                X += a00; 
                Y += a10;
            } 
            indexer += pixInc;
        } 
    }
    private void antiAliasFillRaster(int[] pixels, int off,
                                     int adjust,
                                     int x, int y,
                                     int w, int h) {
        final double constC =
            -(radiusSq) + (centerX * centerX) + (centerY * centerY);
               final float precalc2 = 2 * centerY;
        final float precalc3 =-2 * centerX;
               final float constX = (a00*(x-.5f)) + (a01*(y+.5f)) + a02;
        final float constY = (a10*(x-.5f)) + (a11*(y+.5f)) + a12;
        float X; 
        float Y; 
        int i, j; 
        int indexer = off-1; 
        double [] prevGs = new double[w+1];
        double deltaXSq, deltaYSq;
        double solutionX, solutionY;
        double slope, yintcpt, A, B, C, det;
        double intersectToFocusSq, currentToFocusSq;
        double g00, g01, g10, g11;
        X = constX - a01;
        Y = constY - a11;
        for (i=0; i <= w; i++) {
            final float dx = X - focusX;
            if ( ( dx >-0.000001f ) &&
                 ( dx < 0.000001f ))  {
                solutionX = focusX;
                solutionY = centerY;
                solutionY += (Y > focusY)?trivial:-trivial;
            }
            else {
                slope =   (Y - focusY) / (X - focusX);
                yintcpt = Y - (slope * X); 
                A = (slope * slope) + 1;
                B =  precalc3 + (-2 * slope * (centerY - yintcpt));
                C =  constC + (yintcpt* (yintcpt - precalc2));
                det = Math.sqrt((B * B) - ( 4 * A * C));
                solutionX = -B;
                solutionX += (X < focusX)?-det:det;
                solutionX = solutionX / (2 * A);
                solutionY = (slope * solutionX) + yintcpt;
            }
            deltaXSq = solutionX - focusX;
            deltaXSq = deltaXSq * deltaXSq;
            deltaYSq = solutionY - focusY;
            deltaYSq = deltaYSq * deltaYSq;
            intersectToFocusSq = deltaXSq + deltaYSq;
            deltaXSq = X - focusX;
            deltaXSq = deltaXSq * deltaXSq;
            deltaYSq = Y - focusY;
            deltaYSq = deltaYSq * deltaYSq;
            currentToFocusSq = deltaXSq + deltaYSq;
            prevGs[i] = Math.sqrt(currentToFocusSq / intersectToFocusSq);
            X += a00; 
            Y += a10;
        }
        for (j = 0; j < h; j++) { 
            X = (a01*j) + constX; 
            Y = (a11*j) + constY;
            g10 = prevGs[0];
            float dx = X - focusX;
            if ( ( dx >-0.000001f ) &&
                 ( dx < 0.000001f ))  {
                solutionX = focusX;
                solutionY = centerY;
                solutionY += (Y > focusY)?trivial:-trivial;
            }
            else {
                slope =   (Y - focusY) / (X - focusX);
                yintcpt = Y - (slope * X); 
                A = (slope * slope) + 1;
                B =  precalc3 + (-2 * slope * (centerY - yintcpt));
                C =  constC + (yintcpt* (yintcpt - precalc2));
                det = Math.sqrt((B * B) - ( 4 * A * C));
                solutionX = -B;
                solutionX += (X < focusX)?-det:det;
                solutionX = solutionX / (2 * A);
                solutionY = (slope * solutionX) + yintcpt;
            }
            deltaXSq = solutionX - focusX;
            deltaXSq = deltaXSq * deltaXSq;
            deltaYSq = solutionY - focusY;
            deltaYSq = deltaYSq * deltaYSq;
            intersectToFocusSq = deltaXSq + deltaYSq;
            deltaXSq = X - focusX;
            deltaXSq = deltaXSq * deltaXSq;
            deltaYSq = Y - focusY;
            deltaYSq = deltaYSq * deltaYSq;
            currentToFocusSq = deltaXSq + deltaYSq;
            g11 = Math.sqrt(currentToFocusSq / intersectToFocusSq);
            prevGs[0] = g11;
            X += a00; 
            Y += a10;
            for (i=1; i <= w; i++) {
                g00 = g10;
                g01 = g11;
                g10 = prevGs[i];
                dx = X - focusX;
                if ( ( dx >-0.000001f ) &&
                     ( dx < 0.000001f ))  {
                    solutionX = focusX;
                    solutionY = centerY;
                    solutionY += (Y > focusY)?trivial:-trivial;
                }
                else {
                    slope =   (Y - focusY) / (X - focusX);
                    yintcpt = Y - (slope * X); 
                    A = (slope * slope) + 1;
                    B =  precalc3 + (-2 * slope * (centerY - yintcpt));
                    C =  constC + (yintcpt* (yintcpt - precalc2));
                    det = Math.sqrt((B * B) - ( 4 * A * C));
                    solutionX = -B;
                    solutionX += (X < focusX)?-det:det;
                    solutionX = solutionX / (2 * A);
                    solutionY = (slope * solutionX) + yintcpt;
                }
                deltaXSq = solutionX - focusX;
                deltaXSq = deltaXSq * deltaXSq;
                deltaYSq = solutionY - focusY;
                deltaYSq = deltaYSq * deltaYSq;
                intersectToFocusSq = deltaXSq + deltaYSq;
                deltaXSq = X - focusX;
                deltaXSq = deltaXSq * deltaXSq;
                deltaYSq = Y - focusY;
                deltaYSq = deltaYSq * deltaYSq;
                currentToFocusSq = deltaXSq + deltaYSq;
                g11 = Math.sqrt(currentToFocusSq / intersectToFocusSq);
                prevGs[i] = g11;
                pixels[indexer+i] = indexGradientAntiAlias
                    ((float)((g00+g01+g10+g11)/4),
                     (float)Math.max(Math.abs(g11-g00),
                                     Math.abs(g10-g01)));
                X += a00; 
                Y += a10;
            } 
            indexer += (w+adjust);
        } 
    }
}
