package org.apache.lucene.search.spans;
import java.io.IOException;
import java.util.Collection;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.Similarity;
final class JustCompileSearchSpans {
  private static final String UNSUPPORTED_MSG = "unsupported: used for back-compat testing only !";
  static final class JustCompileSpans extends Spans {
    @Override
    public int doc() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int end() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean next() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean skipTo(int target) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int start() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Collection<byte[]> getPayload() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean isPayloadAvailable() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileSpanQuery extends SpanQuery {
    @Override
    public String getField() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public Spans getSpans(IndexReader reader) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public String toString(String field) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompilePayloadSpans extends Spans {
    @Override
    public Collection<byte[]> getPayload() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean isPayloadAvailable() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int doc() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int end() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean next() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean skipTo(int target) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int start() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileSpanScorer extends SpanScorer {
    protected JustCompileSpanScorer(Spans spans, Weight weight,
        Similarity similarity, byte[] norms) throws IOException {
      super(spans, weight, similarity, norms);
    }
    @Override
    protected boolean setFreqCurrentDoc() throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
}
