package org.apache.solr.update;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
public class AddUpdateCommand extends UpdateCommand {
   public String indexedId;
   public Document doc;
   public SolrInputDocument solrDoc;
   public boolean allowDups;
   public boolean overwritePending;
   public boolean overwriteCommitted;
   public Term updateTerm;
   public int commitWithin = -1;
   public void clear() {
     doc = null;
     solrDoc = null;
     indexedId = null;
   }
   public SolrInputDocument getSolrInputDocument() {
     return solrDoc;
   }
   public Document getLuceneDocument(IndexSchema schema) {
     if (doc == null && solrDoc != null) {
     }
     return doc;    
   }
   public String getIndexedId(IndexSchema schema) {
     if (indexedId == null) {
       SchemaField sf = schema.getUniqueKeyField();
       if (sf != null) {
         if (doc != null) {
           schema.getUniqueKeyField();
           Field storedId = doc.getField(sf.getName());
           indexedId = sf.getType().storedToIndexed(storedId);
         }
         if (solrDoc != null) {
           SolrInputField field = solrDoc.getField(sf.getName());
           if (field != null) {
             indexedId = sf.getType().toInternal( field.getFirstValue().toString() );
           }
         }
       }
     }
     return indexedId;
   }
   public String getPrintableId(IndexSchema schema) {
     SchemaField sf = schema.getUniqueKeyField();
     if (indexedId != null) {
       return schema.getUniqueKeyField().getType().indexedToReadable(indexedId);
     }
     if (doc != null) {
       return schema.printableUniqueKey(doc);
     }
     if (solrDoc != null) {
       SolrInputField field = solrDoc.getField(sf.getName());
       if (field != null) {
         return field.getFirstValue().toString();
       }
     }
     return "(null)";
   }
   public AddUpdateCommand() {
     super("add");
   }
   @Override
  public String toString() {
     StringBuilder sb = new StringBuilder(commandName);
     sb.append(':');
     if (indexedId !=null) sb.append("id=").append(indexedId);
     sb.append(",allowDups=").append(allowDups);
     sb.append(",overwritePending=").append(overwritePending);
     sb.append(",overwriteCommitted=").append(overwriteCommitted);
     return sb.toString();
   }
 }
