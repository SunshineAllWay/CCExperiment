package org.apache.solr.handler.dataimport;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
public class ScriptTransformer extends Transformer {
  private Object engine;
  private Method invokeFunctionMethod;
  private String functionName;
  public Object transformRow(Map<String, Object> row, Context context) {
    try {
      if (engine == null)
        initEngine(context);
      if (engine == null)
        return row;
      return invokeFunctionMethod.invoke(engine, functionName, new Object[]{
              row, context});
    } catch (DataImportHandlerException e) {
      throw e;
    } catch (InvocationTargetException e) {
      wrapAndThrow(SEVERE,e,
              "Could not invoke method :"
                      + functionName
                      + "\n <script>\n"
                      + context.getScript()
                      + "</script>");
    } catch (Exception e) {
      wrapAndThrow(SEVERE,e, "Error invoking script for entity " + context.getEntityAttribute("name"));
    }
    return null;
  }
  private void initEngine(Context context) {
    try {
      String scriptText = context.getScript();
      String scriptLang = context.getScriptLanguage();
      if(scriptText == null ){
        throw new DataImportHandlerException(SEVERE,
              "<script> tag is not present under <dataConfig>");
      }
      Object scriptEngineMgr = Class
              .forName("javax.script.ScriptEngineManager").newInstance();
      Method getEngineMethod = scriptEngineMgr.getClass().getMethod(
              "getEngineByName", String.class);
      engine = getEngineMethod.invoke(scriptEngineMgr, scriptLang);
      Method evalMethod = engine.getClass().getMethod("eval", String.class);
      invokeFunctionMethod = engine.getClass().getMethod("invokeFunction",
              String.class, Object[].class);
      evalMethod.invoke(engine, scriptText);
    } catch (Exception e) {
      wrapAndThrow(SEVERE,e, "<script> can be used only in java 6 or above");
    }
  }
  public void setFunctionName(String methodName) {
    this.functionName = methodName;
  }
  public String getFunctionName() {
    return functionName;
  }
}
