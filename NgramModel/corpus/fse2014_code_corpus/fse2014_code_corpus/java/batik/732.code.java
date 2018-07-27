package org.apache.batik.ext.awt.g2d;
public class TransformType{
    public static final int TRANSFORM_TRANSLATE = 0;
    public static final int TRANSFORM_ROTATE = 1;
    public static final int TRANSFORM_SCALE = 2;
    public static final int TRANSFORM_SHEAR = 3;
    public static final int TRANSFORM_GENERAL = 4;
    public static final String TRANSLATE_STRING = "translate";
    public static final String ROTATE_STRING = "rotate";
    public static final String SCALE_STRING = "scale";
    public static final String SHEAR_STRING = "shear";
    public static final String GENERAL_STRING = "general";
    public static final TransformType TRANSLATE = new TransformType(TRANSFORM_TRANSLATE, TRANSLATE_STRING);
    public static final TransformType ROTATE = new TransformType(TRANSFORM_ROTATE, ROTATE_STRING);
    public static final TransformType SCALE = new TransformType(TRANSFORM_SCALE, SCALE_STRING);
    public static final TransformType SHEAR = new TransformType(TRANSFORM_SHEAR, SHEAR_STRING);
    public static final TransformType GENERAL = new TransformType(TRANSFORM_GENERAL, GENERAL_STRING);
    private String desc;
    private int val;
    private TransformType(int val, String desc){
        this.desc = desc;
        this.val = val;
    }
    public String toString(){
        return desc;
    }
    public int toInt(){
        return val;
    }
    public Object readResolve() {
        switch(val){
        case TRANSFORM_TRANSLATE:
            return TransformType.TRANSLATE;
        case TRANSFORM_ROTATE:
            return TransformType.ROTATE;
        case TRANSFORM_SCALE:
            return TransformType.SCALE;
        case TRANSFORM_SHEAR:
            return TransformType.SHEAR;
        case TRANSFORM_GENERAL:
            return TransformType.GENERAL;
        default:
            throw new Error("Unknown TransformType value:" + val );
        }
    }
}
