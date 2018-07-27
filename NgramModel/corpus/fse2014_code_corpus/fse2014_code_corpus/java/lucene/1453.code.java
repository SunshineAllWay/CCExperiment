package org.apache.lucene.analysis;
import org.apache.lucene.util.AttributeSource;
import java.io.Reader;
import java.io.IOException;
public abstract class Tokenizer extends TokenStream {
  protected Reader input;
  protected Tokenizer() {}
  protected Tokenizer(Reader input) {
    this.input = CharReader.get(input);
  }
  protected Tokenizer(AttributeFactory factory) {
    super(factory);
  }
  protected Tokenizer(AttributeFactory factory, Reader input) {
    super(factory);
    this.input = CharReader.get(input);
  }
  protected Tokenizer(AttributeSource source) {
    super(source);
  }
  protected Tokenizer(AttributeSource source, Reader input) {
    super(source);
    this.input = CharReader.get(input);
  }
  @Override
  public void close() throws IOException {
    input.close();
  }
  protected final int correctOffset(int currentOff) {
    return (input instanceof CharStream) ? ((CharStream) input).correctOffset(currentOff) : currentOff;
  }
  public void reset(Reader input) throws IOException {
    this.input = input;
  }
}
