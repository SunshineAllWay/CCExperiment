package org.apache.lucene.index;
import java.io.IOException;
abstract class FormatPostingsFieldsConsumer {
  abstract FormatPostingsTermsConsumer addField(FieldInfo field) throws IOException;
  abstract void finish() throws IOException;
}
