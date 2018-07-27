package org.apache.lucene.search;
import java.io.IOException;
import java.text.Collator;
import java.util.Locale;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache.DoubleParser;
import org.apache.lucene.search.FieldCache.LongParser;
import org.apache.lucene.search.FieldCache.ByteParser;
import org.apache.lucene.search.FieldCache.FloatParser;
import org.apache.lucene.search.FieldCache.IntParser;
import org.apache.lucene.search.FieldCache.ShortParser;
import org.apache.lucene.search.FieldCache.StringIndex;
public abstract class FieldComparator {
  public abstract int compare(int slot1, int slot2);
  public abstract void setBottom(final int slot);
  public abstract int compareBottom(int doc) throws IOException;
  public abstract void copy(int slot, int doc) throws IOException;
  public abstract void setNextReader(IndexReader reader, int docBase) throws IOException;
  public void setScorer(Scorer scorer) {
  }
  public abstract Comparable<?> value(int slot);
  public static final class ByteComparator extends FieldComparator {
    private final byte[] values;
    private byte[] currentReaderValues;
    private final String field;
    private ByteParser parser;
    private byte bottom;
    ByteComparator(int numHits, String field, FieldCache.Parser parser) {
      values = new byte[numHits];
      this.field = field;
      this.parser = (ByteParser) parser;
    }
    @Override
    public int compare(int slot1, int slot2) {
      return values[slot1] - values[slot2];
    }
    @Override
    public int compareBottom(int doc) {
      return bottom - currentReaderValues[doc];
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getBytes(reader, field, parser);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return Byte.valueOf(values[slot]);
    }
  }
  public static final class DocComparator extends FieldComparator {
    private final int[] docIDs;
    private int docBase;
    private int bottom;
    DocComparator(int numHits) {
      docIDs = new int[numHits];
    }
    @Override
    public int compare(int slot1, int slot2) {
      return docIDs[slot1] - docIDs[slot2];
    }
    @Override
    public int compareBottom(int doc) {
      return bottom - (docBase + doc);
    }
    @Override
    public void copy(int slot, int doc) {
      docIDs[slot] = docBase + doc;
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {
      this.docBase = docBase;
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = docIDs[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return Integer.valueOf(docIDs[slot]);
    }
  }
  public static final class DoubleComparator extends FieldComparator {
    private final double[] values;
    private double[] currentReaderValues;
    private final String field;
    private DoubleParser parser;
    private double bottom;
    DoubleComparator(int numHits, String field, FieldCache.Parser parser) {
      values = new double[numHits];
      this.field = field;
      this.parser = (DoubleParser) parser;
    }
    @Override
    public int compare(int slot1, int slot2) {
      final double v1 = values[slot1];
      final double v2 = values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public int compareBottom(int doc) {
      final double v2 = currentReaderValues[doc];
      if (bottom > v2) {
        return 1;
      } else if (bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getDoubles(reader, field, parser);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return Double.valueOf(values[slot]);
    }
  }
  public static final class FloatComparator extends FieldComparator {
    private final float[] values;
    private float[] currentReaderValues;
    private final String field;
    private FloatParser parser;
    private float bottom;
    FloatComparator(int numHits, String field, FieldCache.Parser parser) {
      values = new float[numHits];
      this.field = field;
      this.parser = (FloatParser) parser;
    }
    @Override
    public int compare(int slot1, int slot2) {
      final float v1 = values[slot1];
      final float v2 = values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public int compareBottom(int doc) {
      final float v2 = currentReaderValues[doc];
      if (bottom > v2) {
        return 1;
      } else if (bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getFloats(reader, field, parser);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return Float.valueOf(values[slot]);
    }
  }
  public static final class IntComparator extends FieldComparator {
    private final int[] values;
    private int[] currentReaderValues;
    private final String field;
    private IntParser parser;
    private int bottom;                           
    IntComparator(int numHits, String field, FieldCache.Parser parser) {
      values = new int[numHits];
      this.field = field;
      this.parser = (IntParser) parser;
    }
    @Override
    public int compare(int slot1, int slot2) {
      final int v1 = values[slot1];
      final int v2 = values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public int compareBottom(int doc) {
      final int v2 = currentReaderValues[doc];
      if (bottom > v2) {
        return 1;
      } else if (bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getInts(reader, field, parser);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return Integer.valueOf(values[slot]);
    }
  }
  public static final class LongComparator extends FieldComparator {
    private final long[] values;
    private long[] currentReaderValues;
    private final String field;
    private LongParser parser;
    private long bottom;
    LongComparator(int numHits, String field, FieldCache.Parser parser) {
      values = new long[numHits];
      this.field = field;
      this.parser = (LongParser) parser;
    }
    @Override
    public int compare(int slot1, int slot2) {
      final long v1 = values[slot1];
      final long v2 = values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public int compareBottom(int doc) {
      final long v2 = currentReaderValues[doc];
      if (bottom > v2) {
        return 1;
      } else if (bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getLongs(reader, field, parser);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return Long.valueOf(values[slot]);
    }
  }
  public static final class RelevanceComparator extends FieldComparator {
    private final float[] scores;
    private float bottom;
    private Scorer scorer;
    RelevanceComparator(int numHits) {
      scores = new float[numHits];
    }
    @Override
    public int compare(int slot1, int slot2) {
      final float score1 = scores[slot1];
      final float score2 = scores[slot2];
      return score1 > score2 ? -1 : (score1 < score2 ? 1 : 0);
    }
    @Override
    public int compareBottom(int doc) throws IOException {
      float score = scorer.score();
      return bottom > score ? -1 : (bottom < score ? 1 : 0);
    }
    @Override
    public void copy(int slot, int doc) throws IOException {
      scores[slot] = scorer.score();
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = scores[bottom];
    }
    @Override
    public void setScorer(Scorer scorer) {
      this.scorer = new ScoreCachingWrappingScorer(scorer);
    }
    @Override
    public Comparable<?> value(int slot) {
      return Float.valueOf(scores[slot]);
    }
  }
  public static final class ShortComparator extends FieldComparator {
    private final short[] values;
    private short[] currentReaderValues;
    private final String field;
    private ShortParser parser;
    private short bottom;
    ShortComparator(int numHits, String field, FieldCache.Parser parser) {
      values = new short[numHits];
      this.field = field;
      this.parser = (ShortParser) parser;
    }
    @Override
    public int compare(int slot1, int slot2) {
      return values[slot1] - values[slot2];
    }
    @Override
    public int compareBottom(int doc) {
      return bottom - currentReaderValues[doc];
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getShorts(reader, field, parser);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return Short.valueOf(values[slot]);
    }
  }
  public static final class StringComparatorLocale extends FieldComparator {
    private final String[] values;
    private String[] currentReaderValues;
    private final String field;
    final Collator collator;
    private String bottom;
    StringComparatorLocale(int numHits, String field, Locale locale) {
      values = new String[numHits];
      this.field = field;
      collator = Collator.getInstance(locale);
    }
    @Override
    public int compare(int slot1, int slot2) {
      final String val1 = values[slot1];
      final String val2 = values[slot2];
      if (val1 == null) {
        if (val2 == null) {
          return 0;
        }
        return -1;
      } else if (val2 == null) {
        return 1;
      }
      return collator.compare(val1, val2);
    }
    @Override
    public int compareBottom(int doc) {
      final String val2 = currentReaderValues[doc];
      if (bottom == null) {
        if (val2 == null) {
          return 0;
        }
        return -1;
      } else if (val2 == null) {
        return 1;
      }
      return collator.compare(bottom, val2);
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return values[slot];
    }
  }
  public static final class StringOrdValComparator extends FieldComparator {
    private final int[] ords;
    private final String[] values;
    private final int[] readerGen;
    private int currentReaderGen = -1;
    private String[] lookup;
    private int[] order;
    private final String field;
    private int bottomSlot = -1;
    private int bottomOrd;
    private String bottomValue;
    private final boolean reversed;
    private final int sortPos;
    public StringOrdValComparator(int numHits, String field, int sortPos, boolean reversed) {
      ords = new int[numHits];
      values = new String[numHits];
      readerGen = new int[numHits];
      this.sortPos = sortPos;
      this.reversed = reversed;
      this.field = field;
    }
    @Override
    public int compare(int slot1, int slot2) {
      if (readerGen[slot1] == readerGen[slot2]) {
        int cmp = ords[slot1] - ords[slot2];
        if (cmp != 0) {
          return cmp;
        }
      }
      final String val1 = values[slot1];
      final String val2 = values[slot2];
      if (val1 == null) {
        if (val2 == null) {
          return 0;
        }
        return -1;
      } else if (val2 == null) {
        return 1;
      }
      return val1.compareTo(val2);
    }
    @Override
    public int compareBottom(int doc) {
      assert bottomSlot != -1;
      int order = this.order[doc];
      final int cmp = bottomOrd - order;
      if (cmp != 0) {
        return cmp;
      }
      final String val2 = lookup[order];
      if (bottomValue == null) {
        if (val2 == null) {
          return 0;
        }
        return -1;
      } else if (val2 == null) {
        return 1;
      }
      return bottomValue.compareTo(val2);
    }
    private void convert(int slot) {
      readerGen[slot] = currentReaderGen;
      int index = 0;
      String value = values[slot];
      if (value == null) {
        ords[slot] = 0;
        return;
      }
      if (sortPos == 0 && bottomSlot != -1 && bottomSlot != slot) {
        assert bottomOrd < lookup.length;
        if (reversed) {
          index = binarySearch(lookup, value, bottomOrd, lookup.length-1);
        } else {
          index = binarySearch(lookup, value, 0, bottomOrd);
        }
      } else {
        index = binarySearch(lookup, value);
      }
      if (index < 0) {
        index = -index - 2;
      }
      ords[slot] = index;
    }
    @Override
    public void copy(int slot, int doc) {
      final int ord = order[doc];
      ords[slot] = ord;
      assert ord >= 0;
      values[slot] = lookup[ord];
      readerGen[slot] = currentReaderGen;
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      StringIndex currentReaderValues = FieldCache.DEFAULT.getStringIndex(reader, field);
      currentReaderGen++;
      order = currentReaderValues.order;
      lookup = currentReaderValues.lookup;
      assert lookup.length > 0;
      if (bottomSlot != -1) {
        convert(bottomSlot);
        bottomOrd = ords[bottomSlot];
      }
    }
    @Override
    public void setBottom(final int bottom) {
      bottomSlot = bottom;
      if (readerGen[bottom] != currentReaderGen) {
        convert(bottomSlot);
      }
      bottomOrd = ords[bottom];
      assert bottomOrd >= 0;
      assert bottomOrd < lookup.length;
      bottomValue = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return values[slot];
    }
    public String[] getValues() {
      return values;
    }
    public int getBottomSlot() {
      return bottomSlot;
    }
    public String getField() {
      return field;
    }
  }
  public static final class StringValComparator extends FieldComparator {
    private String[] values;
    private String[] currentReaderValues;
    private final String field;
    private String bottom;
    StringValComparator(int numHits, String field) {
      values = new String[numHits];
      this.field = field;
    }
    @Override
    public int compare(int slot1, int slot2) {
      final String val1 = values[slot1];
      final String val2 = values[slot2];
      if (val1 == null) {
        if (val2 == null) {
          return 0;
        }
        return -1;
      } else if (val2 == null) {
        return 1;
      }
      return val1.compareTo(val2);
    }
    @Override
    public int compareBottom(int doc) {
      final String val2 = currentReaderValues[doc];
      if (bottom == null) {
        if (val2 == null) {
          return 0;
        }
        return -1;
      } else if (val2 == null) {
        return 1;
      }
      return bottom.compareTo(val2);
    }
    @Override
    public void copy(int slot, int doc) {
      values[slot] = currentReaderValues[doc];
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
    }
    @Override
    public void setBottom(final int bottom) {
      this.bottom = values[bottom];
    }
    @Override
    public Comparable<?> value(int slot) {
      return values[slot];
    }
  }
  final protected static int binarySearch(String[] a, String key) {
    return binarySearch(a, key, 0, a.length-1);
  }
  final protected static int binarySearch(String[] a, String key, int low, int high) {
    while (low <= high) {
      int mid = (low + high) >>> 1;
      String midVal = a[mid];
      int cmp;
      if (midVal != null) {
        cmp = midVal.compareTo(key);
      } else {
        cmp = -1;
      }
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        high = mid - 1;
      else
        return mid;
    }
    return -(low + 1);
  }
}
