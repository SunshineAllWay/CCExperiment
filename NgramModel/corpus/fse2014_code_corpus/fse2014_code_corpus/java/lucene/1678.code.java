package org.apache.lucene.search;
import java.io.IOException;
import java.util.LinkedList;
import org.apache.lucene.analysis.NumericTokenStream; 
import org.apache.lucene.document.NumericField; 
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
public final class NumericRangeQuery<T extends Number> extends MultiTermQuery {
  private NumericRangeQuery(final String field, final int precisionStep, final int valSize,
    T min, T max, final boolean minInclusive, final boolean maxInclusive
  ) {
    assert (valSize == 32 || valSize == 64);
    if (precisionStep < 1)
      throw new IllegalArgumentException("precisionStep must be >=1");
    this.field = StringHelper.intern(field);
    this.precisionStep = precisionStep;
    this.valSize = valSize;
    this.min = min;
    this.max = max;
    this.minInclusive = minInclusive;
    this.maxInclusive = maxInclusive;
    switch (valSize) {
      case 64:
        setRewriteMethod( (precisionStep > 6) ?
          CONSTANT_SCORE_FILTER_REWRITE : 
          CONSTANT_SCORE_AUTO_REWRITE_DEFAULT
        );
        break;
      case 32:
        setRewriteMethod( (precisionStep > 8) ?
          CONSTANT_SCORE_FILTER_REWRITE : 
          CONSTANT_SCORE_AUTO_REWRITE_DEFAULT
        );
        break;
      default:
        throw new IllegalArgumentException("valSize must be 32 or 64");
    }
    if (min != null && min.equals(max)) {
      setRewriteMethod(CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE);
    }
  }
  public static NumericRangeQuery<Long> newLongRange(final String field, final int precisionStep,
    Long min, Long max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Long>(field, precisionStep, 64, min, max, minInclusive, maxInclusive);
  }
  public static NumericRangeQuery<Long> newLongRange(final String field,
    Long min, Long max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Long>(field, NumericUtils.PRECISION_STEP_DEFAULT, 64, min, max, minInclusive, maxInclusive);
  }
  public static NumericRangeQuery<Integer> newIntRange(final String field, final int precisionStep,
    Integer min, Integer max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Integer>(field, precisionStep, 32, min, max, minInclusive, maxInclusive);
  }
  public static NumericRangeQuery<Integer> newIntRange(final String field,
    Integer min, Integer max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Integer>(field, NumericUtils.PRECISION_STEP_DEFAULT, 32, min, max, minInclusive, maxInclusive);
  }
  public static NumericRangeQuery<Double> newDoubleRange(final String field, final int precisionStep,
    Double min, Double max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Double>(field, precisionStep, 64, min, max, minInclusive, maxInclusive);
  }
  public static NumericRangeQuery<Double> newDoubleRange(final String field,
    Double min, Double max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Double>(field, NumericUtils.PRECISION_STEP_DEFAULT, 64, min, max, minInclusive, maxInclusive);
  }
  public static NumericRangeQuery<Float> newFloatRange(final String field, final int precisionStep,
    Float min, Float max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Float>(field, precisionStep, 32, min, max, minInclusive, maxInclusive);
  }
  public static NumericRangeQuery<Float> newFloatRange(final String field,
    Float min, Float max, final boolean minInclusive, final boolean maxInclusive
  ) {
    return new NumericRangeQuery<Float>(field, NumericUtils.PRECISION_STEP_DEFAULT, 32, min, max, minInclusive, maxInclusive);
  }
  @Override
  protected FilteredTermEnum getEnum(final IndexReader reader) throws IOException {
    return new NumericRangeTermEnum(reader);
  }
  public String getField() { return field; }
  public boolean includesMin() { return minInclusive; }
  public boolean includesMax() { return maxInclusive; }
  public T getMin() { return min; }
  public T getMax() { return max; }
  @Override
  public String toString(final String field) {
    final StringBuilder sb = new StringBuilder();
    if (!this.field.equals(field)) sb.append(this.field).append(':');
    return sb.append(minInclusive ? '[' : '{')
      .append((min == null) ? "*" : min.toString())
      .append(" TO ")
      .append((max == null) ? "*" : max.toString())
      .append(maxInclusive ? ']' : '}')
      .append(ToStringUtils.boost(getBoost()))
      .toString();
  }
  @Override
  public final boolean equals(final Object o) {
    if (o==this) return true;
    if (!super.equals(o))
      return false;
    if (o instanceof NumericRangeQuery) {
      final NumericRangeQuery q=(NumericRangeQuery)o;
      return (
        field==q.field &&
        (q.min == null ? min == null : q.min.equals(min)) &&
        (q.max == null ? max == null : q.max.equals(max)) &&
        minInclusive == q.minInclusive &&
        maxInclusive == q.maxInclusive &&
        precisionStep == q.precisionStep
      );
    }
    return false;
  }
  @Override
  public final int hashCode() {
    int hash = super.hashCode();
    hash += field.hashCode()^0x4565fd66 + precisionStep^0x64365465;
    if (min != null) hash += min.hashCode()^0x14fa55fb;
    if (max != null) hash += max.hashCode()^0x733fa5fe;
    return hash +
      (Boolean.valueOf(minInclusive).hashCode()^0x14fa55fb)+
      (Boolean.valueOf(maxInclusive).hashCode()^0x733fa5fe);
  }
  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
    field = StringHelper.intern(field);
  }
  String field;
  final int precisionStep, valSize;
  final T min, max;
  final boolean minInclusive,maxInclusive;
  private final class NumericRangeTermEnum extends FilteredTermEnum {
    private final IndexReader reader;
    private final LinkedList<String> rangeBounds = new LinkedList<String>();
    private final Term termTemplate = new Term(field);
    private String currentUpperBound = null;
    NumericRangeTermEnum(final IndexReader reader) throws IOException {
      this.reader = reader;
      switch (valSize) {
        case 64: {
          long minBound = Long.MIN_VALUE;
          if (min instanceof Long) {
            minBound = min.longValue();
          } else if (min instanceof Double) {
            minBound = NumericUtils.doubleToSortableLong(min.doubleValue());
          }
          if (!minInclusive && min != null) {
            if (minBound == Long.MAX_VALUE) break;
            minBound++;
          }
          long maxBound = Long.MAX_VALUE;
          if (max instanceof Long) {
            maxBound = max.longValue();
          } else if (max instanceof Double) {
            maxBound = NumericUtils.doubleToSortableLong(max.doubleValue());
          }
          if (!maxInclusive && max != null) {
            if (maxBound == Long.MIN_VALUE) break;
            maxBound--;
          }
          NumericUtils.splitLongRange(new NumericUtils.LongRangeBuilder() {
            @Override
            public final void addRange(String minPrefixCoded, String maxPrefixCoded) {
              rangeBounds.add(minPrefixCoded);
              rangeBounds.add(maxPrefixCoded);
            }
          }, precisionStep, minBound, maxBound);
          break;
        }
        case 32: {
          int minBound = Integer.MIN_VALUE;
          if (min instanceof Integer) {
            minBound = min.intValue();
          } else if (min instanceof Float) {
            minBound = NumericUtils.floatToSortableInt(min.floatValue());
          }
          if (!minInclusive && min != null) {
            if (minBound == Integer.MAX_VALUE) break;
            minBound++;
          }
          int maxBound = Integer.MAX_VALUE;
          if (max instanceof Integer) {
            maxBound = max.intValue();
          } else if (max instanceof Float) {
            maxBound = NumericUtils.floatToSortableInt(max.floatValue());
          }
          if (!maxInclusive && max != null) {
            if (maxBound == Integer.MIN_VALUE) break;
            maxBound--;
          }
          NumericUtils.splitIntRange(new NumericUtils.IntRangeBuilder() {
            @Override
            public final void addRange(String minPrefixCoded, String maxPrefixCoded) {
              rangeBounds.add(minPrefixCoded);
              rangeBounds.add(maxPrefixCoded);
            }
          }, precisionStep, minBound, maxBound);
          break;
        }
        default:
          throw new IllegalArgumentException("valSize must be 32 or 64");
      }
      next();
    }
    @Override
    public float difference() {
      return 1.0f;
    }
    @Override
    protected boolean endEnum() {
      throw new UnsupportedOperationException("not implemented");
    }
    @Override
    protected void setEnum(TermEnum tenum) {
      throw new UnsupportedOperationException("not implemented");
    }
    @Override
    protected boolean termCompare(Term term) {
      return (term.field() == field && term.text().compareTo(currentUpperBound) <= 0);
    }
    @Override
    public boolean next() throws IOException {
      if (currentTerm != null) {
        assert actualEnum != null;
        if (actualEnum.next()) {
          currentTerm = actualEnum.term();
          if (termCompare(currentTerm))
            return true;
        }
      }
      currentTerm = null;
      while (rangeBounds.size() >= 2) {
        assert rangeBounds.size() % 2 == 0;
        if (actualEnum != null) {
          actualEnum.close();
          actualEnum = null;
        }
        final String lowerBound = rangeBounds.removeFirst();
        this.currentUpperBound = rangeBounds.removeFirst();
        actualEnum = reader.terms(termTemplate.createTerm(lowerBound));
        currentTerm = actualEnum.term();
        if (currentTerm != null && termCompare(currentTerm))
          return true;
        currentTerm = null;
      }
      assert rangeBounds.size() == 0 && currentTerm == null;
      return false;
    }
    @Override
    public void close() throws IOException {
      rangeBounds.clear();
      currentUpperBound = null;
      super.close();
    }
  }
}
