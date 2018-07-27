package org.apache.lucene.index;
import org.apache.lucene.util.StringHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class TermVectorAccessor {
  public TermVectorAccessor() {
  }
  private TermVectorMapperDecorator decoratedMapper = new TermVectorMapperDecorator();
  public void accept(IndexReader indexReader, int documentNumber, String fieldName, TermVectorMapper mapper) throws IOException {
    fieldName = StringHelper.intern(fieldName);
    decoratedMapper.decorated = mapper;
    decoratedMapper.termVectorStored = false;
    indexReader.getTermFreqVector(documentNumber, fieldName, decoratedMapper);
    if (!decoratedMapper.termVectorStored) {
      mapper.setDocumentNumber(documentNumber);
      build(indexReader, fieldName, mapper, documentNumber);
    }
  }
  private List<String> tokens;
  private List<int[]> positions;
  private List<Integer> frequencies;
  private void build(IndexReader indexReader, String field, TermVectorMapper mapper, int documentNumber) throws IOException {
    if (tokens == null) {
      tokens = new ArrayList<String>(500);
      positions = new ArrayList<int[]>(500);
      frequencies = new ArrayList<Integer>(500);
    } else {
      tokens.clear();
      frequencies.clear();
      positions.clear();
    }
    TermEnum termEnum = indexReader.terms(new Term(field, ""));
    if (termEnum.term() != null) {
      while (termEnum.term().field() == field) {
        TermPositions termPositions = indexReader.termPositions(termEnum.term());
        if (termPositions.skipTo(documentNumber)) {
          frequencies.add(Integer.valueOf(termPositions.freq()));
          tokens.add(termEnum.term().text());
          if (!mapper.isIgnoringPositions()) {
            int[] positions = new int[termPositions.freq()];
            for (int i = 0; i < positions.length; i++) {
              positions[i] = termPositions.nextPosition();
            }
            this.positions.add(positions);
          } else {
            positions.add(null);
          }
        }
        termPositions.close();
        if (!termEnum.next()) {
          break;
        }
      }
      mapper.setDocumentNumber(documentNumber);
      mapper.setExpectations(field, tokens.size(), false, !mapper.isIgnoringPositions());
      for (int i = 0; i < tokens.size(); i++) {
        mapper.map(tokens.get(i), frequencies.get(i).intValue(), (TermVectorOffsetInfo[]) null, positions.get(i));
      }
    }
    termEnum.close();
  }
  private static class TermVectorMapperDecorator extends TermVectorMapper {
    private TermVectorMapper decorated;
    @Override
    public boolean isIgnoringPositions() {
      return decorated.isIgnoringPositions();
    }
    @Override
    public boolean isIgnoringOffsets() {
      return decorated.isIgnoringOffsets();
    }
    private boolean termVectorStored = false;
    @Override
    public void setExpectations(String field, int numTerms, boolean storeOffsets, boolean storePositions) {
      decorated.setExpectations(field, numTerms, storeOffsets, storePositions);
      termVectorStored = true;
    }
    @Override
    public void map(String term, int frequency, TermVectorOffsetInfo[] offsets, int[] positions) {
      decorated.map(term, frequency, offsets, positions);
    }
    @Override
    public void setDocumentNumber(int documentNumber) {
      decorated.setDocumentNumber(documentNumber);
    }
  }
}
