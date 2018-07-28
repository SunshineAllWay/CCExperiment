package org.apache.batik.svggen;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.util.Arrays;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class SVGLookupOp extends AbstractSVGFilterConverter {
    private static final double GAMMA = 1.0/2.4;
    private static final int[] linearToSRGBLut = new int[256];
    private static final int[] sRGBToLinear = new int[256];
    static {
        for(int i=0; i<256; i++) {
            float value = i/255f;
            if (value <= 0.0031308) {
                value *= 12.92f;
            } else {
                value = 1.055f * ((float) Math.pow(value, GAMMA)) - 0.055f;
            }
            linearToSRGBLut[i] = Math.round(value*255);
            value = i/255f;
            if(value <= 0.04045){
                value /= 12.92f;
            } else {
                value = (float)Math.pow((value + 0.055f)/1.055f, 1/GAMMA);
            }
            sRGBToLinear[i] = Math.round(value*255);
        }
    }
    public SVGLookupOp(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGFilterDescriptor toSVG(BufferedImageOp filter,
                                     Rectangle filterRect) {
        if (filter instanceof LookupOp)
            return toSVG((LookupOp)filter);
        else
            return null;
    }
    public SVGFilterDescriptor toSVG(LookupOp lookupOp) {
        SVGFilterDescriptor filterDesc =
            (SVGFilterDescriptor)descMap.get(lookupOp);
        Document domFactory = generatorContext.domFactory;
        if (filterDesc == null) {
            Element filterDef = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                           SVG_FILTER_TAG);
            Element feComponentTransferDef =
                domFactory.createElementNS(SVG_NAMESPACE_URI,
                                           SVG_FE_COMPONENT_TRANSFER_TAG);
            String[] lookupTables = convertLookupTables(lookupOp);
            Element feFuncR = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_R_TAG);
            Element feFuncG = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_G_TAG);
            Element feFuncB = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_B_TAG);
            Element feFuncA = null;
            String type = SVG_TABLE_VALUE;
            if(lookupTables.length == 1){
                feFuncR.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncG.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncB.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncR.setAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE,
                                       lookupTables[0]);
                feFuncG.setAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE,
                                       lookupTables[0]);
                feFuncB.setAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE,
                                       lookupTables[0]);
            }
            else if(lookupTables.length >= 3){
                feFuncR.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncG.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncB.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncR.setAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE,
                                       lookupTables[0]);
                feFuncG.setAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE,
                                       lookupTables[1]);
                feFuncB.setAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE,
                                       lookupTables[2]);
                if(lookupTables.length == 4){
                    feFuncA = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_A_TAG);
                    feFuncA.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                    feFuncA.setAttributeNS(null, SVG_TABLE_VALUES_ATTRIBUTE,
                                           lookupTables[3]);
                }
            }
            feComponentTransferDef.appendChild(feFuncR);
            feComponentTransferDef.appendChild(feFuncG);
            feComponentTransferDef.appendChild(feFuncB);
            if(feFuncA != null)
                feComponentTransferDef.appendChild(feFuncA);
            filterDef.appendChild(feComponentTransferDef);
            filterDef.
                setAttributeNS(null, SVG_ID_ATTRIBUTE,
                               generatorContext.idGenerator.
                               generateID(ID_PREFIX_FE_COMPONENT_TRANSFER));
            String filterAttrBuf = URL_PREFIX + SIGN_POUND + filterDef.getAttributeNS(null, SVG_ID_ATTRIBUTE) + URL_SUFFIX;
            filterDesc = new SVGFilterDescriptor(filterAttrBuf, filterDef);
            defSet.add(filterDef);
            descMap.put(lookupOp, filterDesc);
        }
        return filterDesc;
    }
    private String[] convertLookupTables(LookupOp lookupOp){
        LookupTable lookupTable = lookupOp.getTable();
        int nComponents = lookupTable.getNumComponents();
        if((nComponents != 1) && (nComponents != 3) && (nComponents != 4))
            throw new SVGGraphics2DRuntimeException(ERR_ILLEGAL_BUFFERED_IMAGE_LOOKUP_OP);
        StringBuffer[] lookupTableBuf = new StringBuffer[nComponents];
        for(int i=0; i<nComponents; i++)
            lookupTableBuf[i] = new StringBuffer();
        if(!(lookupTable instanceof ByteLookupTable)){
            int[] src = new int[nComponents];
            int[] dest= new int[nComponents];
            int offset = lookupTable.getOffset();
            for(int i=0; i<offset; i++){
                for(int j=0; j<nComponents; j++){
                    lookupTableBuf[j].append(doubleString(i/255.0)).append(SPACE);
                }
            }
            for(int i=offset; i<=255; i++){
                Arrays.fill( src, i );
                lookupTable.lookupPixel(src, dest);
                for(int j=0; j<nComponents; j++){
                    lookupTableBuf[j].append(doubleString( dest[j]/255.0) ).append(SPACE);
                }
            }
        }
        else{
            byte[] src = new byte[nComponents];
            byte[] dest = new byte[nComponents];
            int offset = lookupTable.getOffset();
            for(int i=0; i<offset; i++){
                for(int j=0; j<nComponents; j++){
                    lookupTableBuf[j].append( doubleString(i/255.0) ).append(SPACE);
                }
            }
            for(int i=0; i<=255; i++){
                Arrays.fill( src, (byte)(0xff & i) );
                ((ByteLookupTable)lookupTable).lookupPixel(src, dest);
                for(int j=0; j<nComponents; j++){
                    lookupTableBuf[j].append( doubleString( (0xff & dest[j])/255.0) ).append(SPACE);
                }
            }
        }
        String[] lookupTables = new String[nComponents];
        for(int i=0; i<nComponents; i++)
            lookupTables[i] = lookupTableBuf[i].toString().trim();
        return lookupTables;
    }
}
