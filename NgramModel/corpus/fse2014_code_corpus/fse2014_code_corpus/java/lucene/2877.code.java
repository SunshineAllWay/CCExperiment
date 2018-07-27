package org.apache.solr.util;
import java.util.Random;
import java.util.BitSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetIterator;
public class BitSetPerf {
  static Random rand = new Random(0);
  static void randomSets(int maxSize, int bitsToSet, BitSet target1, OpenBitSet target2) {
    for (int i=0; i<bitsToSet; i++) {
      int idx;
      do {
        idx = rand.nextInt(maxSize);
      } while (target2.getAndSet(idx));
      target1.set(idx);
    }
  }
  public static void main(String[] args) {
    if (args.length<5) {
      System.out.println("BitSetTest <bitSetSize> <numSets> <numBitsSet> <testName> <iter> <impl>");
      System.out.println("  impl => open for OpenBitSet");
    }
    int bitSetSize = Integer.parseInt(args[0]);
    int numSets = Integer.parseInt(args[1]);
    int numBitsSet = Integer.parseInt(args[2]);
    String test = args[3];
    int iter = Integer.parseInt(args[4]);
    String impl = args.length>5 ? args[5].intern() : "bit";
    BitSet[] sets = new BitSet[numSets];
    OpenBitSet[] osets = new OpenBitSet[numSets];
    for (int i=0; i<numSets; i++) {
      sets[i] = new BitSet(bitSetSize);
      osets[i] = new OpenBitSet(bitSetSize);
      randomSets(bitSetSize, numBitsSet, sets[i], osets[i]);
    }
    BitSet bs = new BitSet(bitSetSize);
    OpenBitSet obs = new OpenBitSet(bitSetSize);
    randomSets(bitSetSize, numBitsSet, bs, obs);
    int ret=0;
    long start = System.currentTimeMillis();
    if ("union".equals(test)) {
      for (int it=0; it<iter; it++) {
        for (int i=0; i<numSets; i++) {
          if (impl=="open") {
            OpenBitSet other=osets[i];
            obs.union(other);
          } else {
            BitSet other=sets[i];
            bs.or(other);
          }
        }
      }
    }
    if ("cardinality".equals(test)) {
      for (int it=0; it<iter; it++) {
        for (int i=0; i<numSets; i++) {
          if (impl=="open") {
            ret += osets[i].cardinality();
          } else {
            ret += sets[i].cardinality();
          }
        }
      }
    }
    if ("get".equals(test)) {
      for (int it=0; it<iter; it++) {
        for (int i=0; i<numSets; i++) {
          if (impl=="open") {
            OpenBitSet oset = osets[i];
            for (int k=0; k<bitSetSize; k++) if (oset.fastGet(k)) ret++;
          } else {
            BitSet bset = sets[i];
            for (int k=0; k<bitSetSize; k++) if (bset.get(k)) ret++;
          }
        }
      }
    }
    if ("icount".equals(test)) {
      for (int it=0; it<iter; it++) {
        for (int i=0; i<numSets-1; i++) {
          if (impl=="open") {
            OpenBitSet a=osets[i];
            OpenBitSet b=osets[i+1];
            ret += OpenBitSet.intersectionCount(a,b);
          } else {
            BitSet a=sets[i];
            BitSet b=sets[i+1];
            BitSet newset = (BitSet)a.clone();
            newset.and(b);
            ret += newset.cardinality();
          }
        }
      }
    }
    if ("clone".equals(test)) {
      for (int it=0; it<iter; it++) {
        for (int i=0; i<numSets; i++) {
          if (impl=="open") {
            osets[i] = (OpenBitSet)osets[i].clone();
          } else {
            sets[i] = (BitSet)sets[i].clone();
          }
        }
      }
    }
    if ("nextSetBit".equals(test)) {
      for (int it=0; it<iter; it++) {
        for (int i=0; i<numSets; i++) {
          if (impl=="open") {
            final OpenBitSet set = osets[i];
            for(int next=set.nextSetBit(0); next>=0; next=set.nextSetBit(next+1)) {
              ret += next;
            }
          } else {
            final BitSet set = sets[i];
            for(int next=set.nextSetBit(0); next>=0; next=set.nextSetBit(next+1)) {
              ret += next;
            }
          }
        }
      }
    }
    if ("iterator".equals(test)) {
      for (int it=0; it<iter; it++) {
        for (int i=0; i<numSets; i++) {
          if (impl=="open") {
            final OpenBitSet set = osets[i];
            final OpenBitSetIterator iterator = new OpenBitSetIterator(set);
            for(int next=iterator.nextDoc(); next>=0; next=iterator.nextDoc()) {
              ret += next;
            }
          } else {
            final BitSet set = sets[i];
            for(int next=set.nextSetBit(0); next>=0; next=set.nextSetBit(next+1)) {
              ret += next;
            }
          }
        }
      }
    }
    long end = System.currentTimeMillis();
    System.out.println("ret="+ret);
    System.out.println("TIME="+(end-start));
  }
}
