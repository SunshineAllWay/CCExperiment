package org.apache.batik.parser;
import java.util.Iterator;
import java.util.LinkedList;
public class FloatArrayProducer
        extends DefaultNumberListHandler
        implements PointsHandler {
    protected LinkedList as;
    protected float[] a;
    protected int index;
    protected int count;
    public float[] getFloatArray() {
        return a;
    }
    public void startNumberList() throws ParseException {
        as = new LinkedList();
        a = new float[11];
        count = 0;
        index = 0;
    }
    public void numberValue(float v) throws ParseException {
        if (index == a.length) {
            as.add(a);
            a = new float[a.length * 2 + 1];
            index = 0;
        }
        a[index++] = v;
        count++;
    }
    public void endNumberList() throws ParseException {
        float[] all = new float[count];
        int pos = 0;
        Iterator it = as.iterator();
        while (it.hasNext()) {
            float[] b = (float[]) it.next();
            System.arraycopy(b, 0, all, pos, b.length);
            pos += b.length;
        }
        System.arraycopy(a, 0, all, pos, index);
        as.clear();
        a = all;
    }
    public void startPoints() throws ParseException {
        startNumberList();
    }
    public void point(float x, float y) throws ParseException {
        numberValue(x);
        numberValue(y);
    }
    public void endPoints() throws ParseException {
        endNumberList();
    }
}
