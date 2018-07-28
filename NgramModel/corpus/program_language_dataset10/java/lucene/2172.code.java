package org.apache.solr.common.params;
import org.apache.solr.common.SolrException;
import java.util.Iterator;
public class RequiredSolrParams extends SolrParams {
  protected final SolrParams params;
  public RequiredSolrParams(SolrParams params) {
    this.params = params;
  }
  @Override
  public String get(String param) {
    String val = params.get(param);
    if( val == null )  {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Missing required parameter: "+param );
    }
    return val;
  }
  @Override
  public String getFieldParam(final String field, final String param) {
    final String fpname = fpname(field,param);
    String val = params.get(fpname);
    if (null == val) {
      val = params.get(param);
      if (null == val)  {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                                 "Missing required parameter: "+fpname+
                                 " (or default: "+param+")" );
      }
    }
    return val;
  }
  @Override
  public String[] getFieldParams(final String field, final String param) {
    final String fpname = fpname(field,param);
    String[] val = params.getParams(fpname);
    if (null == val) {
      val = params.getParams(param);
      if (null == val)  {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
                                 "Missing required parameter: "+fpname+
                                 " (or default: "+param+")" );
      }
    }
    return val;
  }
  @Override
  public String[] getParams(String param) {
    String[] vals = params.getParams(param);
    if( vals == null || vals.length == 0 ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Missing required parameter: "+param );
    }
    return vals;
  }
  @Override
  public Iterator<String> getParameterNamesIterator() {
    return params.getParameterNamesIterator();
  }
  @Override
  public String toString() {
    return "{required("+params+")}";  
  }    
  @Override
  public String get(String param, String def) {
    return params.get(param, def);
  }
  @Override
  public int getInt(String param, int def) {
    return params.getInt(param, def);
  }
  @Override
  public float getFloat(String param, float def) {
    return params.getFloat(param, def);
  }
  @Override
  public boolean getBool(String param, boolean def) {
    return params.getBool(param, def);
  }
  @Override
  public int getFieldInt(String field, String param, int def) {
    return params.getFieldInt(field, param, def);
  }
  @Override
  public boolean getFieldBool(String field, String param, boolean def) {
    return params.getFieldBool(field, param, def);
  }
  @Override
  public float getFieldFloat(String field, String param, float def) {
    return params.getFieldFloat(field, param, def);
  }
  @Override
  public String getFieldParam(String field, String param, String def) {
    return params.getFieldParam(field, param, def);
  }
}
