package org.apache.lucene.document;
import java.io.Serializable;
public interface FieldSelector extends Serializable {
  FieldSelectorResult accept(String fieldName);
}
