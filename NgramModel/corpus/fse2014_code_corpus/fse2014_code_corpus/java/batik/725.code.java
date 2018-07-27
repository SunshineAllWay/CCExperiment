package org.apache.batik.ext.awt.color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
public class ICCColorSpaceExt extends ICC_ColorSpace {
    public static final int PERCEPTUAL = 0;
    public static final int RELATIVE_COLORIMETRIC = 1;
    public static final int ABSOLUTE_COLORIMETRIC = 2;
    public static final int SATURATION = 3;
    public static final int AUTO = 4;
    static final ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int intent;
    public ICCColorSpaceExt(ICC_Profile p, int intent){
        super(p);
        this.intent = intent;
        switch(intent){
        case AUTO:
        case RELATIVE_COLORIMETRIC:
        case ABSOLUTE_COLORIMETRIC:
        case SATURATION:
        case PERCEPTUAL:
            break;
        default:
            throw new IllegalArgumentException();
        }
        if(intent != AUTO){
            byte[] hdr = p.getData(ICC_Profile.icSigHead);
            hdr[ICC_Profile.icHdrRenderingIntent] = (byte)intent;
        }
    }
    public float[] intendedToRGB(float[] values){
        switch(intent){
            case ABSOLUTE_COLORIMETRIC:
            return absoluteColorimetricToRGB(values);
            case PERCEPTUAL:
            case AUTO:
            return perceptualToRGB(values);
            case RELATIVE_COLORIMETRIC:
            return relativeColorimetricToRGB(values);
            case SATURATION:
            return saturationToRGB(values);
            default:
            throw new Error("invalid intent:" + intent );
        }
    }
    public float[] perceptualToRGB(float[] values){
        return toRGB(values);
    }
    public float[] relativeColorimetricToRGB(float[] values){
        float[] ciexyz = toCIEXYZ(values);
        return sRGB.fromCIEXYZ(ciexyz);
    }
    public float[] absoluteColorimetricToRGB(float[] values){
        return perceptualToRGB(values);
    }
    public float[] saturationToRGB(float[] values){
        return perceptualToRGB(values);
    }
}
