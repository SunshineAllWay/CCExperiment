package org.apache.lucene.spatial.tier;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.OpenBitSet;
public class CartesianShapeFilter extends Filter {
  private final Shape shape;
  private final String fieldName;
  CartesianShapeFilter(final Shape shape, final String fieldName){
    this.shape = shape;
    this.fieldName = fieldName;
  }
  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    final OpenBitSet bits = new OpenBitSet(reader.maxDoc());
    final TermDocs termDocs = reader.termDocs();
    final List<Double> area = shape.getArea();
    int sz = area.size();
    final Term term = new Term(fieldName);
    for (int i =0; i< sz; i++) {
      double boxId = area.get(i).doubleValue();
      termDocs.seek(term.createTerm(NumericUtils.doubleToPrefixCoded(boxId)));
      while (termDocs.next()) {
        bits.fastSet(termDocs.doc());
      }
    }
    return bits;
  }
}
