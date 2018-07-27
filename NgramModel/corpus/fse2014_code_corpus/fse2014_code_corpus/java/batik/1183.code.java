package org.apache.batik.svggen;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
public class SVGFont extends AbstractSVGConverter {
    public static final float EXTRA_LIGHT =
        TextAttribute.WEIGHT_EXTRA_LIGHT.floatValue();
    public static final float LIGHT =
        TextAttribute.WEIGHT_LIGHT.floatValue();
    public static final float DEMILIGHT =
        TextAttribute.WEIGHT_DEMILIGHT.floatValue();
    public static final float REGULAR =
        TextAttribute.WEIGHT_REGULAR.floatValue();
    public static final float SEMIBOLD =
        TextAttribute.WEIGHT_SEMIBOLD.floatValue();
    public static final float MEDIUM =
        TextAttribute.WEIGHT_MEDIUM.floatValue();
    public static final float DEMIBOLD =
        TextAttribute.WEIGHT_DEMIBOLD.floatValue();
    public static final float BOLD =
        TextAttribute.WEIGHT_BOLD.floatValue();
    public static final float HEAVY =
        TextAttribute.WEIGHT_HEAVY.floatValue();
    public static final float EXTRABOLD =
        TextAttribute.WEIGHT_EXTRABOLD.floatValue();
    public static final float ULTRABOLD =
        TextAttribute.WEIGHT_ULTRABOLD.floatValue();
    public static final float POSTURE_REGULAR =
        TextAttribute.POSTURE_REGULAR.floatValue();
    public static final float POSTURE_OBLIQUE =
        TextAttribute.POSTURE_OBLIQUE.floatValue();
    static final float[] fontStyles = {
        POSTURE_REGULAR + (POSTURE_OBLIQUE - POSTURE_REGULAR)/2
    };
    static final String[] svgStyles = {
           SVG_NORMAL_VALUE,
           SVG_ITALIC_VALUE
    };
    static final float[] fontWeights = { EXTRA_LIGHT + (LIGHT - EXTRA_LIGHT)/2f,
                                         LIGHT + (DEMILIGHT - LIGHT)/2f,
                                         DEMILIGHT + (REGULAR - DEMILIGHT)/2f,
                                         REGULAR + (SEMIBOLD - REGULAR)/2f,
                                         SEMIBOLD + (MEDIUM - SEMIBOLD)/2f,
                                         MEDIUM + (DEMIBOLD - MEDIUM)/2f,
                                         DEMIBOLD + (BOLD - DEMIBOLD)/2f,
                                         BOLD + (HEAVY - BOLD)/2f,
                                         HEAVY + (EXTRABOLD - HEAVY)/2f,
                                         EXTRABOLD + (ULTRABOLD - EXTRABOLD),
    };
    static final String[] svgWeights = {
         SVG_100_VALUE,
               SVG_200_VALUE,
           SVG_300_VALUE,
             SVG_NORMAL_VALUE,
            SVG_500_VALUE,
              SVG_500_VALUE,
            SVG_600_VALUE,
                SVG_BOLD_VALUE,
               SVG_800_VALUE,
           SVG_800_VALUE,
           SVG_900_VALUE
    };
    static Map logicalFontMap = new HashMap();
    static {
        logicalFontMap.put("dialog", "sans-serif");
        logicalFontMap.put("dialoginput", "monospace");
        logicalFontMap.put("monospaced", "monospace");
        logicalFontMap.put("serif", "serif");
        logicalFontMap.put("sansserif", "sans-serif");
        logicalFontMap.put("symbol", "'WingDings'");
    }
    static final int COMMON_FONT_SIZE = 100;
    final Map fontStringMap = new HashMap();
    public SVGFont(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public void recordFontUsage(String string, Font font) {
        Font   commonSizeFont = createCommonSizeFont(font);
        String fontKey        = (commonSizeFont.getFamily() +
                                 commonSizeFont.getStyle());
        CharListHelper chl = (CharListHelper) fontStringMap.get( fontKey );
        if ( chl == null ){
            chl = new CharListHelper();
        }
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);    
            chl.add( ch );
        }
        fontStringMap.put(fontKey, chl );
    }
    private static Font createCommonSizeFont(Font font) {
        Map attributes = new HashMap();
        attributes.put(TextAttribute.SIZE, new Float(COMMON_FONT_SIZE));
        attributes.put(TextAttribute.TRANSFORM, null);
        return font.deriveFont(attributes);
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        return toSVG(gc.getFont(), gc.getFontRenderContext());
    }
    public SVGFontDescriptor toSVG(Font font, FontRenderContext frc) {
        FontRenderContext localFRC;
        localFRC = new FontRenderContext(new AffineTransform(),
                                         frc.isAntiAliased(),
                                         frc.usesFractionalMetrics());
        String fontSize = doubleString(font.getSize2D()) + "px";
        String fontWeight = weightToSVG(font);
        String fontStyle = styleToSVG(font);
        String fontFamilyStr = familyToSVG(font);
        Font commonSizeFont = createCommonSizeFont(font);
        String fontKey = (commonSizeFont.getFamily() +
                          commonSizeFont.getStyle());
        CharListHelper clh = (CharListHelper)fontStringMap.get(fontKey);
        if (clh == null) {
            return new SVGFontDescriptor(fontSize, fontWeight,
                                         fontStyle, fontFamilyStr,
                                         null);
        }
        Document domFactory = generatorContext.domFactory;
        SVGFontDescriptor fontDesc =
            (SVGFontDescriptor)descMap.get(fontKey);
        Element fontDef;
        if (fontDesc != null) {
            fontDef = fontDesc.getDef();
        } else {
            fontDef = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                 SVG_FONT_TAG);
            Element fontFace = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                          SVG_FONT_FACE_TAG);
            String svgFontFamilyString = fontFamilyStr;
            if (fontFamilyStr.startsWith("'") &&
                fontFamilyStr.endsWith("'")) {
                svgFontFamilyString
                    = fontFamilyStr.substring(1, fontFamilyStr.length()-1);
            }
            fontFace.setAttributeNS(null, SVG_FONT_FAMILY_ATTRIBUTE,
                                    svgFontFamilyString);
            fontFace.setAttributeNS(null, SVG_FONT_WEIGHT_ATTRIBUTE,
                                    fontWeight);
            fontFace.setAttributeNS(null, SVG_FONT_STYLE_ATTRIBUTE,
                                    fontStyle);
            fontFace.setAttributeNS(null, SVG_UNITS_PER_EM_ATTRIBUTE,
                                    ""+COMMON_FONT_SIZE);
            fontDef.appendChild(fontFace);
            Element missingGlyphElement
                = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                             SVG_MISSING_GLYPH_TAG);
            int[] missingGlyphCode = new int[1];
            missingGlyphCode[0] = commonSizeFont.getMissingGlyphCode();
            GlyphVector gv;
            gv = commonSizeFont.createGlyphVector(localFRC, missingGlyphCode);
            Shape missingGlyphShape = gv.getGlyphOutline(0);
            GlyphMetrics gm = gv.getGlyphMetrics(0);
            AffineTransform at = AffineTransform.getScaleInstance(1, -1);
            missingGlyphShape = at.createTransformedShape(missingGlyphShape);
            missingGlyphElement.setAttributeNS(null, SVG_D_ATTRIBUTE,
                                    SVGPath.toSVGPathData(missingGlyphShape, generatorContext));
            missingGlyphElement.setAttributeNS(null, SVG_HORIZ_ADV_X_ATTRIBUTE, String.valueOf( gm.getAdvance() ) );
            fontDef.appendChild(missingGlyphElement);
            fontDef.setAttributeNS(null, SVG_HORIZ_ADV_X_ATTRIBUTE, String.valueOf( gm.getAdvance() ) );
            LineMetrics lm = commonSizeFont.getLineMetrics("By", localFRC);
            fontFace.setAttributeNS(null, SVG_ASCENT_ATTRIBUTE,  String.valueOf( lm.getAscent() ) );
            fontFace.setAttributeNS(null, SVG_DESCENT_ATTRIBUTE, String.valueOf( lm.getDescent() ) );
            fontDef.setAttributeNS(null, SVG_ID_ATTRIBUTE,  generatorContext.idGenerator.generateID(ID_PREFIX_FONT));
        }
        String textUsingFont = clh.getNewChars();
        clh.clearNewChars();
        for (int i = textUsingFont.length()-1; i >= 0; i--) {
            char c = textUsingFont.charAt(i);
            String searchStr = String.valueOf( c );
            boolean foundGlyph = false;
            NodeList fontChildren = fontDef.getChildNodes();
            for (int j = 0; j < fontChildren.getLength(); j++) {
                if (fontChildren.item(j) instanceof Element) {
                    Element childElement = (Element)fontChildren.item(j);
                    if (childElement.getAttributeNS(null, SVG_UNICODE_ATTRIBUTE).equals( searchStr )) {
                        foundGlyph = true;
                        break;
                    }
                }
            }
            if (!foundGlyph) {
                Element glyphElement
                    = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                 SVG_GLYPH_TAG);
                GlyphVector gv;
                gv = commonSizeFont.createGlyphVector(localFRC, ""+c);
                Shape glyphShape = gv.getGlyphOutline(0);
                GlyphMetrics gm = gv.getGlyphMetrics(0);
                AffineTransform at = AffineTransform.getScaleInstance(1, -1);
                glyphShape = at.createTransformedShape(glyphShape);
                glyphElement.setAttributeNS(null, SVG_D_ATTRIBUTE,
                                            SVGPath.toSVGPathData(glyphShape, generatorContext));
                glyphElement.setAttributeNS(null, SVG_HORIZ_ADV_X_ATTRIBUTE, String.valueOf( gm.getAdvance() ) );
                glyphElement.setAttributeNS(null, SVG_UNICODE_ATTRIBUTE,     String.valueOf( c ) );
                fontDef.appendChild(glyphElement);
            } else {
                break;
            }
        }
        SVGFontDescriptor newFontDesc
            = new SVGFontDescriptor(fontSize, fontWeight,
                                    fontStyle, fontFamilyStr,
                                    fontDef);
        if (fontDesc == null) {
            descMap.put(fontKey, newFontDesc);
            defSet.add(fontDef);
        }
        return newFontDesc;
    }
    public static String familyToSVG(Font font) {
        String fontFamilyStr = font.getFamily();
        String logicalFontFamily =
            (String)logicalFontMap.get(font.getName().toLowerCase());
        if (logicalFontFamily != null)
            fontFamilyStr = logicalFontFamily;
        else {
            final char QUOTE = '\'';
            fontFamilyStr = QUOTE + fontFamilyStr + QUOTE;
        }
        return fontFamilyStr;
    }
    public static String styleToSVG(Font font) {
        Map attrMap = font.getAttributes();
        Float styleValue = (Float)attrMap.get(TextAttribute.POSTURE);
        if (styleValue == null) {
            if (font.isItalic())
                styleValue = TextAttribute.POSTURE_OBLIQUE;
            else
                styleValue = TextAttribute.POSTURE_REGULAR;
        }
        float style = styleValue.floatValue();
        int i = 0;
        for (i=0; i< fontStyles.length; i++) {
            if (style <= fontStyles[i])
                break;
        }
        return svgStyles[i];
    }
    public static String weightToSVG(Font font) {
        Map attrMap = font.getAttributes();
        Float weightValue = (Float)attrMap.get(TextAttribute.WEIGHT);
        if (weightValue==null) {
            if (font.isBold())
                weightValue = TextAttribute.WEIGHT_BOLD;
            else
                weightValue = TextAttribute.WEIGHT_REGULAR;
        }
        float weight = weightValue.floatValue();
        int i = 0;
        for (i=0; i<fontWeights.length; i++) {
            if (weight<=fontWeights[i])
                break;
        }
        return svgWeights[i];
    }
    private static class CharListHelper {
        private int nUsed = 0;
        private int[] charList = new int[ 40 ];
        private StringBuffer freshChars = new StringBuffer( 40 );
        CharListHelper() {
        }
        String getNewChars(){
            return freshChars.toString();
        }
        void clearNewChars(){
            freshChars = new StringBuffer( 40 );
        }
        boolean add( int c ){
            int pos = binSearch( charList, nUsed, c );
            if ( pos >= 0 ){
                return false;
            }
            if ( nUsed == charList.length ){
                int[] t = new int[ nUsed + 20 ];
                System.arraycopy( charList, 0, t, 0, nUsed );
                charList = t;
            }
            pos = -pos -1;
            System.arraycopy( charList, pos, charList, pos+1, nUsed - pos );
            charList[ pos ] = c;
            freshChars.append( (char)c );    
            nUsed++;
            return true;
        }
        static int binSearch( int[] list, int nUsed, int chr ){
            int low = 0;
            int high = nUsed -1;
            while ( low <= high ) {
                int mid = ( low + high ) >>> 1;  
                int midVal = list[ mid ];
                if ( midVal < chr ) {
                    low = mid + 1;
                } else if ( midVal > chr ) {
                    high = mid - 1;
                } else {
                    return mid; 
                }
            }
            return -( low + 1 );  
        }
    }
}
