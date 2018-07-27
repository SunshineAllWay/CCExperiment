package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.apache.batik.util.SVGConstants;
public class SVGGraphicContext implements SVGConstants, ErrorConstants {
    private static final String[] leafOnlyAttributes = {
        SVG_OPACITY_ATTRIBUTE,
        SVG_FILTER_ATTRIBUTE,
        SVG_CLIP_PATH_ATTRIBUTE
    };
    private static final String[] defaultValues = {
        "1",
        SVG_NONE_VALUE,
        SVG_NONE_VALUE
    };
    private Map context;
    private Map groupContext;
    private Map graphicElementContext;
    private TransformStackElement[] transformStack;
    public SVGGraphicContext(Map context,
                             TransformStackElement[] transformStack) {
        if (context == null)
            throw new SVGGraphics2DRuntimeException(ERR_MAP_NULL);
        if (transformStack == null)
            throw new SVGGraphics2DRuntimeException(ERR_TRANS_NULL);
        this.context = context;
        this.transformStack = transformStack;
        computeGroupAndGraphicElementContext();
    }
    public SVGGraphicContext(Map groupContext, Map graphicElementContext,
                             TransformStackElement[] transformStack) {
        if (groupContext == null || graphicElementContext == null)
            throw new SVGGraphics2DRuntimeException(ERR_MAP_NULL);
        if (transformStack == null)
            throw new SVGGraphics2DRuntimeException(ERR_TRANS_NULL);
        this.groupContext = groupContext;
        this.graphicElementContext = graphicElementContext;
        this.transformStack = transformStack;
        computeContext();
    }
    public Map getContext() {
        return context;
    }
    public Map getGroupContext() {
        return groupContext;
    }
    public Map getGraphicElementContext() {
        return graphicElementContext;
    }
    public TransformStackElement[] getTransformStack() {
        return transformStack;
    }
    private void computeContext() {
        if (context != null)
            return;
        context = new HashMap(groupContext);
        context.putAll(graphicElementContext);
    }
    private void computeGroupAndGraphicElementContext() {
        if (groupContext != null)
            return;
        groupContext = new HashMap(context);
        graphicElementContext = new HashMap();
        for (int i=0; i< leafOnlyAttributes.length; i++) {
            Object attrValue = groupContext.get(leafOnlyAttributes[i]);
            if (attrValue != null){
                if (!attrValue.equals(defaultValues[i]))
                    graphicElementContext.put(leafOnlyAttributes[i], attrValue);
                groupContext.remove(leafOnlyAttributes[i]);
            }
        }
    }
}
