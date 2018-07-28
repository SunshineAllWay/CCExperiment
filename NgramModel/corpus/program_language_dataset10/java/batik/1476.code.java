package org.apache.batik.bridge;
import org.apache.batik.test.*;
public class JarLoadTest extends DefaultTestSuite {
    public JarLoadTest() {
        String scripts = "application/java-archive";
        String[] scriptSource = {"jarCheckLoadAny",
                                 "jarCheckLoadSameAsDocument"};
        boolean[] secure = {true, false};
        String[] scriptOrigin = {"any", "document", "embeded"};
        for (int i=0; i<scriptSource.length; i++) {
            for (int j=0; j<=i; j++) {
                for (int k=0; k<secure.length; k++) {
                    ScriptSelfTest t= buildTest(scripts, scriptSource[i],
                                                scriptOrigin[j],
                                                secure[k]);
                    addTest(t);
                }
            }
        }
    }
    ScriptSelfTest buildTest(String scripts, String id, String origin, boolean secure) {
        ScriptSelfTest t = new ScriptSelfTest();
        String desc = 
            "(scripts=" + scripts + 
            ")(scriptOrigin=" + origin +
            ")(secure=" + secure + ")";
        t.setId(id + desc);
        t.setScriptOrigin(origin);
        t.setSecure(secure);
        t.setScripts(scripts);
        return t;
    }
}
