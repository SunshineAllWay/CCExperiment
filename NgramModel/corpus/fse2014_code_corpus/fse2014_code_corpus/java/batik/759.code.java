package org.apache.batik.ext.awt.image;
public final class PadMode implements java.io.Serializable {
    public static final int MODE_ZERO_PAD = 1;
    public static final int MODE_REPLICATE = 2;
    public static final int MODE_WRAP = 3;
    public static final PadMode ZERO_PAD = new PadMode(MODE_ZERO_PAD);
    public static final PadMode REPLICATE = new PadMode(MODE_REPLICATE);
    public static final PadMode WRAP = new PadMode(MODE_WRAP);
    public int getMode() {
        return mode;
    }
    private int mode;
    private PadMode(int mode) {
        this.mode = mode;
    }
    private Object readResolve() throws java.io.ObjectStreamException {
        switch(mode){
        case MODE_ZERO_PAD:
            return ZERO_PAD;
        case MODE_REPLICATE:
            return REPLICATE;
        case MODE_WRAP:
            return WRAP;
        default:
            throw new Error("Unknown Pad Mode type");
        }
    }
}
