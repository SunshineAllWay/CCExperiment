package org.apache.batik.bridge;
import org.apache.batik.test.*;
public class EcmaLoadTest extends DefaultTestSuite {
    public EcmaLoadTest() {
        String scripts = "text/ecmascript";
        String[] scriptSource = {"ecmaCheckLoadAny",
                                 "ecmaCheckLoadSameAsDocument",
                                 "ecmaCheckLoadEmbed",
                                 "ecmaCheckLoadEmbedAttr",
        };
        boolean[] secure = {true, false};
        String[][] scriptOrigin = {{"any"},
                                   {"any", "document"},
                                   {"any", "document", "embeded"},
                                   {"any", "document", "embeded"},
                                   };
        for (int i=0; i<scriptSource.length; i++) {
            for (int j=0; j<scriptOrigin[i].length; j++) {
                for (int k=0; k<secure.length; k++) {
                    ScriptSelfTest t= buildTest(scripts, scriptSource[i],
                                                scriptOrigin[i][j],
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
            ")(secure=" + secure + ')';
        t.setId(id + desc);
        t.setScriptOrigin(origin);
        t.setSecure(secure);
        t.setScripts(scripts);
        return t;
    }
}
