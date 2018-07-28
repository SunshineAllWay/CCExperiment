package org.apache.batik.css.dom;
import org.apache.batik.test.svg.SelfContainedSVGOnLoadTest;
public class EcmaScriptCSSDOMTest extends SelfContainedSVGOnLoadTest {
    public void setId(String id){
        super.setId(id);
        svgURL = resolveURL("test-resources/org/apache/batik/css/dom/" + id + ".svg");
    }
}
