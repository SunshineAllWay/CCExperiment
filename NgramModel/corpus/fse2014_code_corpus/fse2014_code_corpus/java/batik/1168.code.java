package org.apache.batik.svggen;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Element;
public class SVGClip extends AbstractSVGConverter {
    public static final Shape ORIGIN = new Line2D.Float(0,0,0,0);
    public static final SVGClipDescriptor NO_CLIP =
        new SVGClipDescriptor(SVG_NONE_VALUE, null);
    private SVGShape shapeConverter;
    public SVGClip(SVGGeneratorContext generatorContext) {
        super(generatorContext);
        this.shapeConverter = new SVGShape(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        Shape clip = gc.getClip();
        SVGClipDescriptor clipDesc = null;
        if (clip != null) {
            StringBuffer clipPathAttrBuf = new StringBuffer(URL_PREFIX);
            GeneralPath clipPath = new GeneralPath(clip);
            ClipKey clipKey = new ClipKey(clipPath, generatorContext);
            clipDesc = (SVGClipDescriptor)descMap.get(clipKey);
            if (clipDesc == null) {
                Element clipDef = clipToSVG(clip);
                if (clipDef == null)
                    clipDesc = NO_CLIP;
                else {
                    clipPathAttrBuf.append(SIGN_POUND);
                    clipPathAttrBuf.append(clipDef.getAttributeNS(null, SVG_ID_ATTRIBUTE));
                    clipPathAttrBuf.append(URL_SUFFIX);
                    clipDesc = new SVGClipDescriptor(clipPathAttrBuf.toString(),
                                                     clipDef);
                    descMap.put(clipKey, clipDesc);
                    defSet.add(clipDef);
                }
            }
        } else
            clipDesc = NO_CLIP;
        return clipDesc;
    }
    private Element clipToSVG(Shape clip) {
        Element clipDef =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_CLIP_PATH_TAG);
        clipDef.setAttributeNS(null, SVG_CLIP_PATH_UNITS_ATTRIBUTE,
                               SVG_USER_SPACE_ON_USE_VALUE);
        clipDef.setAttributeNS(null, SVG_ID_ATTRIBUTE,
                               generatorContext.
                               idGenerator.generateID(ID_PREFIX_CLIP_PATH));
        Element clipPath = shapeConverter.toSVG(clip);
        if (clipPath != null) {
            clipDef.appendChild(clipPath);
            return clipDef;
        } else {
            clipDef.appendChild(shapeConverter.toSVG(ORIGIN));
            return clipDef;
        }
    }
}
class ClipKey {
    int hashCodeValue = 0;
    public ClipKey(GeneralPath proxiedPath, SVGGeneratorContext gc){
        String pathData = SVGPath.toSVGPathData(proxiedPath, gc);
        hashCodeValue = pathData.hashCode();
    }
    public int hashCode() {
        return hashCodeValue;
    }
    public boolean equals(Object clipKey) {
        return clipKey instanceof ClipKey
            && hashCodeValue == ((ClipKey) clipKey).hashCodeValue;
    }
}
