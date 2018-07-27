package org.apache.batik.ext.awt.image.renderable;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.rendered.ColorMatrixRed;
public class ColorMatrixRable8Bit
    extends    AbstractColorInterpolationRable
    implements ColorMatrixRable {
    private static float[][] MATRIX_LUMINANCE_TO_ALPHA
        = {
            {0,       0,       0,       0, 0},
            {0,       0,       0,       0, 0},
            {0,       0,       0,       0, 0},
            {0.2125f, 0.7154f, 0.0721f, 0, 0}
        };
    private int type;
    private float[][] matrix;
    public void setSource(Filter src){
        init(src, null);
    }
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }
    public int getType(){
        return type;
    }
    public float[][] getMatrix(){
        return matrix;
    }
    private ColorMatrixRable8Bit(){
    }
    public static ColorMatrixRable buildMatrix(float[][] matrix){
        if(matrix == null){
            throw new IllegalArgumentException();
        }
        if(matrix.length != 4){
            throw new IllegalArgumentException();
        }
        float[][] newMatrix = new float[4][];
        for(int i=0; i<4; i++){
            float[] m = matrix[i];
            if(m == null){
                throw new IllegalArgumentException();
            }
            if(m.length != 5){
                throw new IllegalArgumentException();
            }
            newMatrix[i] = new float[5];
            for(int j=0; j<5; j++){
                newMatrix[i][j] = m[j];
            }
        }
        ColorMatrixRable8Bit filter
            = new ColorMatrixRable8Bit();
        filter.type = TYPE_MATRIX;
        filter.matrix = newMatrix;
        return filter;
    }
    public static ColorMatrixRable buildSaturate(float s){
        ColorMatrixRable8Bit filter
            = new ColorMatrixRable8Bit();
        filter.type = TYPE_SATURATE;
        filter.matrix = new float[][] {
            { 0.213f+0.787f*s,  0.715f-0.715f*s, 0.072f-0.072f*s, 0, 0 },
            { 0.213f-0.213f*s,  0.715f+0.285f*s, 0.072f-0.072f*s, 0, 0 },
            { 0.213f-0.213f*s,  0.715f-0.715f*s, 0.072f+0.928f*s, 0, 0 },
            { 0,                0,               0,               1, 0 }
        };
        return filter;
    }
    public static ColorMatrixRable buildHueRotate(float a){
        ColorMatrixRable8Bit filter
            = new ColorMatrixRable8Bit();
        filter.type = TYPE_HUE_ROTATE;
        float cos = (float)Math.cos(a);
        float sin = (float)Math.sin(a);
        float a00 = 0.213f + cos*0.787f - sin*0.213f;
        float a10 = 0.213f - cos*0.212f + sin*0.143f;
        float a20 = 0.213f - cos*0.213f - sin*0.787f;
        float a01 = 0.715f - cos*0.715f - sin*0.715f;
        float a11 = 0.715f + cos*0.285f + sin*0.140f;
        float a21 = 0.715f - cos*0.715f + sin*0.715f;
        float a02 = 0.072f - cos*0.072f + sin*0.928f;
        float a12 = 0.072f - cos*0.072f - sin*0.283f;
        float a22 = 0.072f + cos*0.928f + sin*0.072f;
        filter.matrix = new float[][] {
            { a00, a01, a02, 0, 0 },
            { a10, a11, a12, 0, 0 },
            { a20, a21, a22, 0, 0 },
            { 0,   0,   0,   1, 0 }};
        return filter;
    }
    public static ColorMatrixRable buildLuminanceToAlpha(){
        ColorMatrixRable8Bit filter
            = new ColorMatrixRable8Bit();
        filter.type = TYPE_LUMINANCE_TO_ALPHA;
        filter.matrix = MATRIX_LUMINANCE_TO_ALPHA;
        return filter;
    }
    public RenderedImage createRendering(RenderContext rc) {
        RenderedImage srcRI = getSource().createRendering(rc);
        if(srcRI == null)
            return null;
        return new ColorMatrixRed(convertSourceCS(srcRI), matrix);
    }
}
