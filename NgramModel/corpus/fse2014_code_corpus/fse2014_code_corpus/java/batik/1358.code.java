package org.apache.batik.transcoder.wmf.tosvg;
public class GdiObject {
    GdiObject( int _id, boolean _used ) {
        id = _id;
        used = _used;
        type = 0;
    }
    public void clear() {
        used = false;
        type = 0;
    }
    public void Setup( int _type, Object _obj ) {
        obj = _obj;
        type = _type;
        used = true;
    }
    public boolean isUsed() {
        return used;
    }
    public int getType() {
        return type;
    }
    public Object getObject() {
        return obj;
    }
    public int getID() {
        return id;
    }
    int id;
    boolean used;
    Object obj;
    int type = 0;
}
