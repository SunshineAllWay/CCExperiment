package org.apache.lucene.index;
abstract class InvertedDocEndConsumerPerField {
  abstract void finish();
  abstract void abort();
}
