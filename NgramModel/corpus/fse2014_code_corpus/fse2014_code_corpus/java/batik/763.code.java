package org.apache.batik.ext.awt.image;
public class TableTransfer implements TransferFunction {
    public byte [] lutData;
    public int [] tableValues;
    private int n;
    public TableTransfer(int [] tableValues){
        this.tableValues = tableValues;
        this.n = tableValues.length;
    }
    private void buildLutData(){
        lutData = new byte [256];
        int j;
        float fi, r;
        int ffi, cfi;
        for (j=0; j<=255; j++){
            fi = j*(n-1)/255f;
            ffi = (int)Math.floor(fi);
            cfi = (ffi + 1)>(n-1)?(n-1):(ffi+1);
            r = fi - ffi;
            lutData[j] = (byte)((int)((tableValues[ffi] + r*(tableValues[cfi] - tableValues[ffi])))&0xff);
        }
    }
    public byte [] getLookupTable(){
        buildLutData();
        return lutData;
    }
}
