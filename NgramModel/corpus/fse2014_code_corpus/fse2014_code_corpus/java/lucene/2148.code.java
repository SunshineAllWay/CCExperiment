package org.apache.solr.response;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.collections.ExtendedProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class SolrParamResourceLoader extends ResourceLoader {
  private Map<String,String> templates = new HashMap<String,String>();
  public SolrParamResourceLoader(SolrQueryRequest request) {
    super();
    org.apache.solr.common.params.SolrParams params = request.getParams();
    Iterator<String> names = params.getParameterNamesIterator();
    while (names.hasNext()) {
      String name = names.next();
      if (name.startsWith("v.template.")) {
        templates.put(name.substring(11) + ".vm",params.get(name));
      }
    }
  }
  public void init(ExtendedProperties extendedProperties) {
  }
  public InputStream getResourceStream(String s) throws ResourceNotFoundException {
    String template = templates.get(s);
    return template == null ? null : new ByteArrayInputStream(template.getBytes());
  }
  public boolean isSourceModified(Resource resource) {
    return false;
  }
  public long getLastModified(Resource resource) {
    return 0;
  }
}
