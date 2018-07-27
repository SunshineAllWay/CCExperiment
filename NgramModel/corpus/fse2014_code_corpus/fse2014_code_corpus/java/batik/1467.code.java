package org.apache.batik.bridge;
import org.w3c.dom.*;
import org.apache.batik.script.ScriptHandler;
import org.apache.batik.script.Window;
public class IWasLoaded implements ScriptHandler {
    public void run(final Document document, final Window win){
        Element result = document.getElementById("testResult");
        result.setAttributeNS(null, "result", "failed");
        result.setAttributeNS(null, "errorCode", "IWasLoaded.jar should not have been loaded");
    }
}
