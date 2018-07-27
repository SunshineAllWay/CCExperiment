package org.apache.lucene.util;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
public final class BitVector implements Cloneable {
  private byte[] bits;
  private int size;
  private int count;
  public BitVector(int n) {
    size = n;
    bits = new byte[(size >> 3) + 1];
    count = 0;
  }
  BitVector(byte[] bits, int size) {
    this.bits = bits;
    this.size = size;
    count = -1;
  }
  @Override
  public Object clone() {
    byte[] copyBits = new byte[bits.length];
    System.arraycopy(bits, 0, copyBits, 0, bits.length);
    BitVector clone = new BitVector(copyBits, size);
    clone.count = count;
    return clone;
  }
  public final void set(int bit) {
    if (bit >= size) {
      throw new ArrayIndexOutOfBoundsException("bit=" + bit + " size=" + size);
    }
    bits[bit >> 3] |= 1 << (bit & 7);
    count = -1;
  }
  public final boolean getAndSet(int bit) {
    if (bit >= size) {
      throw new ArrayIndexOutOfBoundsException("bit=" + bit + " size=" + size);
    }
    final int pos = bit >> 3;
    final int v = bits[pos];
    final int flag = 1 << (bit & 7);
    if ((flag & v) != 0)
      return true;
    else {
      bits[pos] = (byte) (v | flag);
      if (count != -1)
        count++;
      return false;
    }
  }
  public final void clear(int bit) {
    if (bit >= size) {
      throw new ArrayIndexOutOfBoundsException(bit);
    }
    bits[bit >> 3] &= ~(1 << (bit & 7));
    count = -1;
  }
  public final boolean get(int bit) {
    assert bit >= 0 && bit < size: "bit " + bit + " is out of bounds 0.." + (size-1);
    return (bits[bit >> 3] & (1 << (bit & 7))) != 0;
  }
  public final int size() {
    return size;
  }
  public final int count() {
    if (count == -1) {
      int c = 0;
      int end = bits.length;
      for (int i = 0; i < end; i++)
        c += BYTE_COUNTS[bits[i] & 0xFF];	  
      count = c;
    }
    return count;
  }
  public final int getRecomputedCount() {
    int c = 0;
    int end = bits.length;
    for (int i = 0; i < end; i++)
      c += BYTE_COUNTS[bits[i] & 0xFF];	  
    return c;
  }
  private static final byte[] BYTE_COUNTS = {	  
    0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4,
    1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
    1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
    2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
    1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
    2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
    2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
    3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
    1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
    2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
    2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
    3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
    2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
    3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
    3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
    4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8
  };
  public final void write(Directory d, String name) throws IOException {
    IndexOutput output = d.createOutput(name);
    try {
      if (isSparse()) { 
        writeDgaps(output); 
      } else {
        writeBits(output);
      }
    } finally {
      output.close();
    }
  }
  private void writeBits(IndexOutput output) throws IOException {
    output.writeInt(size());        
    output.writeInt(count());       
    output.writeBytes(bits, bits.length);
  }
  private void writeDgaps(IndexOutput output) throws IOException {
    output.writeInt(-1);            
    output.writeInt(size());        
    output.writeInt(count());       
    int last=0;
    int n = count();
    int m = bits.length;
    for (int i=0; i<m && n>0; i++) {
      if (bits[i]!=0) {
        output.writeVInt(i-last);
        output.writeByte(bits[i]);
        last = i;
        n -= BYTE_COUNTS[bits[i] & 0xFF];
      }
    }
  }
  private boolean isSparse() {
    int factor = 10;  
    if (bits.length < (1<< 7)) return factor * (4 + (8+ 8)*count()) < size();
    if (bits.length < (1<<14)) return factor * (4 + (8+16)*count()) < size();
    if (bits.length < (1<<21)) return factor * (4 + (8+24)*count()) < size();
    if (bits.length < (1<<28)) return factor * (4 + (8+32)*count()) < size();
    return                            factor * (4 + (8+40)*count()) < size();
  }
  public BitVector(Directory d, String name) throws IOException {
    IndexInput input = d.openInput(name);
    try {
      size = input.readInt();       
      if (size == -1) {
        readDgaps(input);
      } else {
        readBits(input);
      }
    } finally {
      input.close();
    }
  }
  private void readBits(IndexInput input) throws IOException {
    count = input.readInt();        
    bits = new byte[(size >> 3) + 1];     
    input.readBytes(bits, 0, bits.length);
  }
  private void readDgaps(IndexInput input) throws IOException {
    size = input.readInt();       
    count = input.readInt();        
    bits = new byte[(size >> 3) + 1];     
    int last=0;
    int n = count();
    while (n>0) {
      last += input.readVInt();
      bits[last] = input.readByte();
      n -= BYTE_COUNTS[bits[last] & 0xFF];
    }          
  }
  public BitVector subset(int start, int end) {
    if (start < 0 || end > size() || end < start)
      throw new IndexOutOfBoundsException();
    if (end == start) return new BitVector(0);
    byte[] bits = new byte[((end - start - 1) >>> 3) + 1];
    int s = start >>> 3;
    for (int i = 0; i < bits.length; i++) {
      int cur = 0xFF & this.bits[i + s];
      int next = i + s + 1 >= this.bits.length ? 0 : 0xFF & this.bits[i + s + 1];
      bits[i] = (byte) ((cur >>> (start & 7)) | ((next << (8 - (start & 7)))));
    }
    int bitsToClear = (bits.length * 8 - (end - start)) % 8;
    bits[bits.length - 1] &= ~(0xFF << (8 - bitsToClear));
    return new BitVector(bits, end - start);
  }
}
