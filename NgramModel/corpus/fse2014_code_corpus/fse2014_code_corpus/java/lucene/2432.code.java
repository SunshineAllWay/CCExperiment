package org.apache.solr.schema;
import org.apache.solr.response.XMLWriter;
import org.apache.lucene.document.Fieldable;
import java.io.IOException;
public class BCDStrField extends BCDIntField {
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeStr(name,toExternal(f));
  }
  @Override
  public String toObject(Fieldable f) {
    return toExternal(f);
  }
}
