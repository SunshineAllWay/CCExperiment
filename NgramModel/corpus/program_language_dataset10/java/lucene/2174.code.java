package org.apache.solr.common.params;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.StrUtils;
public abstract class SolrParams implements Serializable {
  public abstract String get(String param);
  public abstract String[] getParams(String param);
  public abstract Iterator<String> getParameterNamesIterator();
  public String get(String param, String def) {
    String val = get(param);
    return val==null ? def : val;
  }
  public RequiredSolrParams required()
  {
    return new RequiredSolrParams(this);
  }
  protected String fpname(String field, String param) {
    return "f."+field+'.'+param;
  }
  public String getFieldParam(String field, String param) {
    String val = get(fpname(field,param));
    return val!=null ? val : get(param);
  }
  public String getFieldParam(String field, String param, String def) {
    String val = get(fpname(field,param));
    return val!=null ? val : get(param, def);
  }
  public String[] getFieldParams(String field, String param) {
    String[] val = getParams(fpname(field,param));
    return val!=null ? val : getParams(param);
  }
  public Boolean getBool(String param) {
    String val = get(param);
    return val==null ? null : StrUtils.parseBool(val);
  }
  public boolean getBool(String param, boolean def) {
    String val = get(param);
    return val==null ? def : StrUtils.parseBool(val);
  }
  public Boolean getFieldBool(String field, String param) {
    String val = getFieldParam(field, param);
    return val==null ? null : StrUtils.parseBool(val);
  }
  public boolean getFieldBool(String field, String param, boolean def) {
    String val = getFieldParam(field, param);
    return val==null ? def : StrUtils.parseBool(val);
  }
  public Integer getInt(String param) {
    String val = get(param);
    try {
      return val==null ? null : Integer.valueOf(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public int getInt(String param, int def) {
    String val = get(param);
    try {
      return val==null ? def : Integer.parseInt(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public Integer getFieldInt(String field, String param) {
    String val = getFieldParam(field, param);
    try {
      return val==null ? null : Integer.valueOf(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public int getFieldInt(String field, String param, int def) {
    String val = getFieldParam(field, param);
    try {
      return val==null ? def : Integer.parseInt(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public Float getFloat(String param) {
    String val = get(param);
    try {
      return val==null ? null : Float.valueOf(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public float getFloat(String param, float def) {
    String val = get(param);
    try {
      return val==null ? def : Float.parseFloat(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public Double getDouble(String param) {
    String val = get(param);
    try {
      return val==null ? null : Double.valueOf(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public double getDouble(String param, double def) {
    String val = get(param);
    try {
      return val==null ? def : Double.parseDouble(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public Float getFieldFloat(String field, String param) {
    String val = getFieldParam(field, param);
    try {
      return val==null ? null : Float.valueOf(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public float getFieldFloat(String field, String param, float def) {
    String val = getFieldParam(field, param);
    try {
      return val==null ? def : Float.parseFloat(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public Double getFieldDouble(String field, String param) {
    String val = getFieldParam(field, param);
    try {
      return val==null ? null : Double.valueOf(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  public double getFieldDouble(String field, String param, double def) {
    String val = getFieldParam(field, param);
    try {
      return val==null ? def : Double.parseDouble(val);
    }
    catch( Exception ex ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, ex.getMessage(), ex );
    }
  }
  @Deprecated
  protected boolean parseBool(String s) {
    return StrUtils.parseBool(s);
  }
  public static Map<String,String> toMap(NamedList params) {
    HashMap<String,String> map = new HashMap<String,String>();
    for (int i=0; i<params.size(); i++) {
      map.put(params.getName(i), params.getVal(i).toString());
    }
    return map;
  }
  public static Map<String,String[]> toMultiMap(NamedList params) {
    HashMap<String,String[]> map = new HashMap<String,String[]>();
    for (int i=0; i<params.size(); i++) {
      String name = params.getName(i);
      String val = params.getVal(i).toString();
      MultiMapSolrParams.addParam(name,val,map);
    }
    return map;
  }
  public static SolrParams toSolrParams(NamedList params) {
    HashMap<String,String> map = new HashMap<String,String>();
    for (int i=0; i<params.size(); i++) {
      String prev = map.put(params.getName(i), params.getVal(i).toString());
      if (prev!=null) return new MultiMapSolrParams(toMultiMap(params));
    }
    return new MapSolrParams(map);
  }
  public NamedList<Object> toNamedList() {
    final SimpleOrderedMap<Object> result = new SimpleOrderedMap<Object>();
    for(Iterator<String> it=getParameterNamesIterator(); it.hasNext(); ) {
      final String name = it.next();
      final String [] values = getParams(name);
      if(values.length==1) {
        result.add(name,values[0]);
      } else {
        result.add(name,values);
      }
    }
    return result;
  }
}
