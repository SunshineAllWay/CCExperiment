package org.apache.lucene.util;
import java.util.Random;
public class TestPriorityQueue extends LuceneTestCase {
    public TestPriorityQueue(String name) {
        super(name);
    }
    private static class IntegerQueue extends PriorityQueue<Integer> {
        public IntegerQueue(int count) {
            super();
            initialize(count);
        }
        @Override
        protected boolean lessThan(Integer a, Integer b) {
            return (a < b);
        }
    }
    public void testPQ() throws Exception {
        testPQ(10000, newRandom());
    }
    public static void testPQ(int count, Random gen) {
        PriorityQueue<Integer> pq = new IntegerQueue(count);
        int sum = 0, sum2 = 0;
        for (int i = 0; i < count; i++)
        {
            int next = gen.nextInt();
            sum += next;
            pq.add(next);
        }
        int last = Integer.MIN_VALUE;
        for (int i = 0; i < count; i++)
        {
            Integer next = pq.pop();
            assertTrue(next.intValue() >= last);
            last = next.intValue();
            sum2 += last;
        }
        assertEquals(sum, sum2);
    }
    public void testClear() {
        PriorityQueue<Integer> pq = new IntegerQueue(3);
        pq.add(2);
        pq.add(3);
        pq.add(1);
        assertEquals(3, pq.size());
        pq.clear();
        assertEquals(0, pq.size());
    }
    public void testFixedSize() {
        PriorityQueue<Integer> pq = new IntegerQueue(3);
        pq.insertWithOverflow(2);
        pq.insertWithOverflow(3);
        pq.insertWithOverflow(1);
        pq.insertWithOverflow(5);
        pq.insertWithOverflow(7);
        pq.insertWithOverflow(1);
        assertEquals(3, pq.size());
        assertEquals((Integer) 3, pq.top());
    }
    public void testInsertWithOverflow() {
      int size = 4;
      PriorityQueue<Integer> pq = new IntegerQueue(size);
      Integer i1 = 2;
      Integer i2 = 3;
      Integer i3 = 1;
      Integer i4 = 5;
      Integer i5 = 7;
      Integer i6 = 1;
      assertNull(pq.insertWithOverflow(i1));
      assertNull(pq.insertWithOverflow(i2));
      assertNull(pq.insertWithOverflow(i3));
      assertNull(pq.insertWithOverflow(i4));
      assertTrue(pq.insertWithOverflow(i5) == i3); 
      assertTrue(pq.insertWithOverflow(i6) == i6); 
      assertEquals(size, pq.size());
      assertEquals((Integer) 2, pq.top());
    }
}
