package org.apache.lucene.analysis.shingle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.EmptyTokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.shingle.ShingleMatrixFilter.Matrix.Column.Row;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.index.Payload;
public final class ShingleMatrixFilter extends TokenStream {
  public static Character defaultSpacerCharacter = Character.valueOf('_');
  public static TokenSettingsCodec defaultSettingsCodec = new OneDimensionalNonWeightedTokenSettingsCodec();
  public static boolean ignoringSinglePrefixOrSuffixShingleByDefault = false;
  public static abstract class TokenSettingsCodec {
    public abstract TokenPositioner getTokenPositioner(Token token) throws IOException;
    public abstract void setTokenPositioner(Token token, ShingleMatrixFilter.TokenPositioner tokenPositioner);
    public abstract float getWeight(Token token);
    public abstract void setWeight(Token token, float weight);
  }
  public static class TokenPositioner {
    public static final TokenPositioner newColumn = new TokenPositioner(0);
    public static final TokenPositioner newRow = new TokenPositioner(1);
    public static final TokenPositioner sameRow = new TokenPositioner(2);
    private final int index;
    private TokenPositioner(int index) {
      this.index = index;
    }
    public int getIndex() {
      return index;
    }
  }
  private TokenSettingsCodec settingsCodec;
  private int minimumShingleSize;
  private int maximumShingleSize;
  private boolean ignoringSinglePrefixOrSuffixShingle = false;
  private Character spacerCharacter = defaultSpacerCharacter;
  private TokenStream input;
  private TermAttribute termAtt;
  private PositionIncrementAttribute posIncrAtt;
  private PayloadAttribute payloadAtt;
  private OffsetAttribute offsetAtt;
  private TypeAttribute typeAtt;
  private FlagsAttribute flagsAtt;
  private TermAttribute in_termAtt;
  private PositionIncrementAttribute in_posIncrAtt;
  private PayloadAttribute in_payloadAtt;
  private OffsetAttribute in_offsetAtt;
  private TypeAttribute in_typeAtt;
  private FlagsAttribute in_flagsAtt;
  public ShingleMatrixFilter(Matrix matrix, int minimumShingleSize, int maximumShingleSize, Character spacerCharacter, boolean ignoringSinglePrefixOrSuffixShingle, TokenSettingsCodec settingsCodec) {
    this.matrix = matrix;
    this.minimumShingleSize = minimumShingleSize;
    this.maximumShingleSize = maximumShingleSize;
    this.spacerCharacter = spacerCharacter;
    this.ignoringSinglePrefixOrSuffixShingle = ignoringSinglePrefixOrSuffixShingle;
    this.settingsCodec = settingsCodec;
    termAtt = addAttribute(TermAttribute.class);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    payloadAtt = addAttribute(PayloadAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
    flagsAtt = addAttribute(FlagsAttribute.class);
    this.input = new EmptyTokenStream();
    in_termAtt = input.addAttribute(TermAttribute.class);
    in_posIncrAtt = input.addAttribute(PositionIncrementAttribute.class);
    in_payloadAtt = input.addAttribute(PayloadAttribute.class);
    in_offsetAtt = input.addAttribute(OffsetAttribute.class);
    in_typeAtt = input.addAttribute(TypeAttribute.class);
    in_flagsAtt = input.addAttribute(FlagsAttribute.class);
  }
  public ShingleMatrixFilter(TokenStream input, int minimumShingleSize, int maximumShingleSize) {
    this(input, minimumShingleSize, maximumShingleSize, defaultSpacerCharacter);
  }
  public ShingleMatrixFilter(TokenStream input, int minimumShingleSize, int maximumShingleSize, Character spacerCharacter) {
    this(input, minimumShingleSize, maximumShingleSize, spacerCharacter, ignoringSinglePrefixOrSuffixShingleByDefault);
  }
  public ShingleMatrixFilter(TokenStream input, int minimumShingleSize, int maximumShingleSize, Character spacerCharacter, boolean ignoringSinglePrefixOrSuffixShingle) {
    this(input, minimumShingleSize, maximumShingleSize, spacerCharacter, ignoringSinglePrefixOrSuffixShingle, defaultSettingsCodec);
  }
  public ShingleMatrixFilter(TokenStream input, int minimumShingleSize, int maximumShingleSize, Character spacerCharacter, boolean ignoringSinglePrefixOrSuffixShingle, TokenSettingsCodec settingsCodec) {
    this.input = input;
    this.minimumShingleSize = minimumShingleSize;
    this.maximumShingleSize = maximumShingleSize;
    this.spacerCharacter = spacerCharacter;
    this.ignoringSinglePrefixOrSuffixShingle = ignoringSinglePrefixOrSuffixShingle;
    this.settingsCodec = settingsCodec;
    termAtt = addAttribute(TermAttribute.class);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    payloadAtt = addAttribute(PayloadAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
    flagsAtt = addAttribute(FlagsAttribute.class);
    in_termAtt = input.addAttribute(TermAttribute.class);
    in_posIncrAtt = input.addAttribute(PositionIncrementAttribute.class);
    in_payloadAtt = input.addAttribute(PayloadAttribute.class);
    in_offsetAtt = input.addAttribute(OffsetAttribute.class);
    in_typeAtt = input.addAttribute(TypeAttribute.class);
    in_flagsAtt = input.addAttribute(FlagsAttribute.class);
  }
  private Iterator<Matrix.Column.Row[]> permutations;
  private List<Token> currentPermuationTokens;
  private List<Matrix.Column.Row> currentPermutationRows;
  private int currentPermutationTokensStartOffset;
  private int currentShingleLength;
  private Set<List<Token>> shinglesSeen = new HashSet<List<Token>>();
  @Override
  public void reset() throws IOException {
    permutations = null;
    shinglesSeen.clear();
    input.reset();
  }
  private Matrix matrix;
  private Token reusableToken = new Token();
  @Override
  public final boolean incrementToken() throws IOException {
    if (matrix == null) {
      matrix = new Matrix();
      while (matrix.columns.size() < maximumShingleSize && readColumn()) {
      }
    }
    Token token;
    do {
      token = produceNextToken(reusableToken);
    } while (token == request_next_token);
    if (token == null) return false;
    clearAttributes();
    termAtt.setTermBuffer(token.termBuffer(), 0, token.termLength());
    posIncrAtt.setPositionIncrement(token.getPositionIncrement());
    flagsAtt.setFlags(token.getFlags());
    offsetAtt.setOffset(token.startOffset(), token.endOffset());
    typeAtt.setType(token.type());
    payloadAtt.setPayload(token.getPayload());
    return true;
  }
  private Token getNextInputToken(Token token) throws IOException {
    if (!input.incrementToken()) return null;
    token.setTermBuffer(in_termAtt.termBuffer(), 0, in_termAtt.termLength());
    token.setPositionIncrement(in_posIncrAtt.getPositionIncrement());
    token.setFlags(in_flagsAtt.getFlags());
    token.setOffset(in_offsetAtt.startOffset(), in_offsetAtt.endOffset());
    token.setType(in_typeAtt.type());
    token.setPayload(in_payloadAtt.getPayload());
    return token;
  }
  private Token getNextToken(Token token) throws IOException {
    if (!this.incrementToken()) return null;
    token.setTermBuffer(termAtt.termBuffer(), 0, termAtt.termLength());
    token.setPositionIncrement(posIncrAtt.getPositionIncrement());
    token.setFlags(flagsAtt.getFlags());
    token.setOffset(offsetAtt.startOffset(), offsetAtt.endOffset());
    token.setType(typeAtt.type());
    token.setPayload(payloadAtt.getPayload());
    return token;
  }
  private static final Token request_next_token = new Token();
  private Token produceNextToken(final Token reusableToken) throws IOException {
    if (currentPermuationTokens != null) {
      currentShingleLength++;
      if (currentShingleLength + currentPermutationTokensStartOffset <= currentPermuationTokens.size()
          && currentShingleLength <= maximumShingleSize) {
        if (ignoringSinglePrefixOrSuffixShingle
            && currentShingleLength == 1
            && ((currentPermutationRows.get(currentPermutationTokensStartOffset)).getColumn().isFirst() || (currentPermutationRows.get(currentPermutationTokensStartOffset)).getColumn().isLast())) {
          return getNextToken(reusableToken);
        }
        int termLength = 0;
        List<Token> shingle = new ArrayList<Token>(currentShingleLength);
        for (int i = 0; i < currentShingleLength; i++) {
          Token shingleToken = currentPermuationTokens.get(i + currentPermutationTokensStartOffset);
          termLength += shingleToken.termLength();
          shingle.add(shingleToken);
        }
        if (spacerCharacter != null) {
          termLength += currentShingleLength - 1;
        }
        if (!shinglesSeen.add(shingle)) {
          return request_next_token;
        }
        StringBuilder sb = new StringBuilder(termLength + 10); 
        for (Token shingleToken : shingle) {
          if (spacerCharacter != null && sb.length() > 0) {
            sb.append(spacerCharacter);
          }
          sb.append(shingleToken.termBuffer(), 0, shingleToken.termLength());
        }
        reusableToken.setTermBuffer(sb.toString());
        updateToken(reusableToken, shingle, currentPermutationTokensStartOffset, currentPermutationRows, currentPermuationTokens);
        return reusableToken;
      } else {
        if (currentPermutationTokensStartOffset < currentPermuationTokens.size() - 1) {
          currentPermutationTokensStartOffset++;
          currentShingleLength = minimumShingleSize - 1;
          return request_next_token;
        }
        if (permutations == null) {
          return null;
        }
        if (!permutations.hasNext()) {
          if (input != null && readColumn()) {
          }
          Matrix.Column deletedColumn = matrix.columns.remove(0);
          List<Token> deletedColumnTokens = new ArrayList<Token>();
          for (Matrix.Column.Row row : deletedColumn.getRows()) {
            for (Token token : row.getTokens()) {
              deletedColumnTokens.add(token);
            }
          }
          for (Iterator<List<Token>> shinglesSeenIterator = shinglesSeen.iterator(); shinglesSeenIterator.hasNext();) {
            List<Token> shingle = shinglesSeenIterator.next();
            for (Token deletedColumnToken : deletedColumnTokens) {
              if (shingle.contains(deletedColumnToken)) {
                shinglesSeenIterator.remove();
                break;
              }
            }
          }
          if (matrix.columns.size() < minimumShingleSize) {
            return null;
          }
          permutations = matrix.permutationIterator();
        }
        nextTokensPermutation();
        return request_next_token;
      }
    }
    if (permutations == null) {
      permutations = matrix.permutationIterator();
    }
    if (!permutations.hasNext()) {
      return null;
    }
    nextTokensPermutation();
    return request_next_token;
  }
  private void nextTokensPermutation() {
    Matrix.Column.Row[] rowsPermutation = permutations.next();
    List<Matrix.Column.Row> currentPermutationRows = new ArrayList<Matrix.Column.Row>();
    List<Token> currentPermuationTokens = new ArrayList<Token>();
    for (Matrix.Column.Row row : rowsPermutation) {
      for (Token token : row.getTokens()) {
        currentPermuationTokens.add(token);
        currentPermutationRows.add(row);
      }
    }
    this.currentPermuationTokens = currentPermuationTokens;
    this.currentPermutationRows = currentPermutationRows;
    currentPermutationTokensStartOffset = 0;
    currentShingleLength = minimumShingleSize - 1;
  }
  public void updateToken(Token token, List<Token> shingle, int currentPermutationStartOffset, List<Row> currentPermutationRows, List<Token> currentPermuationTokens) {
    token.setType(ShingleMatrixFilter.class.getName());
    token.setFlags(0);
    token.setPositionIncrement(1);
    token.setStartOffset(shingle.get(0).startOffset());
    token.setEndOffset(shingle.get(shingle.size() - 1).endOffset());
    settingsCodec.setWeight(token, calculateShingleWeight(token, shingle, currentPermutationStartOffset, currentPermutationRows, currentPermuationTokens));
  }
  public float calculateShingleWeight(Token shingleToken, List<Token> shingle, int currentPermutationStartOffset, List<Row> currentPermutationRows, List<Token> currentPermuationTokens) {
    double[] weights = new double[shingle.size()];
    double total = 0f;
    double top = 0d;
    for (int i=0; i<weights.length; i++) {
      weights[i] = settingsCodec.getWeight(shingle.get(i));
      double tmp = weights[i];
      if (tmp > top) {
        top = tmp;
      }
      total += tmp;
    }
    double factor = 1d / Math.sqrt(total);
    double weight = 0d;
    for (double partWeight : weights) {
      weight += partWeight * factor;
    }
    return (float) weight;
  }
  private Token readColumnBuf;
  private boolean readColumn() throws IOException {
    Token token;
    if (readColumnBuf != null) {
      token = readColumnBuf;
      readColumnBuf = null;
    } else {
      token = getNextInputToken(new Token());
    }
    if (token == null) {
      return false;
    }
    Matrix.Column currentReaderColumn = matrix.new Column();
    Matrix.Column.Row currentReaderRow = currentReaderColumn.new Row();
    currentReaderRow.getTokens().add(token);
    TokenPositioner tokenPositioner;
    while ((readColumnBuf = getNextInputToken(new Token())) != null
        && (tokenPositioner = settingsCodec.getTokenPositioner(readColumnBuf)) != TokenPositioner.newColumn) {
      if (tokenPositioner == TokenPositioner.sameRow) {
        currentReaderRow.getTokens().add(readColumnBuf);
      } else  {
        currentReaderRow = currentReaderColumn.new Row();
        currentReaderRow.getTokens().add(readColumnBuf);
      }
      readColumnBuf = null;
    }
    if (readColumnBuf == null) {
      readColumnBuf = getNextInputToken(new Token());
      if (readColumnBuf == null) {
        currentReaderColumn.setLast(true);
      }
    }
    return true;
  }
  public static class Matrix {
    private boolean columnsHasBeenCreated = false;
    private List<Column> columns = new ArrayList<Column>();
    public List<Column> getColumns() {
      return columns;
    }
    public class Column {
      private boolean last;
      private boolean first;
      public Matrix getMatrix() {
        return Matrix.this;
      }
      public Column(Token token) {
        this();
        Row row = new Row();
        row.getTokens().add(token);
      }
      public Column() {
        synchronized (Matrix.this) {
          if (!columnsHasBeenCreated) {
            this.setFirst(true);
            columnsHasBeenCreated = true;
          }
        }
        Matrix.this.columns.add(this);
      }
      private List<Row> rows = new ArrayList<Row>();
      public List<Row> getRows() {
        return rows;
      }
      public int getIndex() {
        return Matrix.this.columns.indexOf(this);
      }
      @Override
      public String toString() {
        return "Column{" +
            "first=" + first +
            ", last=" + last +
            ", rows=" + rows +
            '}';
      }
      public boolean isFirst() {
        return first;
      }
      public void setFirst(boolean first) {
        this.first = first;
      }
      public void setLast(boolean last) {
        this.last = last;
      }
      public boolean isLast() {
        return last;
      }
      public class Row {
        public Column getColumn() {
          return Column.this;
        }
        private List<Token> tokens = new LinkedList<Token>();
        public Row() {
          Column.this.rows.add(this);
        }
        public int getIndex() {
          return Column.this.rows.indexOf(this);
        }
        public List<Token> getTokens() {
          return tokens;
        }
        public void setTokens(List<Token> tokens) {
          this.tokens = tokens;
        }
        @Override
        public String toString() {
          return "Row{" +
              "index=" + getIndex() +
              ", tokens=" + (tokens == null ? null : tokens) +
              '}';
        }
      }
    }
    public Iterator<Column.Row[]> permutationIterator() {
      return new Iterator<Column.Row[]>() {
        private int[] columnRowCounters = new int[columns.size()];
        public void remove() {
          throw new IllegalStateException("not implemented");
        }
        public boolean hasNext() {
          int s = columnRowCounters.length;
          int n = columns.size();
          return s != 0 && n >= s && columnRowCounters[s - 1] < (columns.get(s - 1)).getRows().size();
        }
        public Column.Row[] next() {
          if (!hasNext()) {
            throw new NoSuchElementException("no more elements");
          }
          Column.Row[] rows = new Column.Row[columnRowCounters.length];
          for (int i = 0; i < columnRowCounters.length; i++) {
            rows[i] = columns.get(i).rows.get(columnRowCounters[i]);
          }
          incrementColumnRowCounters();
          return rows;
        }
        private void incrementColumnRowCounters() {
          for (int i = 0; i < columnRowCounters.length; i++) {
            columnRowCounters[i]++;
            if (columnRowCounters[i] == columns.get(i).rows.size() &&
                i < columnRowCounters.length - 1) {
              columnRowCounters[i] = 0;
            } else {
              break;
            }
          }
        }
      };
    }
    @Override
    public String toString() {
      return "Matrix{" +
          "columns=" + columns +
          '}';
    }
  }
  public int getMinimumShingleSize() {
    return minimumShingleSize;
  }
  public void setMinimumShingleSize(int minimumShingleSize) {
    this.minimumShingleSize = minimumShingleSize;
  }
  public int getMaximumShingleSize() {
    return maximumShingleSize;
  }
  public void setMaximumShingleSize(int maximumShingleSize) {
    this.maximumShingleSize = maximumShingleSize;
  }
  public Matrix getMatrix() {
    return matrix;
  }
  public void setMatrix(Matrix matrix) {
    this.matrix = matrix;
  }
  public Character getSpacerCharacter() {
    return spacerCharacter;
  }
  public void setSpacerCharacter(Character spacerCharacter) {
    this.spacerCharacter = spacerCharacter;
  }
  public boolean isIgnoringSinglePrefixOrSuffixShingle() {
    return ignoringSinglePrefixOrSuffixShingle;
  }
  public void setIgnoringSinglePrefixOrSuffixShingle(boolean ignoringSinglePrefixOrSuffixShingle) {
    this.ignoringSinglePrefixOrSuffixShingle = ignoringSinglePrefixOrSuffixShingle;
  }
  public static class OneDimensionalNonWeightedTokenSettingsCodec extends TokenSettingsCodec {
    @Override
    public TokenPositioner getTokenPositioner(Token token) throws IOException {
      return TokenPositioner.newColumn;
    }
    @Override
    public void setTokenPositioner(Token token, TokenPositioner tokenPositioner) {
    }
    @Override
    public float getWeight(Token token) {
      return 1f;
    }
    @Override
    public void setWeight(Token token, float weight) {
    }
  }
  public static class TwoDimensionalNonWeightedSynonymTokenSettingsCodec extends TokenSettingsCodec {
    @Override
    public TokenPositioner getTokenPositioner(Token token) throws IOException {
      if (token.getPositionIncrement() == 0) {
        return TokenPositioner.newRow;
      } else {
        return TokenPositioner.newColumn;
      }
    }
    @Override
    public void setTokenPositioner(Token token, TokenPositioner tokenPositioner) {
      throw new UnsupportedOperationException();
    }
    @Override
    public float getWeight(Token token) {
      return 1f;
    }
    @Override
    public void setWeight(Token token, float weight) {
    }
  }
  public static class SimpleThreeDimensionalTokenSettingsCodec extends TokenSettingsCodec {
    @Override
    public TokenPositioner getTokenPositioner(Token token) throws IOException {
      switch (token.getFlags()) {
        case 0:
          return TokenPositioner.newColumn;
        case 1:
          return TokenPositioner.newRow;
        case 2:
          return TokenPositioner.sameRow;
      }
      throw new IOException("Unknown matrix positioning of token " + token);
    }
    @Override
    public void setTokenPositioner(Token token, TokenPositioner tokenPositioner) {
      token.setFlags(tokenPositioner.getIndex());
    }
    @Override
    public float getWeight(Token token) {
      if (token.getPayload() == null || token.getPayload().getData() == null) {
        return 1f;
      } else {
        return PayloadHelper.decodeFloat(token.getPayload().getData());
      }
    }
    @Override
    public void setWeight(Token token, float weight) {
      if (weight == 1f) {
        token.setPayload(null);
      } else {
        token.setPayload(new Payload(PayloadHelper.encodeFloat(weight)));
      }
    }
  }
}
