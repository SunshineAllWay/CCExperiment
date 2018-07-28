package org.apache.batik.dom;
import org.apache.batik.test.svg.SelfContainedSVGOnLoadTest;
public class EcmaScriptDOMTest extends SelfContainedSVGOnLoadTest {
    public void setId(String id){
        super.setId(id);
        svgURL = resolveURL("test-resources/org/apache/batik/dom/" + id + ".svg");
    }
}
