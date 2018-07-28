package org.apache.batik.svggen;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
public class DOMTreeManager implements SVGSyntax, ErrorConstants {
    int maxGCOverrides;
    protected final List groupManagers = Collections.synchronizedList( new ArrayList() );
    protected List genericDefSet = new LinkedList();
    SVGGraphicContext defaultGC;
    protected Element topLevelGroup;
    SVGGraphicContextConverter gcConverter;
    protected SVGGeneratorContext generatorContext;
    protected SVGBufferedImageOp filterConverter;
    protected List otherDefs;
    public DOMTreeManager(GraphicContext gc,
                          SVGGeneratorContext generatorContext,
                          int maxGCOverrides){
        if (gc == null)
            throw new SVGGraphics2DRuntimeException(ERR_GC_NULL);
        if (maxGCOverrides <= 0)
            throw new SVGGraphics2DRuntimeException(ERR_MAXGCOVERRIDES_OUTOFRANGE);
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ERR_CONTEXT_NULL);
        this.generatorContext = generatorContext;
        this.maxGCOverrides = maxGCOverrides;
        recycleTopLevelGroup();
        defaultGC = gcConverter.toSVG(gc);
    }
    public void addGroupManager(DOMGroupManager groupManager){
        if(groupManager != null)
            groupManagers.add(groupManager);
    }
    public void removeGroupManager(DOMGroupManager groupManager){
        if(groupManager != null)
            groupManagers.remove( groupManager );
    }
    public void appendGroup(Element group, DOMGroupManager groupManager){
        topLevelGroup.appendChild(group);
        synchronized( groupManagers ){
            int nManagers = groupManagers.size();
            for(int i=0; i<nManagers; i++){
                DOMGroupManager gm = (DOMGroupManager)groupManagers.get(i);
                if( gm != groupManager )
                    gm.recycleCurrentGroup();
            }
        }
    }
    protected void recycleTopLevelGroup(){
        recycleTopLevelGroup(true);
    }
    protected void recycleTopLevelGroup(boolean recycleConverters){
        synchronized( groupManagers ){
            int nManagers = groupManagers.size();
            for(int i=0; i<nManagers; i++){
                DOMGroupManager gm = (DOMGroupManager)groupManagers.get(i);
                gm.recycleCurrentGroup();
            }
        }
        topLevelGroup = generatorContext.domFactory.
            createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
        if (recycleConverters) {
            filterConverter =
                new SVGBufferedImageOp(generatorContext);
            gcConverter =
                new SVGGraphicContextConverter(generatorContext);
        }
    }
    public void setTopLevelGroup(Element topLevelGroup){
        if(topLevelGroup == null)
            throw new SVGGraphics2DRuntimeException(ERR_TOP_LEVEL_GROUP_NULL);
        if(!SVG_G_TAG.equalsIgnoreCase(topLevelGroup.getTagName()))
            throw new SVGGraphics2DRuntimeException(ERR_TOP_LEVEL_GROUP_NOT_G);
        recycleTopLevelGroup(false);
        this.topLevelGroup = topLevelGroup;
    }
    public Element getRoot(){
        return getRoot(null);
    }
    public Element getRoot(Element svgElement){
        Element svg = svgElement;
        if (svg == null) {
            svg = generatorContext.domFactory.
                createElementNS(SVG_NAMESPACE_URI, SVG_SVG_TAG);
        }
        if (gcConverter.getCompositeConverter().
            getAlphaCompositeConverter().requiresBackgroundAccess())
            svg.setAttributeNS
                (null, SVG_ENABLE_BACKGROUND_ATTRIBUTE, SVG_NEW_VALUE);
        if (generatorContext.generatorComment != null) {
            Comment generatorComment = generatorContext.domFactory.
                createComment(generatorContext.generatorComment);
            svg.appendChild(generatorComment);
        }
        applyDefaultRenderingStyle(svg);
        svg.appendChild(getGenericDefinitions());
        svg.appendChild(getTopLevelGroup());
        return svg;
    }
    public void applyDefaultRenderingStyle(Element element) {
        Map groupDefaults = defaultGC.getGroupContext();
        generatorContext.styleHandler.setStyle(element, groupDefaults, generatorContext);
    }
    public Element getGenericDefinitions() {
        Element genericDefs =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_DEFS_TAG);
        Iterator iter = genericDefSet.iterator();
        while (iter.hasNext()) {
            genericDefs.appendChild((Element)iter.next());
        }
        genericDefs.setAttributeNS(null, SVG_ID_ATTRIBUTE, ID_PREFIX_GENERIC_DEFS);
        return genericDefs;
    }
    public ExtensionHandler getExtensionHandler(){
        return generatorContext.getExtensionHandler();
    }
    void setExtensionHandler(ExtensionHandler extensionHandler) {
        generatorContext.setExtensionHandler(extensionHandler);
    }
    public List getDefinitionSet(){
        List defSet = gcConverter.getDefinitionSet();
        defSet.removeAll(genericDefSet);
        defSet.addAll(filterConverter.getDefinitionSet());
        if (otherDefs != null){
            defSet.addAll(otherDefs);
            otherDefs = null;
        }
        filterConverter = new SVGBufferedImageOp(generatorContext);
        gcConverter = new SVGGraphicContextConverter(generatorContext);
        return defSet;
    }
    public void addOtherDef(Element definition){
        if (otherDefs == null){
            otherDefs = new LinkedList();
        }
        otherDefs.add(definition);
    }
    public Element getTopLevelGroup(){
        boolean includeDefinitionSet = true;
        return getTopLevelGroup(includeDefinitionSet);
    }
    public Element getTopLevelGroup(boolean includeDefinitionSet){
        Element topLevelGroup = this.topLevelGroup;
        if(includeDefinitionSet){
            List defSet = getDefinitionSet();
            if(defSet.size() > 0){
                Element defElement = null;
                NodeList defsElements =
                    topLevelGroup.getElementsByTagName(SVG_DEFS_TAG);
                if (defsElements.getLength() > 0)
                    defElement = (Element)defsElements.item(0);
                if (defElement == null) {
                    defElement =
                        generatorContext.domFactory.
                        createElementNS(SVG_NAMESPACE_URI,
                                        SVG_DEFS_TAG);
                    defElement.
                        setAttributeNS(null, SVG_ID_ATTRIBUTE,
                                       generatorContext.idGenerator.
                                       generateID(ID_PREFIX_DEFS));
                    topLevelGroup.insertBefore(defElement,
                                               topLevelGroup.getFirstChild());
                }
                Iterator iter = defSet.iterator();
                while(iter.hasNext())
                    defElement.appendChild((Element)iter.next());
            }
        }
        recycleTopLevelGroup(false);
        return topLevelGroup;
    }
    public SVGBufferedImageOp getFilterConverter() {
        return filterConverter;
    }
    public SVGGraphicContextConverter getGraphicContextConverter() {
        return gcConverter;
    }
    SVGGeneratorContext getGeneratorContext() {
        return generatorContext;
    }
    Document getDOMFactory() {
        return generatorContext.domFactory;
    }
    StyleHandler getStyleHandler() {
        return generatorContext.styleHandler;
    }
}
