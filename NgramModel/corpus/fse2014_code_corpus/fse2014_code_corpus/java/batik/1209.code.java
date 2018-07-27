package org.apache.batik.svggen;
import java.util.Stack;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.apache.batik.ext.awt.g2d.TransformType;
public class SVGTransform extends AbstractSVGConverter{
    private static double radiansToDegrees = 180.0 / Math.PI;
    public SVGTransform(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc){
        return new SVGTransformDescriptor(toSVGTransform(gc));
    }
    public final String toSVGTransform(GraphicContext gc){
        return toSVGTransform(gc.getTransformStack());
    }
    public final String toSVGTransform(TransformStackElement[] transformStack){
        int nTransforms = transformStack.length;
        Stack presentation = new Stack() {
            public Object push(Object o) {
                Object element;
                if(((TransformStackElement)o).isIdentity()) {
                    element = pop();
                } else {
                    super.push(o);
                    element = null;
                }
                return element;
            }
            public Object pop() {
                Object element = null;
                if(!super.empty()) {
                    element = super.pop();
                }
                return element;
            }
        };
        boolean canConcatenate = false;
        int i = 0, j = 0, next = 0;
        TransformStackElement element = null;
        while(i < nTransforms) {
            next = i;
            if(element == null) {
                element = (TransformStackElement) transformStack[i].clone();
                next++;
            }
            canConcatenate = true;
            for(j = next; j < nTransforms; j++) {
                canConcatenate = element.concatenate(transformStack[j]);
                if(!canConcatenate)
                    break;
            }
            i = j;
            element = (TransformStackElement) presentation.push(element);
        }
        if (element != null){
            presentation.push(element);
        }
        int nPresentations = presentation.size();
        StringBuffer transformStackBuffer = new StringBuffer( nPresentations * 8 );
        for(i = 0; i < nPresentations; i++) {
            transformStackBuffer.append(convertTransform((TransformStackElement) presentation.get(i)));
            transformStackBuffer.append(SPACE);
        }
        String transformValue = transformStackBuffer.toString().trim();
        return transformValue;
    }
    final String convertTransform(TransformStackElement transformElement){
        StringBuffer transformString = new StringBuffer();
        double[] transformParameters = transformElement.getTransformParameters();
        switch(transformElement.getType().toInt()){
        case TransformType.TRANSFORM_TRANSLATE:
            if(!transformElement.isIdentity()) {
                transformString.append(TRANSFORM_TRANSLATE);
                transformString.append(OPEN_PARENTHESIS);
                transformString.append(doubleString(transformParameters[0]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[1]));
                transformString.append(CLOSE_PARENTHESIS);
            }
            break;
        case TransformType.TRANSFORM_ROTATE:
            if(!transformElement.isIdentity()) {
                transformString.append(TRANSFORM_ROTATE);
                transformString.append(OPEN_PARENTHESIS);
                transformString.append(doubleString(radiansToDegrees*transformParameters[0]));
                transformString.append(CLOSE_PARENTHESIS);
            }
            break;
        case TransformType.TRANSFORM_SCALE:
            if(!transformElement.isIdentity()) {
                transformString.append(TRANSFORM_SCALE);
                transformString.append(OPEN_PARENTHESIS);
                transformString.append(doubleString(transformParameters[0]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[1]));
                transformString.append(CLOSE_PARENTHESIS);
            }
            break;
        case TransformType.TRANSFORM_SHEAR:
            if(!transformElement.isIdentity()) {
                transformString.append(TRANSFORM_MATRIX);
                transformString.append(OPEN_PARENTHESIS);
                transformString.append(1);
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[1]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[0]));
                transformString.append(COMMA);
                transformString.append(1);
                transformString.append(COMMA);
                transformString.append(0);
                transformString.append(COMMA);
                transformString.append(0);
                transformString.append(CLOSE_PARENTHESIS);
            }
            break;
        case TransformType.TRANSFORM_GENERAL:
            if(!transformElement.isIdentity()) {
                transformString.append(TRANSFORM_MATRIX);
                transformString.append(OPEN_PARENTHESIS);
                transformString.append(doubleString(transformParameters[0]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[1]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[2]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[3]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[4]));
                transformString.append(COMMA);
                transformString.append(doubleString(transformParameters[5]));
                transformString.append(CLOSE_PARENTHESIS);
            }
            break;
        default:
            throw new Error();
        }
        return transformString.toString();
    }
}
