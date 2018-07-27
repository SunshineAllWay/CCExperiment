package org.apache.lucene.util;
import java.util.Random;
public class TestStringIntern extends LuceneTestCase {
  String[] testStrings;
  String[] internedStrings;
  Random r = newRandom();
  private String randStr(int len) {
    char[] arr = new char[len];
    for (int i=0; i<len; i++) {
      arr[i] = (char)('a' + r.nextInt(26));
    }
    return new String(arr);
  }
  private void makeStrings(int sz) {
    testStrings = new String[sz];
    internedStrings = new String[sz];
    for (int i=0; i<sz; i++) {
      testStrings[i] = randStr(r.nextInt(8)+3);
    }
  }
  public void testStringIntern() throws InterruptedException {
    makeStrings(1024*10);  
    int nThreads = 20;
    final int iter=1000000;
    Thread[] threads = new Thread[nThreads];
    for (int i=0; i<nThreads; i++) {
      final int seed = i;
      threads[i] = new Thread() {
        @Override
        public void run() {
          Random rand = new Random(seed);
          String[] myInterned = new String[testStrings.length];
          for (int j=0; j<iter; j++) {
            int idx = rand.nextInt(testStrings.length);
            String s = testStrings[idx];
            if (rand.nextBoolean()) s = new String(s); 
            String interned = StringHelper.intern(s);
            String prevInterned = myInterned[idx];
            String otherInterned = internedStrings[idx];
            if (otherInterned != null && otherInterned != interned) {
              fail();
            }
            internedStrings[idx] = interned;
            if (prevInterned != null && prevInterned != interned) {
              fail();
            }
            myInterned[idx] = interned;
          }
        }
      };
      threads[i].start();
    }
    for (int i=0; i<nThreads; i++) {
      threads[i].join();
    }
  }
}
