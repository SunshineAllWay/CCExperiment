package org.apache.lucene.util;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
public class TestBitVector extends LuceneTestCase
{
    public TestBitVector(String s) {
        super(s);
    }
    public void testConstructSize() throws Exception {
        doTestConstructOfSize(8);
        doTestConstructOfSize(20);
        doTestConstructOfSize(100);
        doTestConstructOfSize(1000);
    }
    private void doTestConstructOfSize(int n) {
        BitVector bv = new BitVector(n);
        assertEquals(n,bv.size());
    }
    public void testGetSet() throws Exception {
        doTestGetSetVectorOfSize(8);
        doTestGetSetVectorOfSize(20);
        doTestGetSetVectorOfSize(100);
        doTestGetSetVectorOfSize(1000);
    }
    private void doTestGetSetVectorOfSize(int n) {
        BitVector bv = new BitVector(n);
        for(int i=0;i<bv.size();i++) {
            assertFalse(bv.get(i));
            bv.set(i);
            assertTrue(bv.get(i));
        }
    }
    public void testClear() throws Exception {
        doTestClearVectorOfSize(8);
        doTestClearVectorOfSize(20);
        doTestClearVectorOfSize(100);
        doTestClearVectorOfSize(1000);
    }
    private void doTestClearVectorOfSize(int n) {
        BitVector bv = new BitVector(n);
        for(int i=0;i<bv.size();i++) {
            assertFalse(bv.get(i));
            bv.set(i);
            assertTrue(bv.get(i));
            bv.clear(i);
            assertFalse(bv.get(i));
        }
    }
    public void testCount() throws Exception {
        doTestCountVectorOfSize(8);
        doTestCountVectorOfSize(20);
        doTestCountVectorOfSize(100);
        doTestCountVectorOfSize(1000);
    }
    private void doTestCountVectorOfSize(int n) {
        BitVector bv = new BitVector(n);
        for(int i=0;i<bv.size();i++) {
            assertFalse(bv.get(i));
            assertEquals(i,bv.count());
            bv.set(i);
            assertTrue(bv.get(i));
            assertEquals(i+1,bv.count());
        }
        bv = new BitVector(n);
        for(int i=0;i<bv.size();i++) {
            assertFalse(bv.get(i));
            assertEquals(0,bv.count());
            bv.set(i);
            assertTrue(bv.get(i));
            assertEquals(1,bv.count());
            bv.clear(i);
            assertFalse(bv.get(i));
            assertEquals(0,bv.count());
        }
    }
    public void testWriteRead() throws Exception {
        doTestWriteRead(8);
        doTestWriteRead(20);
        doTestWriteRead(100);
        doTestWriteRead(1000);
    }
    private void doTestWriteRead(int n) throws Exception {
        Directory d = new  RAMDirectory();
        BitVector bv = new BitVector(n);
        for(int i=0;i<bv.size();i++) {
            assertFalse(bv.get(i));
            assertEquals(i,bv.count());
            bv.set(i);
            assertTrue(bv.get(i));
            assertEquals(i+1,bv.count());
            bv.write(d, "TESTBV");
            BitVector compare = new BitVector(d, "TESTBV");
            assertTrue(doCompare(bv,compare));
        }
    }
    public void testDgaps() throws IOException {
      doTestDgaps(1,0,1);
      doTestDgaps(10,0,1);
      doTestDgaps(100,0,1);
      doTestDgaps(1000,4,7);
      doTestDgaps(10000,40,43);
      doTestDgaps(100000,415,418);
      doTestDgaps(1000000,3123,3126);
    }
    private void doTestDgaps(int size, int count1, int count2) throws IOException {
      Directory d = new  RAMDirectory();
      BitVector bv = new BitVector(size);
      for (int i=0; i<count1; i++) {
        bv.set(i);
        assertEquals(i+1,bv.count());
      }
      bv.write(d, "TESTBV");
      for (int i=count1; i<count2; i++) {
        BitVector bv2 = new BitVector(d, "TESTBV");
        assertTrue(doCompare(bv,bv2));
        bv = bv2;
        bv.set(i);
        assertEquals(i+1,bv.count());
        bv.write(d, "TESTBV");
      }
      for (int i=count2-1; i>=count1; i--) {
        BitVector bv2 = new BitVector(d, "TESTBV");
        assertTrue(doCompare(bv,bv2));
        bv = bv2;
        bv.clear(i);
        assertEquals(i,bv.count());
        bv.write(d, "TESTBV");
      }
    }
    private boolean doCompare(BitVector bv, BitVector compare) {
        boolean equal = true;
        for(int i=0;i<bv.size();i++) {
            if(bv.get(i)!=compare.get(i)) {
                equal = false;
                break;
            }
        }
        return equal;
    }
    private static int[] subsetPattern = new int[] { 1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1 };
    public void testSubset() {
    	doTestSubset(0, 0);
    	doTestSubset(0, 20);
    	doTestSubset(0, 7);
    	doTestSubset(0, 8);
    	doTestSubset(0, 9);
    	doTestSubset(0, 15);
    	doTestSubset(0, 16);
    	doTestSubset(0, 17);
    	doTestSubset(1, 7);
    	doTestSubset(1, 8);
    	doTestSubset(1, 9);
    	doTestSubset(1, 15);
    	doTestSubset(1, 16);
    	doTestSubset(1, 17);
    	doTestSubset(2, 20);
    	doTestSubset(3, 20);
    	doTestSubset(4, 20);
    	doTestSubset(5, 20);
    	doTestSubset(6, 20);
    	doTestSubset(7, 14);
    	doTestSubset(7, 15);
    	doTestSubset(7, 16);
    	doTestSubset(8, 15);
    	doTestSubset(9, 20);
    	doTestSubset(10, 20);
    	doTestSubset(11, 20);
    	doTestSubset(12, 20);
    	doTestSubset(13, 20);
    }
    private void doTestSubset(int start, int end) {
    	BitVector full = createSubsetTestVector();
    	BitVector subset = full.subset(start, end);
    	assertEquals(end - start, subset.size());
    	int count = 0;
    	for (int i = start, j = 0; i < end; i++, j++) {
    		if (subsetPattern[i] == 1) {
    			count++;
    			assertTrue(subset.get(j));
    		} else {
    			assertFalse(subset.get(j));
    		}
    	}
    	assertEquals(count, subset.count());
    }
    private BitVector createSubsetTestVector() {
    	BitVector bv = new BitVector(subsetPattern.length);
    	for (int i = 0; i < subsetPattern.length; i++) {
    		if (subsetPattern[i] == 1) {
    			bv.set(i);
    		}
    	}
    	return bv;
    }
}
