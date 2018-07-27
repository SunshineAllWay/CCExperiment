package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
public class SVGCompositeDescriptor implements SVGDescriptor, SVGSyntax{
    private Element def;
    private String opacityValue;
    private String filterValue;
    public SVGCompositeDescriptor(String opacityValue,
                                  String filterValue){
        this.opacityValue = opacityValue;
        this.filterValue = filterValue;
    }
    public SVGCompositeDescriptor(String opacityValue,
                                  String filterValue,
                                  Element def){
        this(opacityValue, filterValue);
        this.def = def;
    }
    public String getOpacityValue(){
        return opacityValue;
    }
    public String getFilterValue(){
        return filterValue;
    }
    public Element getDef(){
        return def;
    }
    public Map getAttributeMap(Map attrMap) {
        if(attrMap == null)
            attrMap = new HashMap();
        attrMap.put(SVG_OPACITY_ATTRIBUTE, opacityValue);
        attrMap.put(SVG_FILTER_ATTRIBUTE, filterValue);
        return attrMap;
    }
    public List getDefinitionSet(List defSet) {
        if (defSet == null)
            defSet = new LinkedList();
        if (def != null)
            defSet.add(def);
        return defSet;
    }
}
