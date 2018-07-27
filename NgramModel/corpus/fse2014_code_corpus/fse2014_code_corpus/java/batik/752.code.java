package org.apache.batik.ext.awt.image;
public class DiscreteTransfer implements TransferFunction {
    public byte [] lutData;
    public int [] tableValues;
    private int n;
    public DiscreteTransfer(int [] tableValues){
        this.tableValues = tableValues;
        this.n = tableValues.length;
    }
    private void buildLutData(){
        lutData = new byte [256];
        int i, j;
        for (j=0; j<=255; j++){
            i = (int)(Math.floor(j*n/255f));
            if(i == n){
                i = n-1;
            }
            lutData[j] = (byte)(tableValues[i] & 0xff);
        }
    }
    public byte [] getLookupTable(){
        buildLutData();
        return lutData;
    }
}
