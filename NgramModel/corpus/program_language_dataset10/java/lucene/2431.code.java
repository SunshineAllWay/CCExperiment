package org.apache.solr.schema;
import org.apache.solr.response.XMLWriter;
import org.apache.lucene.document.Fieldable;
import java.io.IOException;
public class BCDLongField extends BCDIntField {
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeLong(name,toExternal(f));
  }
  @Override
  public Long toObject(Fieldable f) {
    return Long.valueOf( toExternal(f) );
  }
}
