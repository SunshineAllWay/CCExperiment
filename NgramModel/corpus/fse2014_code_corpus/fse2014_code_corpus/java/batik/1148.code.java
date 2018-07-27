package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.w3c.dom.Element;
public class DOMGroupManager implements SVGSyntax, ErrorConstants {
    public static final short DRAW = 0x01;
    public static final short FILL = 0x10;
    protected GraphicContext gc;
    protected DOMTreeManager domTreeManager;
    protected SVGGraphicContext groupGC;
    protected Element currentGroup;
    public DOMGroupManager(GraphicContext gc, DOMTreeManager domTreeManager) {
        if (gc == null)
            throw new SVGGraphics2DRuntimeException(ERR_GC_NULL);
        if (domTreeManager == null)
            throw new SVGGraphics2DRuntimeException(ERR_DOMTREEMANAGER_NULL);
        this.gc = gc;
        this.domTreeManager = domTreeManager;
        recycleCurrentGroup();
        groupGC = domTreeManager.gcConverter.toSVG(gc);
    }
    void recycleCurrentGroup() {
        currentGroup = domTreeManager.getDOMFactory().
            createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
    }
    public void addElement(Element element) {
        addElement(element, (short)(DRAW|FILL));
    }
    public void addElement(Element element, short method) {
        if (!currentGroup.hasChildNodes()) {
            currentGroup.appendChild(element);
            groupGC = domTreeManager.gcConverter.toSVG(gc);
            SVGGraphicContext deltaGC;
            deltaGC = processDeltaGC(groupGC,
                                     domTreeManager.defaultGC);
            domTreeManager.getStyleHandler().
                setStyle(currentGroup, deltaGC.getGroupContext(),
                         domTreeManager.getGeneratorContext());
            if ((method & DRAW) == 0) {
                deltaGC.getGraphicElementContext().put(SVG_STROKE_ATTRIBUTE,
                                                       SVG_NONE_VALUE);
            }
            if ((method & FILL) == 0) {
                deltaGC.getGraphicElementContext().put(SVG_FILL_ATTRIBUTE,
                                                       SVG_NONE_VALUE);
            }
            domTreeManager.getStyleHandler().
                setStyle(element, deltaGC.getGraphicElementContext(),
                         domTreeManager.getGeneratorContext());
            setTransform(currentGroup, deltaGC.getTransformStack());
            domTreeManager.appendGroup(currentGroup, this);
        } else {
            if(gc.isTransformStackValid()) {
                SVGGraphicContext elementGC =
                    domTreeManager.gcConverter.toSVG(gc);
                SVGGraphicContext deltaGC = processDeltaGC(elementGC, groupGC);
                trimContextForElement(deltaGC, element);
                if (countOverrides(deltaGC) <= domTreeManager.maxGCOverrides) {
                    currentGroup.appendChild(element);
                    if ((method & DRAW) == 0) {
                        deltaGC.getContext().
                            put(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
                    }
                    if ((method & FILL) == 0) {
                        deltaGC.getContext().
                            put(SVG_FILL_ATTRIBUTE, SVG_NONE_VALUE);
                    }
                    domTreeManager.getStyleHandler().
                        setStyle(element, deltaGC.getContext(),
                                 domTreeManager.getGeneratorContext());
                    setTransform(element, deltaGC.getTransformStack());
                } else {
                    currentGroup =
                        domTreeManager.getDOMFactory().
                        createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
                    addElement(element, method);
                }
            } else {
                currentGroup =
                    domTreeManager.getDOMFactory().
                    createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
                gc.validateTransformStack();
                addElement(element, method);
            }
        }
    }
    protected int countOverrides(SVGGraphicContext deltaGC) {
        return deltaGC.getGroupContext().size();
    }
    protected void trimContextForElement(SVGGraphicContext svgGC, Element element) {
        String tag = element.getTagName();
        Map groupAttrMap = svgGC.getGroupContext();
        if (tag != null) {
            Iterator iter = groupAttrMap.keySet().iterator();
            while(iter.hasNext()){
                String attrName = (String)iter.next();
                SVGAttribute attr = SVGAttributeMap.get(attrName);
                if(attr != null && !attr.appliesTo(tag))
                    groupAttrMap.remove(attrName);
            }
        }
    }
    protected void setTransform(Element element,
                              TransformStackElement[] transformStack) {
        String transform = domTreeManager.gcConverter.
            toSVG(transformStack).trim();
        if (transform.length() > 0)
            element.setAttributeNS(null, SVG_TRANSFORM_ATTRIBUTE, transform);
    }
    static SVGGraphicContext processDeltaGC(SVGGraphicContext gc,
                                            SVGGraphicContext referenceGc) {
        Map groupDelta = processDeltaMap(gc.getGroupContext(),
                                         referenceGc.getGroupContext());
        Map graphicElementDelta = gc.getGraphicElementContext();
        TransformStackElement[] gcTransformStack = gc.getTransformStack();
        TransformStackElement[] referenceStack = referenceGc.getTransformStack();
        int deltaStackLength = gcTransformStack.length - referenceStack.length;
        TransformStackElement[] deltaTransformStack =
            new TransformStackElement[deltaStackLength];
        System.arraycopy(gcTransformStack, referenceStack.length,
                         deltaTransformStack, 0, deltaStackLength);
        SVGGraphicContext deltaGC = new SVGGraphicContext(groupDelta,
                                                          graphicElementDelta,
                                                          deltaTransformStack);
        return deltaGC;
    }
    static Map processDeltaMap(Map map, Map referenceMap) {
        Map mapDelta = new HashMap();
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()){
            String key = (String)iter.next();
            String value = (String)map.get(key);
            String refValue = (String)referenceMap.get(key);
            if (!value.equals(refValue)) {
                mapDelta.put(key, value);
            }
        }
        return mapDelta;
    }
}
