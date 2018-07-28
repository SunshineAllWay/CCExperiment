package org.apache.lucene.search.vectorhighlight;
public interface FragListBuilder {
  public FieldFragList createFieldFragList( FieldPhraseList fieldPhraseList, int fragCharSize );
}
