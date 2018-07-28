package org.apache.batik.dom.svg;
import org.apache.batik.test.svg.SelfContainedSVGOnLoadTest;
public class EcmaScriptSVGDOMTest extends SelfContainedSVGOnLoadTest {
    public void setId(String id){
        super.setId(id);
        svgURL = resolveURL("test-resources/org/apache/batik/dom/svg/" + id + ".svg");
    }
}
