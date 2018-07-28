package org.apache.batik.svggen;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class SVGCSSStyler implements SVGSyntax{
    private static final char CSS_PROPERTY_VALUE_SEPARATOR = ':';
    private static final char CSS_RULE_SEPARATOR = ';';
    private static final char SPACE = ' ';
    public static void style(Node node){
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null){
            Element element = (Element)node;
            StringBuffer styleAttrBuffer = new StringBuffer();
            int nAttr = attributes.getLength();
            List toBeRemoved = new ArrayList();
            for(int i=0; i<nAttr; i++){
                Attr attr = (Attr)attributes.item(i);
                String attrName = attr.getName();
                if(SVGStylingAttributes.set.contains( attrName )){
                    styleAttrBuffer.append( attrName );
                    styleAttrBuffer.append(CSS_PROPERTY_VALUE_SEPARATOR);
                    styleAttrBuffer.append(attr.getValue());
                    styleAttrBuffer.append(CSS_RULE_SEPARATOR);
                    styleAttrBuffer.append(SPACE);
                    toBeRemoved.add( attrName );
                }
            }
            if(styleAttrBuffer.length() > 0){
                element.setAttributeNS(null,
                                       SVG_STYLE_ATTRIBUTE,
                                       styleAttrBuffer.toString().trim());
                int n = toBeRemoved.size();
                for(int i=0; i<n; i++) {
                    element.removeAttribute((String)toBeRemoved.get( i ));
                }
            }
        }
        NodeList children = node.getChildNodes();
        int nChildren = children.getLength();
        for(int i=0; i<nChildren; i++){
            Node child = children.item(i);
            style(child);
        }
    }
}
