package org.apache.solr.common;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
public class SolrInputDocument implements Map<String,SolrInputField>, Iterable<SolrInputField>, Serializable
{
  private final Map<String,SolrInputField> _fields;
  private float _documentBoost = 1.0f;
  public SolrInputDocument() {
    _fields = new LinkedHashMap<String,SolrInputField>();
  }
  public void clear()
  {
    if( _fields != null ) {
      _fields.clear();
    }
  }
  public void addField(String name, Object value) 
  {
    addField(name, value, 1.0f );
  }
  public Object getFieldValue(String name) 
  {
    SolrInputField field = getField(name);
    Object o = null;
    if (field!=null) o = field.getFirstValue();
    return o;
  }
  public Collection<Object> getFieldValues(String name) 
  {
    SolrInputField field = getField(name);
    if (field!=null) {
      return field.getValues();
    }
    return null;
  } 
  public Collection<String> getFieldNames() 
  {
    return _fields.keySet();
  }
  public void setField(String name, Object value) 
  {
    setField(name, value, 1.0f );
  }
  public void setField(String name, Object value, float boost ) 
  {
    SolrInputField field = new SolrInputField( name );
    _fields.put( name, field );
    field.setValue( value, boost );
  }
  public void addField(String name, Object value, float boost ) 
  {
    SolrInputField field = _fields.get( name );
    if( field == null || field.value == null ) {
      setField(name, value, boost);
    }
    else {
      field.addValue( value, boost );
    }
  }
  public SolrInputField removeField(String name) {
    return _fields.remove( name );
  }
  public SolrInputField getField( String field )
  {
    return _fields.get( field );
  }
  public Iterator<SolrInputField> iterator() {
    return _fields.values().iterator();
  }
  public float getDocumentBoost() {
    return _documentBoost;
  }
  public void setDocumentBoost(float documentBoost) {
    _documentBoost = documentBoost;
  }
  @Override
  public String toString()
  {
    return "SolrInputDocument["+_fields+"]";
  }
  public boolean containsKey(Object key) {
    return _fields.containsKey(key);
  }
  public boolean containsValue(Object value) {
    return _fields.containsValue(value);
  }
  public Set<Entry<String, SolrInputField>> entrySet() {
    return _fields.entrySet();
  }
  public SolrInputField get(Object key) {
    return _fields.get(key);
  }
  public boolean isEmpty() {
    return _fields.isEmpty();
  }
  public Set<String> keySet() {
    return _fields.keySet();
  }
  public SolrInputField put(String key, SolrInputField value) {
    return _fields.put(key, value);
  }
  public void putAll(Map<? extends String, ? extends SolrInputField> t) {
    _fields.putAll( t );
  }
  public SolrInputField remove(Object key) {
    return _fields.remove(key);
  }
  public int size() {
    return _fields.size();
  }
  public Collection<SolrInputField> values() {
    return _fields.values();
  }
}
