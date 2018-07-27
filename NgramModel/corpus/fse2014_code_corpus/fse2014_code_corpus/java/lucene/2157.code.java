package org.apache.solr.common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
public class SolrInputField implements Iterable<Object>, Serializable
{
  String name;
  Object value = null; 
  float boost = 1.0f;
  public SolrInputField( String n )
  {
    this.name = n;
  }
  public void setValue(Object v, float b) {
    boost = b;
    if( v instanceof Object[] ) {
      Object[] arr = (Object[])v;
      Collection<Object> c = new ArrayList<Object>( arr.length );
      for( Object o : arr ) {
        c.add( o );
      }
      value = c;
    }
    else {
      value = v;
    }
  }
  @SuppressWarnings("unchecked")
  public void addValue(Object v, float b) {
    if( value == null ) {
      setValue(v, b);
      return;
    }
    boost *= b;
    Collection<Object> vals = null;
    if( value instanceof Collection ) {
      vals = (Collection<Object>)value;
    }
    else {
      vals = new ArrayList<Object>( 3 );
      vals.add( value );
      value = vals;
    }
    if( v instanceof Iterable ) {
      for( Object o : (Iterable<Object>)v ) {
        vals.add( o );
      }
    }
    else if( v instanceof Object[] ) {
      for( Object o : (Object[])v ) {
        vals.add( o );
      }
    }
    else {
      vals.add( v );
    }
  }
  @SuppressWarnings("unchecked")
  public Object getFirstValue() {
    if( value instanceof Collection ) {
      Collection c = (Collection<Object>)value;
      if( c.size() > 0 ) {
        return c.iterator().next();
      }
      return null;
    }
    return value;
  }
  public Object getValue() {
    return value;
  }
  @SuppressWarnings("unchecked")
  public Collection<Object> getValues() {
    if( value instanceof Collection ) {
      return (Collection<Object>)value;
    }
    if( value != null ) {
      Collection<Object> vals = new ArrayList<Object>(1);
      vals.add( value );
      return vals;
    }
    return null;
  }
  public int getValueCount() {
    if( value instanceof Collection ) {
      return ((Collection)value).size();
    }
    return (value == null) ? 0 : 1;
  }
  public float getBoost() {
    return boost;
  }
  public void setBoost(float boost) {
    this.boost = boost;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  @SuppressWarnings("unchecked")
  public Iterator<Object> iterator() {
    if( value instanceof Collection ) {
      return ((Collection)value).iterator();
    }
    return new Iterator<Object>() {
      boolean nxt = (value!=null);
      public boolean hasNext() {
        return nxt;
      }
      public Object next() {
        nxt = false;
        return value;
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  @Override
  public String toString()
  {
    return name + "("+boost+")={" + value + "}";
  }
}
