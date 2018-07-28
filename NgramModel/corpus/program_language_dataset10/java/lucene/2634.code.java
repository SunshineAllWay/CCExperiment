package org.apache.solr.client.solrj.beans;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.ByteBuffer;
public class DocumentObjectBinder {
  private final Map<Class, List<DocField>> infocache = new ConcurrentHashMap<Class, List<DocField>>();
  public DocumentObjectBinder() {
  }
  public <T> List<T> getBeans(Class<T> clazz, SolrDocumentList solrDocList) {
    List<DocField> fields = getDocFields( clazz );
    List<T> result = new ArrayList<T>(solrDocList.size());
    for(int j=0;j<solrDocList.size();j++) {
      SolrDocument sdoc = solrDocList.get(j);
	  result.add(getBean(clazz, fields, sdoc));
    }
    return result;
  }
  public <T> T getBean(Class<T> clazz, SolrDocument solrDoc) {
    return getBean(clazz, null,solrDoc);
  }
  private <T> T getBean(Class<T> clazz, List<DocField> fields, SolrDocument solrDoc) {
    if (fields == null) {
      fields = getDocFields(clazz);
    }
    T obj = null;
    try {
      obj = clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Could not instantiate object of " + clazz, e);
    }
    for (int i = 0; i < fields.size(); i++) {
      DocField docField = fields.get(i);
      docField.inject(obj, solrDoc);
    }
    return obj;
  }
  public SolrInputDocument toSolrInputDocument( Object obj )
  {
    List<DocField> fields = getDocFields( obj.getClass() );
    if( fields.isEmpty() ) {
      throw new RuntimeException( "class: "+obj.getClass()+" does not define any fields." );
    }
    SolrInputDocument doc = new SolrInputDocument();
	for (DocField field : fields) {
		if (field.dynamicFieldNamePatternMatcher != null
				&& field.get(obj) != null && field.isContainedInMap) {
			Map<String, Object> mapValue = (HashMap<String, Object>) field
					.get(obj);
			for (Map.Entry<String, Object> e : mapValue.entrySet()) {
				doc.setField( e.getKey(), e.getValue(), 1.0f);
			}
		} else {
			doc.setField(field.name, field.get(obj), 1.0f);
		}
	}
    return doc;
  }
  private List<DocField> getDocFields( Class clazz )
  {
    List<DocField> fields = infocache.get(clazz);
    if (fields == null) {
      synchronized(infocache) {
        infocache.put(clazz, fields = collectInfo(clazz));
      }
    }
    return fields;
  }
  private List<DocField> collectInfo(Class clazz) {
    List<DocField> fields = new ArrayList<DocField>();
    Class superClazz = clazz;
    ArrayList<AccessibleObject> members = new ArrayList<AccessibleObject>();
    while (superClazz != null && superClazz != Object.class) {
      members.addAll(Arrays.asList(superClazz.getDeclaredFields()));
      members.addAll(Arrays.asList(superClazz.getDeclaredMethods()));
      superClazz = superClazz.getSuperclass();
    }
    for (AccessibleObject member : members) {
      if (member.isAnnotationPresent(Field.class)) {
        member.setAccessible(true);
        fields.add(new DocField(member));
      }
    }
    return fields;
  }
  private static class DocField {
    private String name;
    private java.lang.reflect.Field field;
    private Method setter;
    private Method getter;
    private Class type;
    private boolean isArray = false, isList=false;
    boolean isContainedInMap =false;
    private Pattern dynamicFieldNamePatternMatcher;
    public DocField(AccessibleObject member) {
      if (member instanceof java.lang.reflect.Field) {
        field = (java.lang.reflect.Field) member;
      } else {
        setter = (Method) member;
      }
      Field annotation = member.getAnnotation(Field.class);
      storeName(annotation);
      storeType();
      if( setter != null ) {
        String gname = setter.getName();
        if( gname.startsWith("set") ) {
          gname = "get" + gname.substring(3);
          try {
            getter = setter.getDeclaringClass().getMethod( gname, (Class[])null );
          }
          catch( Exception ex ) {
            if( type == Boolean.class ) {
              gname = "is" + setter.getName().substring( 3 );
              try {
                getter = setter.getDeclaringClass().getMethod( gname, (Class[])null );
              }
              catch( Exception ex2 ) {
              }
            }
          }
        }
      }
    }
    private void storeName(Field annotation) {
      if (annotation.value().equals(Field.DEFAULT)) {
        if (field != null) {
          name = field.getName();
        } else {
          String setterName = setter.getName();
          if (setterName.startsWith("set") && setterName.length() > 3) {
            name = setterName.substring(3, 4).toLowerCase() + setterName.substring(4);
          } else {
            name = setter.getName();
          }
        }
      }
      else if(annotation.value().indexOf('*') >= 0){
        name = annotation.value().replaceFirst("\\*", "\\.*");
        dynamicFieldNamePatternMatcher = Pattern.compile("^"+name+"$");
      } else {
        name = annotation.value();
      }
    }
    private void storeType() {
      if (field != null) {
        type = field.getType();
      } else {
        Class[] params = setter.getParameterTypes();
        if (params.length != 1)
          throw new RuntimeException("Invalid setter method. Must have one and only one parameter");
        type = params[0];
      }
      if(type == Collection.class || type == List.class || type == ArrayList.class) {
        type = Object.class;
        isList = true;
      } else if(type == byte[].class){
      }else if (type.isArray()) {
        isArray = true;
        type = type.getComponentType();
      }
      else if (type == Map.class || type == HashMap.class) {
        isContainedInMap = true;
        type = Object.class;
        if(field != null){
          if(field.getGenericType() instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type[] types = parameterizedType.getActualTypeArguments();
            if(types != null && types.length == 2 && types[0] == String.class){
              if(types[1] instanceof Class){
                if(types[1]== Collection.class || types[1] == List.class || types[1] == ArrayList.class){
                  type = Object.class;
                  isList = true;
                }else{
                  type = (Class) types[1];
                }
              }
              else if(types[1] instanceof ParameterizedType){
                Type rawType = ((ParameterizedType)types[1]).getRawType();
                if(rawType== Collection.class || rawType == List.class || rawType == ArrayList.class){
                  type = Object.class;
                  isList = true;
                }
              }
              else if(types[1] instanceof GenericArrayType){
                type = (Class) ((GenericArrayType) types[1]).getGenericComponentType();
                isArray = true;
              }
              else{
                throw new RuntimeException("Allowed type for values of mapping a dynamicField are : " +
                    "Object, Object[] and List");
              }
            }
          }
        }
      }
    }
    @SuppressWarnings("unchecked")
    private Object getFieldValue(SolrDocument sdoc){
      Object fieldValue = sdoc.getFieldValue(name);
      if(fieldValue != null) {
        return fieldValue;
      }
      if(dynamicFieldNamePatternMatcher != null){
        Map<String, Object> allValuesMap = null;
        ArrayList allValuesList = null;
        if(isContainedInMap){
         allValuesMap = new HashMap<String, Object>();
        } else {
          allValuesList = new ArrayList();
        }
        for(String field : sdoc.getFieldNames()){
          if(dynamicFieldNamePatternMatcher.matcher(field).find()){
            Object val = sdoc.getFieldValue(field);
            if(val == null) continue;
            if(isContainedInMap){
              if(isList){
                if (!(val instanceof List)) {
                  ArrayList al = new ArrayList();
                  al.add(val);
                  val = al;
                }
              } else if(isArray){
                if (!(val instanceof List)) {
                  Object[] arr= (Object[]) Array.newInstance(type,1);
                  arr[0] = val;
                  val= arr;
                } else {
                  val = Array.newInstance(type,((List)val).size());
                }
              }
              allValuesMap.put(field, val);
            }else {
              if (val instanceof Collection) {
                allValuesList.addAll((Collection) val);
              } else {
                allValuesList.add(val);
              }
            }
          }
        }
        if (isContainedInMap) {
          return allValuesMap.isEmpty() ? null : allValuesMap;
        } else {
          return allValuesList.isEmpty() ? null : allValuesList;
        }
      }
      return null;
    }
    <T> void inject(T obj, SolrDocument sdoc) {
      Object val = getFieldValue(sdoc);
      if(val == null) {
        return;
      }
      if(isArray && !isContainedInMap){
        List list = null;
        if(val.getClass().isArray()){
          set(obj,val);
          return;
        } else if (val instanceof List) {
          list = (List) val;
        } else{
          list = new ArrayList();
          list.add(val);
        }
        set(obj, list.toArray((Object[]) Array.newInstance(type,list.size())));        
      } else if(isList && !isContainedInMap){
        if (!(val instanceof List)) {
          ArrayList list = new ArrayList();
          list.add(val);
          val =  list;
        }
        set(obj, val);
      } else if(isContainedInMap){
        if (val instanceof Map) {
          set(obj,  val);
        }
      } else {
        set(obj, val);
      }
    }
    private void set(Object obj, Object v) {
      if(v!= null && type == ByteBuffer.class && v.getClass()== byte[].class) {
        v = ByteBuffer.wrap((byte[])v);
      }
      try {
        if (field != null) {
          field.set(obj, v);
        } else if (setter != null) {
          setter.invoke(obj, v);
        }
      } 
      catch (Exception e) {
        throw new RuntimeException("Exception while setting value : "+v+" on " + (field != null ? field : setter), e);
      }
    }
    public Object get( final Object obj )
    {
      if (field != null) {
        try {
          return field.get(obj);
        } 
        catch (Exception e) {        
          throw new RuntimeException("Exception while getting value: " + field, e);
        }
      }
      else if (getter == null) {
        throw new RuntimeException( "Missing getter for field: "+name+" -- You can only call the 'get' for fields that have a field of 'get' method" );
      }
      try {
        return getter.invoke( obj, (Object[])null );
      } 
      catch (Exception e) {        
        throw new RuntimeException("Exception while getting value: " + getter, e);
      }
    }
  }
}
