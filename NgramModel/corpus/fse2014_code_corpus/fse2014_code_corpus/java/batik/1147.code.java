package org.apache.batik.svggen;
import org.apache.batik.util.SVGConstants;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.w3c.dom.Element;
public class DefaultStyleHandler implements StyleHandler, SVGConstants {
    static Map ignoreAttributes = new HashMap();
    static {
        Set textAttributes = new HashSet( );
        textAttributes.add(SVG_FONT_SIZE_ATTRIBUTE);
        textAttributes.add(SVG_FONT_FAMILY_ATTRIBUTE);
        textAttributes.add(SVG_FONT_STYLE_ATTRIBUTE);
        textAttributes.add(SVG_FONT_WEIGHT_ATTRIBUTE);
        ignoreAttributes.put(SVG_RECT_TAG, textAttributes);
        ignoreAttributes.put(SVG_CIRCLE_TAG, textAttributes);
        ignoreAttributes.put(SVG_ELLIPSE_TAG, textAttributes);
        ignoreAttributes.put(SVG_POLYGON_TAG, textAttributes);
        ignoreAttributes.put(SVG_POLYGON_TAG, textAttributes);
        ignoreAttributes.put(SVG_LINE_TAG, textAttributes);
        ignoreAttributes.put(SVG_PATH_TAG, textAttributes);
    }
    public void setStyle(Element element, Map styleMap,
                         SVGGeneratorContext generatorContext) {
        String tagName = element.getTagName();
        Iterator iter = styleMap.keySet().iterator();
        while (iter.hasNext()) {
            String styleName = (String)iter.next();
            if (element.getAttributeNS(null, styleName).length() == 0){
                if (appliesTo(styleName, tagName)) {
                    element.setAttributeNS(null, styleName,
                                           (String)styleMap.get(styleName));
                }
            }
        }
    }
    protected boolean appliesTo(String styleName, String tagName) {
        Set s = (Set)ignoreAttributes.get(tagName);
        if (s == null) {
            return true;
        } else {
            return !s.contains(styleName);
        }
    }
}
