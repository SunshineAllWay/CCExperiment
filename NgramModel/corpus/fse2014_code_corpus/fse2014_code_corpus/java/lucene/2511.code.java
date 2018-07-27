package org.apache.solr.search;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
public class SortedIntDocSet extends DocSetBase {
  protected final int[] docs;
  public SortedIntDocSet(int[] docs) {
    this.docs = docs;
  }
  public SortedIntDocSet(int[] docs, int len) {
    this(shrink(docs,len));
  }
  public int[] getDocs() { return docs; }
  public int size()      { return docs.length; }
  public long memSize() {
    return (docs.length<<2)+8;
  }
  public static int[] zeroInts = new int[0];
  public static SortedIntDocSet zero = new SortedIntDocSet(zeroInts);
  public static int[] shrink(int[] arr, int newSize) {
    if (arr.length == newSize) return arr;
    int[] newArr = new int[newSize];
    System.arraycopy(arr, 0, newArr, 0, newSize);
    return newArr;
  }
  public static int firstNonSorted(int[] arr, int offset, int len) {
    if (len <= 1) return -1;
    int lower = arr[offset];
    int end = offset + len;
    for(int i=offset+1; i<end; i++) {
      int next = arr[i];
      if (next <= lower) {
        for (int j=i-1; j>offset; j--) {
          if (arr[j]<next) return j+1;
        }
        return offset;
      }
      lower = next;
    }
    return -1;
  }
  public static int intersectionSize(int[] smallerSortedList, int[] biggerSortedList) {
    final int a[] = smallerSortedList;
    final int b[] = biggerSortedList;
    int step = (b.length/a.length)+1;
    step = step + step;
    int icount = 0;
    int low = 0;
    int max = b.length-1;
    for (int i=0; i<a.length; i++) {
      int doca = a[i];
      int high = max;
      int probe = low + step;     
      if (probe<high) {
        if (b[probe]>=doca) {
          high=probe;
        } else {
          low=probe+1;
          probe = low + step;
          if (probe<high) {
            if (b[probe]>=doca) {
              high=probe;
            } else {
              low=probe+1;
            }
          }
        }
      }
      while (low <= high) {
        int mid = (low+high) >>> 1;
        int docb = b[mid];
        if (docb < doca) {
          low = mid+1;
        }
        else if (docb > doca) {
          high = mid-1;
        }
        else {
          icount++;
          low = mid+1;  
          break;
        }
      }
    }
    return icount;
  }
  public int intersectionSize(DocSet other) {
    if (!(other instanceof SortedIntDocSet)) {
      int icount = 0;
      for (int i=0; i<docs.length; i++) {
        if (other.exists(docs[i])) icount++;
      }
      return icount;
    }
    int[] otherDocs = ((SortedIntDocSet)other).docs;
    final int[] a = docs.length < otherDocs.length ? docs : otherDocs;
    final int[] b = docs.length < otherDocs.length ? otherDocs : docs;
    if (a.length==0) return 0;
    if ((b.length>>3) >= a.length) {
      return intersectionSize(a,b);
    }
    int icount=0;
    int i=0,j=0;
    int doca=a[i],docb=b[j];
    for(;;) {
      if (doca > docb) {
        if (++j >= b.length) break;
        docb=b[j];
      } else if (doca < docb) {
        if (++i >= a.length) break;
        doca=a[i];
      } else {
        icount++;
        if (++i >= a.length) break;
        doca=a[i];
        if (++j >= b.length) break;
        docb=b[j];
      }
    }
    return icount;
  }
  public static int intersection(int a[], int lena, int b[], int lenb, int[] target) {
    if (lena > lenb) {
      int ti=lena; lena=lenb; lenb=ti;
      int[] ta=a; a=b; b=ta;
    }
    if (lena==0) return 0;
    if ((lenb>>3) >= lena) {
      return intersectionBinarySearch(a, lena, b, lenb, target);
    }
    int icount=0;
    int i=0,j=0;
    int doca=a[i],docb=b[j];
    for(;;) {
      if (doca > docb) {
        if (++j >= lenb) break;
        docb=b[j];
      } else if (doca < docb) {
        if (++i >= lena) break;
        doca=a[i];
      } else {
        target[icount++] = doca;
        if (++i >= lena) break;
        doca=a[i];
        if (++j >= lenb) break;
        docb=b[j];
      }
    }
    return icount;
  }
  protected static int intersectionBinarySearch(int[] a, int lena, int[] b, int lenb, int[] target) {
    int step = (lenb/lena)+1;
    step = step + step;
    int icount = 0;
    int low = 0;
    int max = lenb-1;
    for (int i=0; i<lena; i++) {
      int doca = a[i];
      int high = max;
      int probe = low + step;     
      if (probe<high) {
        if (b[probe]>=doca) {
          high=probe;
        } else {
          low=probe+1;
          probe = low + step;
          if (probe<high) {
            if (b[probe]>=doca) {
              high=probe;
            } else {
              low=probe+1;
            }
          }
        }
      }
      while (low <= high) {
        int mid = (low+high) >>> 1;
        int docb = b[mid];
        if (docb < doca) {
          low = mid+1;
        }
        else if (docb > doca) {
          high = mid-1;
        }
        else {
          target[icount++] = doca;
          low = mid+1;  
          break;
        }
      }
    }
    return icount;
  }
  @Override
  public DocSet intersection(DocSet other) {
    if (!(other instanceof SortedIntDocSet)) {
      int icount = 0;
      int arr[] = new int[docs.length];
      for (int i=0; i<docs.length; i++) {
        int doc = docs[i];
        if (other.exists(doc)) arr[icount++] = doc;
      }
      return new SortedIntDocSet(arr,icount);
    }
    int[] otherDocs = ((SortedIntDocSet)other).docs;
    int maxsz = Math.min(docs.length, otherDocs.length);
    int[] arr = new int[maxsz];
    int sz = intersection(docs, docs.length, otherDocs, otherDocs.length, arr);
    return new SortedIntDocSet(arr,sz);
  }
  protected static int andNotBinarySearch(int a[], int lena, int b[], int lenb, int[] target) {
   int step = (lenb/lena)+1;
    step = step + step;
    int count = 0;
    int low = 0;
    int max = lenb-1;
    outer:
    for (int i=0; i<lena; i++) {
      int doca = a[i];
      int high = max;
      int probe = low + step;     
      if (probe<high) {
        if (b[probe]>=doca) {
          high=probe;
        } else {
          low=probe+1;
          probe = low + step;
          if (probe<high) {
            if (b[probe]>=doca) {
              high=probe;
            } else {
              low=probe+1;
            }
          }
        }
      }
      while (low <= high) {
        int mid = (low+high) >>> 1;
        int docb = b[mid];
        if (docb < doca) {
          low = mid+1;
        }
        else if (docb > doca) {
          high = mid-1;
        }
        else {
          low = mid+1;  
          continue outer;
        }
      }
      target[count++] = doca;
    }
    return count;
  }
  public static int andNot(int a[], int lena, int b[], int lenb, int[] target) {
    if (lena==0) return 0;
    if (lenb==0) {
      System.arraycopy(a,0,target,0,lena);
      return lena;
    }
    if ((lenb>>3) >= lena) {
      return andNotBinarySearch(a, lena, b, lenb, target);
    }
    int count=0;
    int i=0,j=0;
    int doca=a[i],docb=b[j];
    for(;;) {
      if (doca > docb) {
        if (++j >= lenb) break;
        docb=b[j];
      } else if (doca < docb) {
        target[count++] = doca;
        if (++i >= lena) break;
        doca=a[i];
      } else {
        if (++i >= lena) break;
        doca=a[i];
        if (++j >= lenb) break;
        docb=b[j];
      }
    }
    int leftover=lena - i;
    if (leftover > 0) {
      System.arraycopy(a,i,target,count,leftover);
      count += leftover;
    }
    return count;
  }
  @Override
  public DocSet andNot(DocSet other) {
    if (other.size()==0) return this;
    if (!(other instanceof SortedIntDocSet)) {
      int count = 0;
      int arr[] = new int[docs.length];
      for (int i=0; i<docs.length; i++) {
        int doc = docs[i];
        if (!other.exists(doc)) arr[count++] = doc;
      }
      return new SortedIntDocSet(arr,count);
    }
    int[] otherDocs = ((SortedIntDocSet)other).docs;
    int[] arr = new int[docs.length];
    int sz = andNot(docs, docs.length, otherDocs, otherDocs.length, arr);
    return new SortedIntDocSet(arr,sz);
  }
  public boolean exists(int doc) {
    int low = 0;
    int high = docs.length-1;
    while (low <= high) {
      int mid = (low+high) >>> 1;
      int docb = docs[mid];
      if (docb < doc) {
        low = mid+1;
      }
      else if (docb > doc) {
        high = mid-1;
      }
      else {
        return true;
      }
    }
    return false;
  }
  public DocIterator iterator() {
    return new DocIterator() {
      int pos=0;
      public boolean hasNext() {
        return pos < docs.length;
      }
      public Integer next() {
        return nextDoc();
      }
      public void remove() {
        throw new UnsupportedOperationException("The remove  operation is not supported by this Iterator.");
      }
      public int nextDoc() {
        return docs[pos++];
      }
      public float score() {
        return 0.0f;
      }
    };
  }
  @Override
  public OpenBitSet getBits() {
    int maxDoc = size() > 0 ? docs[size()-1] : 0;
    OpenBitSet bs = new OpenBitSet(maxDoc+1);
    for (int doc : docs) {
      bs.fastSet(doc);
    }
    return bs;
  }
  public static int findIndex(int[] arr, int value, int low, int high) {
    while (low <= high) {
      int mid = (low+high) >>> 1;
      int found = arr[mid];
      if (found < value) {
        low = mid+1;
      }
      else if (found > value) {
        high = mid-1;
      }
      else {
        return mid;
      }
    }
    return low;
  }
  @Override
  public Filter getTopFilter() {
    return new Filter() {
      int lastEndIdx = 0;
      @Override
      public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        int offset = 0;
        SolrIndexReader r = (SolrIndexReader)reader;
        while (r.getParent() != null) {
          offset += r.getBase();
          r = r.getParent();
        }
        final int base = offset;
        final int maxDoc = reader.maxDoc();
        final int max = base + maxDoc;   
        int sidx = Math.max(0,lastEndIdx);
        if (sidx > 0 && docs[sidx-1] >= base) {
          sidx = 0;
        }
        if (sidx < docs.length && docs[sidx] < base) {
          sidx = findIndex(docs, base, sidx, docs.length-1);
        }
        final int startIdx = sidx;
        int eidx = Math.min(docs.length, startIdx + maxDoc) - 1;
        eidx = findIndex(docs, max, startIdx, eidx) - 1;
        final int endIdx = eidx;
        lastEndIdx = endIdx;
        return new DocIdSet() {
          public DocIdSetIterator iterator() throws IOException {
            return new DocIdSetIterator() {
              int idx = startIdx;
              int adjustedDoc = -1;
              public int doc() {
                return adjustedDoc;
              }
              @Override
              public int docID() {
                return adjustedDoc;
              }
              @Override
              public int nextDoc() throws IOException {
                return adjustedDoc = (idx > endIdx) ? NO_MORE_DOCS : (docs[idx++] - base);
              }
              @Override
              public int advance(int target) throws IOException {
                if (idx > endIdx || target==NO_MORE_DOCS) return adjustedDoc=NO_MORE_DOCS;
                target += base;
                int rawDoc = docs[idx++];
                if (rawDoc >= target) return adjustedDoc=rawDoc-base;
                int high = endIdx;
                while (idx <= high) {
                  int mid = (idx+high) >>> 1;
                  rawDoc = docs[mid];
                  if (rawDoc < target) {
                    idx = mid+1;
                  }
                  else if (rawDoc > target) {
                    high = mid-1;
                  }
                  else {
                    idx=mid+1;
                    return adjustedDoc=rawDoc - base;
                  }
                }
                if (idx <= endIdx) {
                  return adjustedDoc = docs[idx++] - base;
                } else {
                  return adjustedDoc=NO_MORE_DOCS;
                }
              }
            };
          }
          @Override
          public boolean isCacheable() {
            return true;
          }
        };
      }
    };
  }
}
