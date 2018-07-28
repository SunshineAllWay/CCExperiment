package org.apache.batik.ext.awt.g2d;
import java.awt.geom.AffineTransform;
public abstract class TransformStackElement implements Cloneable{
    private TransformType type;
    private double[] transformParameters;
    protected TransformStackElement(TransformType type,
                                    double[] transformParameters){
        this.type = type;
        this.transformParameters = transformParameters;
    }
    public Object clone() {
        TransformStackElement newElement = null;
        try {
            newElement = (TransformStackElement) super.clone();
        } catch(java.lang.CloneNotSupportedException ex) {}
        double[] transformParameters = new double[this.transformParameters.length];
        System.arraycopy(this.transformParameters, 0, transformParameters, 0, transformParameters.length);
        newElement.transformParameters = transformParameters;
        return newElement;
    }
    public static TransformStackElement createTranslateElement(double tx,
                                                               double ty){
        return new TransformStackElement(TransformType.TRANSLATE,
                                         new double[]{ tx, ty }) {
                boolean isIdentity(double[] parameters) {
                    return parameters[0] == 0 && parameters[1] == 0;
                }
            };
    }
    public static TransformStackElement createRotateElement(double theta){
        return new TransformStackElement(TransformType.ROTATE,
                                         new double[]{ theta }) {
                boolean isIdentity(double[] parameters) {
                    return Math.cos(parameters[0]) == 1;
                }
            };
    }
    public static TransformStackElement createScaleElement(double scaleX,
                                                           double scaleY){
        return new TransformStackElement(TransformType.SCALE,
                                         new double[]{ scaleX, scaleY }) {
                boolean isIdentity(double[] parameters) {
                    return parameters[0] == 1 && parameters[1] == 1;
                }
            };
    }
    public static TransformStackElement createShearElement(double shearX,
                                                           double shearY){
        return new TransformStackElement(TransformType.SHEAR,
                                         new double[]{ shearX, shearY }) {
                boolean isIdentity(double[] parameters) {
                    return parameters[0] == 0 && parameters[1] == 0;
                }
            };
    }
    public static TransformStackElement createGeneralTransformElement
        (AffineTransform txf){
        double[] matrix = new double[6];
        txf.getMatrix(matrix);
        return new TransformStackElement(TransformType.GENERAL, matrix) {
                boolean isIdentity(double[] m) {
                    return (m[0] == 1 && m[2] == 0 && m[4] == 0 &&
                            m[1] == 0 && m[3] == 1 && m[5] == 0);
                }
            };
    }
    abstract boolean isIdentity(double[] parameters);
    public boolean isIdentity() {
        return isIdentity(transformParameters);
    }
    public double[] getTransformParameters(){
        return transformParameters;
    }
    public TransformType getType(){
        return type;
    }
    public boolean concatenate(TransformStackElement stackElement){
        boolean canConcatenate = false;
        if(type.toInt() == stackElement.type.toInt()){
            canConcatenate = true;
            switch(type.toInt()){
            case TransformType.TRANSFORM_TRANSLATE:
                transformParameters[0] += stackElement.transformParameters[0];
                transformParameters[1] += stackElement.transformParameters[1];
                break;
            case TransformType.TRANSFORM_ROTATE:
                transformParameters[0] += stackElement.transformParameters[0];
                break;
            case TransformType.TRANSFORM_SCALE:
                transformParameters[0] *= stackElement.transformParameters[0];
                transformParameters[1] *= stackElement.transformParameters[1];
                break;
            case TransformType.TRANSFORM_GENERAL:
                transformParameters
                    = matrixMultiply(transformParameters,
                                     stackElement.transformParameters);
                break;
            default:
                canConcatenate = false;
            }
        }
        return canConcatenate;
    }
    private double[] matrixMultiply(double[] matrix1, double[] matrix2) {
        double[] product = new double[6];
        AffineTransform transform1 = new AffineTransform(matrix1);
        transform1.concatenate(new AffineTransform(matrix2));
        transform1.getMatrix(product);
        return product;
    }
}
