package org.apache.log4j;
import java.util.Hashtable;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.ThreadLocalMap;
public class MDC {
  final static MDC mdc = new MDC();
  static final int HT_SIZE = 7;
  boolean java1;
  Object tlm;
  private
  MDC() {
    java1 = Loader.isJava1();
    if(!java1) {
      tlm = new ThreadLocalMap();
    }
  }
  static
  public
  void put(String key, Object o) {
     if (mdc != null) {
         mdc.put0(key, o);
     }
  }
  static 
  public
  Object get(String key) {
    if (mdc != null) {
        return mdc.get0(key);
    }
    return null;
  }
  static 
  public
  void remove(String key) {
    if (mdc != null) {
        mdc.remove0(key);
    }
  }
  public static Hashtable getContext() {
    if (mdc != null) {
        return mdc.getContext0();
    } else {
        return null;
    }
  }
  public static void clear() {
    if (mdc != null) {
        mdc.clear0();
    }
  }
  private
  void put0(String key, Object o) {
    if(java1 || tlm == null) {
      return;
    } else {
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht == null) {
        ht = new Hashtable(HT_SIZE);
        ((ThreadLocalMap)tlm).set(ht);
      }    
      ht.put(key, o);
    }
  }
  private
  Object get0(String key) {
    if(java1 || tlm == null) {
      return null;
    } else {       
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht != null && key != null) {
        return ht.get(key);
      } else {
        return null;
      }
    }
  }
  private
  void remove0(String key) {
    if(!java1 && tlm != null) {
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht != null) {
        ht.remove(key);
      } 
    }
  }
  private
  Hashtable getContext0() {
     if(java1 || tlm == null) {
      return null;
    } else {       
      return (Hashtable) ((ThreadLocalMap)tlm).get();
    }
  }
  private
  void clear0() {
    if(!java1 && tlm != null) {
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht != null) {
        ht.clear();
      } 
    }
  }
}
