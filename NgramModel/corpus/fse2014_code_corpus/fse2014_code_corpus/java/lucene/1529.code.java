package org.apache.lucene.index;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMOutputStream;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.IndexInput;
final class FieldsWriter
{
  static final byte FIELD_IS_TOKENIZED = 0x1;
  static final byte FIELD_IS_BINARY = 0x2;
  @Deprecated
  static final byte FIELD_IS_COMPRESSED = 0x4;
  static final int FORMAT = 0;
  static final int FORMAT_VERSION_UTF8_LENGTH_IN_BYTES = 1;
  static final int FORMAT_LUCENE_3_0_NO_COMPRESSED_FIELDS = 2;
  static final int FORMAT_CURRENT = FORMAT_LUCENE_3_0_NO_COMPRESSED_FIELDS;
    private FieldInfos fieldInfos;
    private IndexOutput fieldsStream;
    private IndexOutput indexStream;
    private boolean doClose;
    FieldsWriter(Directory d, String segment, FieldInfos fn) throws IOException {
        fieldInfos = fn;
        boolean success = false;
        final String fieldsName = IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_EXTENSION);
        try {
          fieldsStream = d.createOutput(fieldsName);
          fieldsStream.writeInt(FORMAT_CURRENT);
          success = true;
        } finally {
          if (!success) {
            try {
              close();
            } catch (Throwable t) {
            }
            try {
              d.deleteFile(fieldsName);
            } catch (Throwable t) {
            }
          }
        }
        success = false;
        final String indexName = IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_INDEX_EXTENSION);
        try {
          indexStream = d.createOutput(indexName);
          indexStream.writeInt(FORMAT_CURRENT);
          success = true;
        } finally {
          if (!success) {
            try {
              close();
            } catch (IOException ioe) {
            }
            try {
              d.deleteFile(fieldsName);
            } catch (Throwable t) {
            }
            try {
              d.deleteFile(indexName);
            } catch (Throwable t) {
            }
          }
        }
        doClose = true;
    }
    FieldsWriter(IndexOutput fdx, IndexOutput fdt, FieldInfos fn) {
        fieldInfos = fn;
        fieldsStream = fdt;
        indexStream = fdx;
        doClose = false;
    }
    void setFieldsStream(IndexOutput stream) {
      this.fieldsStream = stream;
    }
    void flushDocument(int numStoredFields, RAMOutputStream buffer) throws IOException {
      indexStream.writeLong(fieldsStream.getFilePointer());
      fieldsStream.writeVInt(numStoredFields);
      buffer.writeTo(fieldsStream);
    }
    void skipDocument() throws IOException {
      indexStream.writeLong(fieldsStream.getFilePointer());
      fieldsStream.writeVInt(0);
    }
    void flush() throws IOException {
      indexStream.flush();
      fieldsStream.flush();
    }
    final void close() throws IOException {
      if (doClose) {
        try {
          if (fieldsStream != null) {
            try {
              fieldsStream.close();
            } finally {
              fieldsStream = null;
            }
          }
        } catch (IOException ioe) {
          try {
            if (indexStream != null) {
              try {
                indexStream.close();
              } finally {
                indexStream = null;
              }
            }
          } catch (IOException ioe2) {
          }
          throw ioe;
        } finally {
          if (indexStream != null) {
            try {
              indexStream.close();
            } finally {
              indexStream = null;
            }
          }
        }
      }
    }
    final void writeField(FieldInfo fi, Fieldable field) throws IOException {
      fieldsStream.writeVInt(fi.number);
      byte bits = 0;
      if (field.isTokenized())
        bits |= FieldsWriter.FIELD_IS_TOKENIZED;
      if (field.isBinary())
        bits |= FieldsWriter.FIELD_IS_BINARY;
      fieldsStream.writeByte(bits);
      if (field.isBinary()) {
        final byte[] data;
        final int len;
        final int offset;
        data = field.getBinaryValue();
        len = field.getBinaryLength();
        offset =  field.getBinaryOffset();
        fieldsStream.writeVInt(len);
        fieldsStream.writeBytes(data, offset, len);
      }
      else {
        fieldsStream.writeString(field.stringValue());
      }
    }
    final void addRawDocuments(IndexInput stream, int[] lengths, int numDocs) throws IOException {
      long position = fieldsStream.getFilePointer();
      long start = position;
      for(int i=0;i<numDocs;i++) {
        indexStream.writeLong(position);
        position += lengths[i];
      }
      fieldsStream.copyBytes(stream, position-start);
      assert fieldsStream.getFilePointer() == position;
    }
    final void addDocument(Document doc) throws IOException {
        indexStream.writeLong(fieldsStream.getFilePointer());
        int storedCount = 0;
        List<Fieldable> fields = doc.getFields();
        for (Fieldable field : fields) {
            if (field.isStored())
                storedCount++;
        }
        fieldsStream.writeVInt(storedCount);
        for (Fieldable field : fields) {
            if (field.isStored())
              writeField(fieldInfos.fieldInfo(field.name()), field);
        }
    }
}
