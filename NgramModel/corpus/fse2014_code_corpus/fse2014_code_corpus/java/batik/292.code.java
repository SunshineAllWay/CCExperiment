package org.apache.batik.css.engine;
import org.apache.batik.css.engine.value.Value;
public class StyleDeclaration {
    protected static final int INITIAL_LENGTH = 8;
    protected Value[] values = new Value[INITIAL_LENGTH];
    protected int[] indexes = new int[INITIAL_LENGTH];
    protected boolean[] priorities = new boolean[INITIAL_LENGTH];
    protected int count;
    public int size() {
        return count;
    }
    public Value getValue(int idx) {
        return values[idx];
    }
    public int getIndex(int idx) {
        return indexes[idx];
    }
    public boolean getPriority(int idx) {
        return priorities[idx];
    }
    public void remove(int idx) {
        count--;
        int from  = idx+1;
        int to    = idx;
        int nCopy = count - idx;
        System.arraycopy( values,     from, values,     to, nCopy );
        System.arraycopy( indexes,    from, indexes,    to, nCopy );
        System.arraycopy( priorities, from, priorities, to, nCopy );
        values[ count ] = null;
        indexes[ count ] = 0;
        priorities[ count ] = false;
    }
    public void put(int idx, Value v, int i, boolean prio) {
        values[idx]     = v;
        indexes[idx]    = i;
        priorities[idx] = prio;
    }
    public void append(Value v, int idx, boolean prio) {
        if (values.length == count) {
            Value[]   newval  = new Value[count * 2];
            int[]     newidx  = new int[count * 2];
            boolean[] newprio = new boolean[count * 2];
            System.arraycopy( values, 0, newval, 0, count );
            System.arraycopy( indexes, 0, newidx, 0, count );
            System.arraycopy( priorities, 0, newprio, 0, count );
            values     = newval;
            indexes    = newidx;
            priorities = newprio;
        }
        for (int i = 0; i < count; i++) {
            if (indexes[i] == idx) {
                if (prio || (priorities[i] == prio)) {
                    values    [i] = v;
                    priorities[i] = prio;
                }
                return;
            }
        }
        values    [count] = v;
        indexes   [count] = idx;
        priorities[count] = prio;
        count++;
    }
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer( count * 8 );
        for (int i = 0; i < count; i++) {
            sb.append(eng.getPropertyName(indexes[i]));
            sb.append(": ");
            sb.append(values[i]);
            sb.append(";\n");
        }
        return sb.toString();
    }
}
