package org.apache.lucene.index;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
final class DocInverterPerField extends DocFieldConsumerPerField {
  final private DocInverterPerThread perThread;
  final private FieldInfo fieldInfo;
  final InvertedDocConsumerPerField consumer;
  final InvertedDocEndConsumerPerField endConsumer;
  final DocumentsWriter.DocState docState;
  final FieldInvertState fieldState;
  public DocInverterPerField(DocInverterPerThread perThread, FieldInfo fieldInfo) {
    this.perThread = perThread;
    this.fieldInfo = fieldInfo;
    docState = perThread.docState;
    fieldState = perThread.fieldState;
    this.consumer = perThread.consumer.addField(this, fieldInfo);
    this.endConsumer = perThread.endConsumer.addField(this, fieldInfo);
  }
  @Override
  void abort() {
    consumer.abort();
    endConsumer.abort();
  }
  @Override
  public void processFields(final Fieldable[] fields,
                            final int count) throws IOException {
    fieldState.reset(docState.doc.getBoost());
    final int maxFieldLength = docState.maxFieldLength;
    final boolean doInvert = consumer.start(fields, count);
    for(int i=0;i<count;i++) {
      final Fieldable field = fields[i];
      if (field.isIndexed() && doInvert) {
        final boolean anyToken;
        if (fieldState.length > 0)
          fieldState.position += docState.analyzer.getPositionIncrementGap(fieldInfo.name);
        if (!field.isTokenized()) {		  
          String stringValue = field.stringValue();
          final int valueLength = stringValue.length();
          perThread.singleToken.reinit(stringValue, 0, valueLength);
          fieldState.attributeSource = perThread.singleToken;
          consumer.start(field);
          boolean success = false;
          try {
            consumer.add();
            success = true;
          } finally {
            if (!success)
              docState.docWriter.setAborting();
          }
          fieldState.offset += valueLength;
          fieldState.length++;
          fieldState.position++;
          anyToken = valueLength > 0;
        } else {                                  
          final TokenStream stream;
          final TokenStream streamValue = field.tokenStreamValue();
          if (streamValue != null) 
            stream = streamValue;
          else {
            final Reader reader;			  
            final Reader readerValue = field.readerValue();
            if (readerValue != null)
              reader = readerValue;
            else {
              String stringValue = field.stringValue();
              if (stringValue == null)
                throw new IllegalArgumentException("field must have either TokenStream, String or Reader value");
              perThread.stringReader.init(stringValue);
              reader = perThread.stringReader;
            }
            stream = docState.analyzer.reusableTokenStream(fieldInfo.name, reader);
          }
          stream.reset();
          final int startLength = fieldState.length;
          try {
            boolean hasMoreTokens = stream.incrementToken();
            fieldState.attributeSource = stream;
            OffsetAttribute offsetAttribute = fieldState.attributeSource.addAttribute(OffsetAttribute.class);
            PositionIncrementAttribute posIncrAttribute = fieldState.attributeSource.addAttribute(PositionIncrementAttribute.class);
            consumer.start(field);
            for(;;) {
              if (!hasMoreTokens) break;
              final int posIncr = posIncrAttribute.getPositionIncrement();
              fieldState.position += posIncr;
              if (fieldState.position > 0) {
                fieldState.position--;
              }
              if (posIncr == 0)
                fieldState.numOverlap++;
              boolean success = false;
              try {
                consumer.add();
                success = true;
              } finally {
                if (!success)
                  docState.docWriter.setAborting();
              }
              fieldState.position++;
              if (++fieldState.length >= maxFieldLength) {
                if (docState.infoStream != null)
                  docState.infoStream.println("maxFieldLength " +maxFieldLength+ " reached for field " + fieldInfo.name + ", ignoring following tokens");
                break;
              }
              hasMoreTokens = stream.incrementToken();
            }
            stream.end();
            fieldState.offset += offsetAttribute.endOffset();
            anyToken = fieldState.length > startLength;
          } finally {
            stream.close();
          }
        }
        if (anyToken)
          fieldState.offset += docState.analyzer.getOffsetGap(field);
        fieldState.boost *= field.getBoost();
      }
    }
    consumer.finish();
    endConsumer.finish();
  }
}
