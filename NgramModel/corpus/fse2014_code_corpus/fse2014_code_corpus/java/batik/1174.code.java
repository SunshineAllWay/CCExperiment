package org.apache.batik.svggen;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class SVGConvolveOp extends AbstractSVGFilterConverter {
    public SVGConvolveOp(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGFilterDescriptor toSVG(BufferedImageOp filter,
                                     Rectangle filterRect){
        if(filter instanceof ConvolveOp)
            return toSVG((ConvolveOp)filter);
        else
            return null;
    }
    public SVGFilterDescriptor toSVG(ConvolveOp convolveOp){
        SVGFilterDescriptor filterDesc =
            (SVGFilterDescriptor)descMap.get(convolveOp);
        Document domFactory = generatorContext.domFactory;
        if (filterDesc == null) {
            Kernel kernel = convolveOp.getKernel();
            Element filterDef =
                domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_FILTER_TAG);
            Element feConvolveMatrixDef =
                domFactory.createElementNS(SVG_NAMESPACE_URI,
                                           SVG_FE_CONVOLVE_MATRIX_TAG);
            feConvolveMatrixDef.setAttributeNS(null, SVG_ORDER_ATTRIBUTE,
                                             kernel.getWidth() + SPACE +
                                             kernel.getHeight());
            float[] data = kernel.getKernelData(null);
            StringBuffer kernelMatrixBuf = new StringBuffer( data.length * 8 );
            for(int i=0; i<data.length; i++){
                kernelMatrixBuf.append(doubleString(data[i]));
                kernelMatrixBuf.append(SPACE);
            }
            feConvolveMatrixDef.
                setAttributeNS(null, SVG_KERNEL_MATRIX_ATTRIBUTE,
                               kernelMatrixBuf.toString().trim());
            filterDef.appendChild(feConvolveMatrixDef);
            filterDef.setAttributeNS(null, SVG_ID_ATTRIBUTE,
                                     generatorContext.idGenerator.
                                     generateID(ID_PREFIX_FE_CONVOLVE_MATRIX));
            if(convolveOp.getEdgeCondition() == ConvolveOp.EDGE_NO_OP)
                feConvolveMatrixDef.setAttributeNS(null, SVG_EDGE_MODE_ATTRIBUTE,
                                                 SVG_DUPLICATE_VALUE);
            else
                feConvolveMatrixDef.setAttributeNS(null, SVG_EDGE_MODE_ATTRIBUTE,
                                                 SVG_NONE_VALUE);
            StringBuffer filterAttrBuf = new StringBuffer(URL_PREFIX);
            filterAttrBuf.append(SIGN_POUND);
            filterAttrBuf.append(filterDef.getAttributeNS(null, SVG_ID_ATTRIBUTE));
            filterAttrBuf.append(URL_SUFFIX);
            filterDesc = new SVGFilterDescriptor(filterAttrBuf.toString(),
                                                 filterDef);
            defSet.add(filterDef);
            descMap.put(convolveOp, filterDesc);
        }
        return filterDesc;
    }
}
