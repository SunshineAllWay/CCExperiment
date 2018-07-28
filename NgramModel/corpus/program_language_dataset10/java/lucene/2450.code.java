package org.apache.solr.schema;
import java.io.IOException;
import java.util.Map;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.SolrIndexReader;
public class RandomSortField extends FieldType {
  private static int hash(int key) {
    key = ~key + (key << 15); 
    key = key ^ (key >>> 12);
    key = key + (key << 2);
    key = key ^ (key >>> 4);
    key = key * 2057; 
    key = key ^ (key >>> 16);
    return key >>> 1; 
  }
  private static int getSeed(String fieldName, IndexReader r) {
    SolrIndexReader top = (SolrIndexReader)r;
    int base=0;
    while (top.getParent() != null) {
      base += top.getBase();
      top = top.getParent();
    }
    return fieldName.hashCode() + base + (int)top.getVersion();
  }
  @Override
  public SortField getSortField(SchemaField field, boolean reverse) {
    return new SortField(field.getName(), randomComparatorSource, reverse);
  }
  @Override
  public ValueSource getValueSource(SchemaField field) {
    return new RandomValueSource(field.getName());
  }
  @Override
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException { }
  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException { }
  private static FieldComparatorSource randomComparatorSource = new FieldComparatorSource() {
    public FieldComparator newComparator(final String fieldname, final int numHits, int sortPos, boolean reversed) throws IOException {
      return new FieldComparator() {
        int seed;
        private final int[] values = new int[numHits];
        int bottomVal;
        public int compare(int slot1, int slot2) {
          return values[slot1] - values[slot2];  
        }
        public void setBottom(int slot) {
          bottomVal = values[slot];
        }
        public int compareBottom(int doc) throws IOException {
          return bottomVal - hash(doc+seed);
        }
        public void copy(int slot, int doc) throws IOException {
          values[slot] = hash(doc+seed);
        }
        public void setNextReader(IndexReader reader, int docBase) throws IOException {
          seed = getSeed(fieldname, reader);
        }
        public Comparable value(int slot) {
          return values[slot];
        }
      };
    }
  };
  public class RandomValueSource extends ValueSource {
    private final String field;
    public RandomValueSource(String field) {
      this.field=field;
    }
    @Override
    public String description() {
      return field;
    }
    @Override
    public DocValues getValues(Map context, final IndexReader reader) throws IOException {
      return new DocValues() {
          private final int seed = getSeed(field, reader);
          @Override
          public float floatVal(int doc) {
            return (float)hash(doc+seed);
          }
          @Override
          public int intVal(int doc) {
            return (int)hash(doc+seed);
          }
          @Override
          public long longVal(int doc) {
            return (long)hash(doc+seed);
          }
          @Override
          public double doubleVal(int doc) {
            return (double)hash(doc+seed);
          }
          @Override
          public String strVal(int doc) {
            return Integer.toString(hash(doc+seed));
          }
          @Override
          public String toString(int doc) {
            return description() + '=' + intVal(doc);
          }
        };
    }
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof RandomValueSource)) return false;
      RandomValueSource other = (RandomValueSource)o;
      return this.field.equals(other.field);
    }
    @Override
    public int hashCode() {
      return field.hashCode();
    };
  }
}
