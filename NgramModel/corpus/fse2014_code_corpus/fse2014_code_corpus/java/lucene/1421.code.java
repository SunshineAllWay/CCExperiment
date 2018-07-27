package org.apache.lucene.analysis;
import java.io.Reader;
import java.io.IOException;
import java.io.Closeable;
import org.apache.lucene.util.CloseableThreadLocal;
import org.apache.lucene.util.VirtualMethod;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.document.Fieldable;
public abstract class Analyzer implements Closeable {
  public abstract TokenStream tokenStream(String fieldName, Reader reader);
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
    return tokenStream(fieldName, reader);
  }
  private CloseableThreadLocal<Object> tokenStreams = new CloseableThreadLocal<Object>();
  protected Object getPreviousTokenStream() {
    try {
      return tokenStreams.get();
    } catch (NullPointerException npe) {
      if (tokenStreams == null) {
        throw new AlreadyClosedException("this Analyzer is closed");
      } else {
        throw npe;
      }
    }
  }
  protected void setPreviousTokenStream(Object obj) {
    try {
      tokenStreams.set(obj);
    } catch (NullPointerException npe) {
      if (tokenStreams == null) {
        throw new AlreadyClosedException("this Analyzer is closed");
      } else {
        throw npe;
      }
    }
  }
  private static final VirtualMethod<Analyzer> tokenStreamMethod =
    new VirtualMethod<Analyzer>(Analyzer.class, "tokenStream", String.class, Reader.class);
  private static final VirtualMethod<Analyzer> reusableTokenStreamMethod =
    new VirtualMethod<Analyzer>(Analyzer.class, "reusableTokenStream", String.class, Reader.class);
  @Deprecated
  protected final boolean overridesTokenStreamMethod =
    VirtualMethod.compareImplementationDistance(this.getClass(), tokenStreamMethod, reusableTokenStreamMethod) > 0;
  @Deprecated
  protected void setOverridesTokenStreamMethod(Class<? extends Analyzer> baseClass) {
  }
  public int getPositionIncrementGap(String fieldName) {
    return 0;
  }
  public int getOffsetGap(Fieldable field) {
    if (field.isTokenized())
      return 1;
    else
      return 0;
  }
  public void close() {
    tokenStreams.close();
    tokenStreams = null;
  }
}
