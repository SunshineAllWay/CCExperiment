package org.apache.solr.update;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.schema.*;
public class DocumentBuilder {
  private final IndexSchema schema;
  private Document doc;
  private HashMap<String,String> map;
  public DocumentBuilder(IndexSchema schema) {
    this.schema = schema;
  }
  public void startDoc() {
    doc = new Document();
    map = new HashMap<String,String>();
  }
  protected void addSingleField(SchemaField sfield, String val, float boost) {
    if (sfield.isPolyField()) {
      Fieldable[] fields = sfield.createFields(val, boost);
      if (fields.length > 0) {
        if (!sfield.multiValued()) {
          String oldValue = map.put(sfield.getName(), val);
          if (oldValue != null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "ERROR: multiple values encountered for non multiValued field " + sfield.getName()
                    + ": first='" + oldValue + "' second='" + val + "'");
          }
        }
        for (Fieldable field : fields) {
          doc.add(field);
        }
      }
    } else {
      Field field = sfield.createField(val, boost);
      if (field != null) {
        if (!sfield.multiValued()) {
          String oldValue = map.put(sfield.getName(), val);
          if (oldValue != null) {
            throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"ERROR: multiple values encountered for non multiValued field " + sfield.getName()
                    + ": first='" + oldValue + "' second='" + val + "'");
          }
        }
      }
      doc.add(field);
    }
  }
  public void addField(SchemaField sfield, String val, float boost) {
    addSingleField(sfield,val,boost);
  }
  public void addField(String name, String val) {
    addField(name, val, 1.0f);
  }
  public void addField(String name, String val, float boost) {
    SchemaField sfield = schema.getFieldOrNull(name);
    if (sfield != null) {
      addField(sfield,val,boost);
    }
    final List<CopyField> copyFields = schema.getCopyFieldsList(name);
    if (copyFields != null) {
      for(CopyField cf : copyFields) {
        addSingleField(cf.getDestination(), cf.getLimitedValue( val ), boost);
      }
    }
    if (sfield==null && (copyFields==null || copyFields.size()==0)) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"ERROR:unknown field '" + name + "'");
    }
  }
  public void setBoost(float boost) {
    doc.setBoost(boost);
  }
  public void endDoc() {
  }
  public Document getDoc() throws IllegalArgumentException {
    List<String> missingFields = null;
    for (SchemaField field : schema.getRequiredFields()) {
      if (doc.getField(field.getName() ) == null) {
        if (field.getDefaultValue() != null) {
          addField(doc, field, field.getDefaultValue(), 1.0f);
        } else {
          if (missingFields==null) {
            missingFields = new ArrayList<String>(1);
          }
          missingFields.add(field.getName());
        }
      }
    }
    if (missingFields != null) {
      StringBuilder builder = new StringBuilder();
      if( schema.getUniqueKeyField() != null ) {
        String n = schema.getUniqueKeyField().getName();
        String v = doc.get( n );
        builder.append( "Document ["+n+"="+v+"] " );
      }
      builder.append("missing required fields: " );
      for (String field : missingFields) {
        builder.append(field);
        builder.append(" ");
      }
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, builder.toString());
    }
    Document ret = doc; doc=null;
    return ret;
  }
  private static void addField(Document doc, SchemaField field, String val, float boost) {
    if (field.isPolyField()) {
      Fieldable[] farr = field.getType().createFields(field, val, boost);
      for (Fieldable f : farr) {
        if (f != null) doc.add(f); 
      }
    } else {
      Field f = field.createField(val, boost);
      if (f != null) doc.add(f);  
    }
  }
  public static Document toDocument( SolrInputDocument doc, IndexSchema schema )
  { 
    Document out = new Document();
    out.setBoost( doc.getDocumentBoost() );
    for( SolrInputField field : doc ) {
      String name = field.getName();
      SchemaField sfield = schema.getFieldOrNull(name);
      boolean used = false;
      float boost = field.getBoost();
      if( sfield!=null && !sfield.multiValued() && field.getValueCount() > 1 ) {
        String id = "";
        SchemaField sf = schema.getUniqueKeyField();
        if( sf != null ) {
          id = "["+doc.getFieldValue( sf.getName() )+"] ";
        }
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
            "ERROR: "+id+"multiple values encountered for non multiValued field " + 
              sfield.getName() + ": " +field.getValue() );
      }
      boolean hasField = false;
      for( Object v : field ) {
        if( v == null ) {
          continue;
        }
        String val = null;
        hasField = true;
        boolean isBinaryField = false;
        if (sfield != null && sfield.getType() instanceof BinaryField) {
          isBinaryField = true;
          BinaryField binaryField = (BinaryField) sfield.getType();
          Field f = binaryField.createField(sfield,v,boost);
          if(f != null){
            out.add(f);
          }
          used = true;
        } else {
          if (sfield != null && v instanceof Date && sfield.getType() instanceof DateField) {
            DateField df = (DateField) sfield.getType();
            val = df.toInternal((Date) v) + 'Z';
          } else if (v != null) {
            val = v.toString();
          }
          if (sfield != null) {
            used = true;
            addField(out, sfield, val, boost);
          }
        }
        List<CopyField> copyFields = schema.getCopyFieldsList(name);
        for (CopyField cf : copyFields) {
          SchemaField destinationField = cf.getDestination();
          if (!destinationField.multiValued() && out.get(destinationField.getName()) != null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "ERROR: multiple values encountered for non multiValued copy field " +
                            destinationField.getName() + ": " + val);
          }
          used = true;
          Fieldable [] fields = null;
          if (isBinaryField) {
            if (destinationField.getType() instanceof BinaryField) {
              BinaryField binaryField = (BinaryField) destinationField.getType();
              fields = new Field[]{binaryField.createField(destinationField, v, boost)};
            }
          } else {
            fields = destinationField.createFields(cf.getLimitedValue(val), boost);
          }
          if (fields != null) { 
            for (Fieldable f : fields) {
              out.add(f);
            }
          }
        }
        boost = 1.0f; 
      }
      if( !used && hasField ) {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"ERROR:unknown field '" +
                name + "'");
      }
    }
    for (SchemaField field : schema.getRequiredFields()) {
      if (out.getField(field.getName() ) == null) {
        if (field.getDefaultValue() != null) {
          addField(out, field, field.getDefaultValue(), 1.0f);
        } 
        else {
          String id = schema.printableUniqueKey( out );
          String msg = "Document ["+id+"] missing required field: " + field.getName();
          throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, msg );
        }
      }
    }
    return out;
  }
  public SolrDocument loadStoredFields( SolrDocument doc, Document luceneDoc  )
  {
    for( Object f : luceneDoc.getFields() ) {
      Fieldable field = (Fieldable)f;
      if( field.isStored() ) {
        SchemaField sf = schema.getField( field.name() );
        if( !schema.isCopyFieldTarget( sf ) ) {
          doc.addField( field.name(), sf.getType().toObject( field ) );
        }
      }
    }
    return doc;
  }
}
