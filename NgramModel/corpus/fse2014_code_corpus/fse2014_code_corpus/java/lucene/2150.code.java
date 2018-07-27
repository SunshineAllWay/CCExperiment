package org.apache.solr.response;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.Properties;
public class VelocityResponseWriter implements QueryResponseWriter {
  private static final Logger log = LoggerFactory.getLogger(VelocityResponseWriter.class);
  public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException {
    VelocityEngine engine = getEngine(request);  
    Template template = getTemplate(engine, request);
    VelocityContext context = new VelocityContext();
    context.put("request", request);
    SolrResponse rsp = new QueryResponse();
    NamedList<Object> parsedResponse = new EmbeddedSolrServer(request.getCore()).getParsedResponse(request, response);
    try {
      rsp.setResponse(parsedResponse);
      context.put("page", new PageTool(request, response));  
    } catch (ClassCastException e) {
      e.printStackTrace();
      rsp = new SolrResponseBase();
      rsp.setResponse(parsedResponse);
    }
    context.put("response", rsp);
    context.put("esc", new EscapeTool());
    context.put("date", new ComparisonDateTool());
    context.put("list", new ListTool());
    context.put("math", new MathTool());
    context.put("number", new NumberTool());
    context.put("sort", new SortTool());
    context.put("engine", engine);  
    String layout_template = request.getParams().get("v.layout");
    String json_wrapper = request.getParams().get("v.json");
    boolean wrap_response = (layout_template != null) || (json_wrapper != null);
    if (wrap_response) {
      StringWriter stringWriter = new StringWriter();
      template.merge(context, stringWriter);
      if (layout_template != null) {
        context.put("content", stringWriter.toString());
        stringWriter = new StringWriter();
        try {
          engine.getTemplate(layout_template + ".vm").merge(context, stringWriter);
        } catch (Exception e) {
          throw new IOException(e.getMessage());
        }
      }
      if (json_wrapper != null) {
        writer.write(request.getParams().get("v.json") + "(");
        writer.write(getJSONWrap(stringWriter.toString()));
        writer.write(')');
      } else {  
        writer.write(stringWriter.toString());
      }
    } else {
      template.merge(context, writer);
    }
  }
  private VelocityEngine getEngine(SolrQueryRequest request) {
    VelocityEngine engine = new VelocityEngine();
    String template_root = request.getParams().get("v.base_dir");
    File baseDir = new File(request.getCore().getResourceLoader().getConfigDir(), "velocity");
    if (template_root != null) {
      baseDir = new File(template_root);
    }
    engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, baseDir.getAbsolutePath());
    engine.setProperty("params.resource.loader.instance", new SolrParamResourceLoader(request));
    SolrVelocityResourceLoader resourceLoader =
        new SolrVelocityResourceLoader(request.getCore().getSolrConfig().getResourceLoader());
    engine.setProperty("solr.resource.loader.instance", resourceLoader);
    engine.setProperty(VelocityEngine.RESOURCE_LOADER, "params,file,solr");
    String propFile = request.getParams().get("v.properties");
    try {
      if (propFile == null)
        engine.init();
      else {
        InputStream is = null;
        try {
          is = resourceLoader.getResourceStream(propFile);
          Properties props = new Properties();
          props.load(is);
          engine.init(props);
        }
        finally {
          if (is != null) is.close();
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return engine;
  }
  private Template getTemplate(VelocityEngine engine, SolrQueryRequest request) throws IOException {
    Template template;
    String template_name = request.getParams().get("v.template");
    String qt = request.getParams().get("qt");
    String path = (String) request.getContext().get("path");
    if (template_name == null && path != null) {
      template_name = path;
    }  
    if (template_name == null && qt != null) {
      template_name = qt;
    }
    if (template_name == null) template_name = "index";
    try {
      template = engine.getTemplate(template_name + ".vm");
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
    return template;
  }
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    return request.getParams().get("v.contentType", "text/html");
  }
  private String getJSONWrap(String xmlResult) {  
    String replace1 = xmlResult.replaceAll("\\\\", "\\\\\\\\");
    replace1 = replace1.replaceAll("\\n", "\\\\n");
    replace1 = replace1.replaceAll("\\r", "\\\\r");
    String replaced = replace1.replaceAll("\"", "\\\\\"");
    return "{\"result\":\"" + replaced + "\"}";
  }
  public void init(NamedList args) {
  }
}
