package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
public class SVGTransformDescriptor implements SVGDescriptor, SVGSyntax{
    private String transform;
    public SVGTransformDescriptor(String transform){
        this.transform = transform;
    }
    public Map getAttributeMap(Map attrMap){
        if(attrMap == null)
            attrMap = new HashMap();
        attrMap.put(SVG_TRANSFORM_ATTRIBUTE, transform);
        return attrMap;
    }
    public List getDefinitionSet(List defSet) {
        if (defSet == null)
            defSet = new LinkedList();
        return defSet;
    }
}
