package org.apache.solr.client.solrj.response;
import org.apache.solr.common.luke.FieldFlag;
import org.apache.solr.common.util.NamedList;
import java.io.Serializable;
import java.util.*;
public class LukeResponse extends SolrResponseBase {
  public static class FieldTypeInfo implements Serializable {
    String name;
    String className;
    boolean tokenized;
    String analyzer;
    List<String> fields;
    public FieldTypeInfo(String name) {
      this.name = name;
      fields = Collections.emptyList();
    }
    public String getAnalyzer() {
      return analyzer;
    }
    public String getClassName() {
      return className;
    }
    public List<String> getFields() {
      return fields;
    }
    public String getName() {
      return name;
    }
    public boolean isTokenized() {
      return tokenized;
    }
    @SuppressWarnings("unchecked")
    public void read(NamedList<Object> nl) {
      for (Map.Entry<String, Object> entry : nl) {
        String key = entry.getKey();
        if ("fields".equals(key) && entry.getValue() != null) {
          List<String> theFields = (List<String>) entry.getValue();
          fields = new ArrayList<String>(theFields);
        } else if ("tokenized".equals(key) == true) {
          tokenized = Boolean.parseBoolean(entry.getValue().toString());
        } else if ("analyzer".equals(key) == true) {
          analyzer = entry.getValue().toString();
        } else if ("className".equals(key) == true) {
          className = entry.getValue().toString();
        }
      }
    }
  }
  public static class FieldInfo implements Serializable {
    String name;
    String type;
    String schema;
    int docs;
    int distinct;
    EnumSet<FieldFlag> flags;
    boolean cacheableFaceting;
    NamedList<Integer> topTerms;
    public FieldInfo(String n) {
      name = n;
    }
    @SuppressWarnings("unchecked")
    public void read(NamedList<Object> nl) {
      for (Map.Entry<String, Object> entry : nl) {
        if ("type".equals(entry.getKey())) {
          type = (String) entry.getValue();
        }
        if ("flags".equals(entry.getKey())) {
          flags = parseFlags((String) entry.getValue());
        } else if ("schema".equals(entry.getKey())) {
          schema = (String) entry.getValue();
        } else if ("docs".equals(entry.getKey())) {
          docs = (Integer) entry.getValue();
        } else if ("distinct".equals(entry.getKey())) {
          distinct = (Integer) entry.getValue();
        } else if ("cacheableFaceting".equals(entry.getKey())) {
          cacheableFaceting = (Boolean) entry.getValue();
        } else if ("topTerms".equals(entry.getKey())) {
          topTerms = (NamedList<Integer>) entry.getValue();
        }
      }
    }
    public static EnumSet<FieldFlag> parseFlags(String flagStr) {
      EnumSet<FieldFlag> result = EnumSet.noneOf(FieldFlag.class);
      char[] chars = flagStr.toCharArray();
      for (int i = 0; i < chars.length; i++) {
        if (chars[i] != '-') {
          FieldFlag flag = FieldFlag.getFlag(chars[i]);
          result.add(flag);
        }
      }
      return result;
    }
    public EnumSet<FieldFlag> getFlags() {
      return flags;
    }
    public boolean isCacheableFaceting() {
      return cacheableFaceting;
    }
    public String getType() {
      return type;
    }
    public int getDistinct() {
      return distinct;
    }
    public int getDocs() {
      return docs;
    }
    public String getName() {
      return name;
    }
    public String getSchema() {
      return schema;
    }
    public NamedList<Integer> getTopTerms() {
      return topTerms;
    }
  }
  private NamedList<Object> indexInfo;
  private Map<String, FieldInfo> fieldInfo;
  private Map<String, FieldTypeInfo> fieldTypeInfo;
  @Override
  @SuppressWarnings("unchecked")
  public void setResponse(NamedList<Object> res) {
    super.setResponse(res);
    indexInfo = (NamedList<Object>) res.get("index");
    NamedList<Object> schema = (NamedList<Object>) res.get("schema");
    NamedList<Object> flds = (NamedList<Object>) res.get("fields");
    if (flds == null && schema != null ) {
      flds = (NamedList<Object>) schema.get("fields");
    }
    if (flds != null) {
      fieldInfo = new HashMap<String, FieldInfo>();
      for (Map.Entry<String, Object> field : flds) {
        FieldInfo f = new FieldInfo(field.getKey());
        f.read((NamedList<Object>) field.getValue());
        fieldInfo.put(field.getKey(), f);
      }
    }
    if( schema != null ) {
      NamedList<Object> fldTypes = (NamedList<Object>) schema.get("types");
      if (fldTypes != null) {
        fieldTypeInfo = new HashMap<String, FieldTypeInfo>();
        for (Map.Entry<String, Object> fieldType : fldTypes) {
          FieldTypeInfo ft = new FieldTypeInfo(fieldType.getKey());
          ft.read((NamedList<Object>) fieldType.getValue());
          fieldTypeInfo.put(fieldType.getKey(), ft);
        }
      }
    }
  }
  public String getIndexDirectory() {
    if (indexInfo == null) return null;
    return (String) indexInfo.get("directory");
  }
  public Integer getNumDocs() {
    if (indexInfo == null) return null;
    return (Integer) indexInfo.get("numDocs");
  }
  public Integer getMaxDoc() {
    if (indexInfo == null) return null;
    return (Integer) indexInfo.get("maxDoc");
  }
  public Integer getNumTerms() {
    if (indexInfo == null) return null;
    return (Integer) indexInfo.get("numTerms");
  }
  public Map<String, FieldTypeInfo> getFieldTypeInfo() {
    return fieldTypeInfo;
  }
  public FieldTypeInfo getFieldTypeInfo(String name) {
    return fieldTypeInfo.get(name);
  }
  public NamedList<Object> getIndexInfo() {
    return indexInfo;
  }
  public Map<String, FieldInfo> getFieldInfo() {
    return fieldInfo;
  }
  public FieldInfo getFieldInfo(String f) {
    return fieldInfo.get(f);
  }
}
