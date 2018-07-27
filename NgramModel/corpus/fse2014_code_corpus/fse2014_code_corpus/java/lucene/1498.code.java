package org.apache.lucene.index;
import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_OBJECT_REF;
import org.apache.lucene.util.ArrayUtil;
final class CharBlockPool {
  public char[][] buffers = new char[10][];
  int numBuffer;
  int bufferUpto = -1;                        
  public int charUpto = DocumentsWriter.CHAR_BLOCK_SIZE;             
  public char[] buffer;                              
  public int charOffset = -DocumentsWriter.CHAR_BLOCK_SIZE;          
  final private DocumentsWriter docWriter;
  public CharBlockPool(DocumentsWriter docWriter) {
    this.docWriter = docWriter;
  }
  public void reset() {
    docWriter.recycleCharBlocks(buffers, 1+bufferUpto);
    bufferUpto = -1;
    charUpto = DocumentsWriter.CHAR_BLOCK_SIZE;
    charOffset = -DocumentsWriter.CHAR_BLOCK_SIZE;
  }
  public void nextBuffer() {
    if (1+bufferUpto == buffers.length) {
      char[][] newBuffers = new char[ArrayUtil.oversize(buffers.length+1,
                                                        NUM_BYTES_OBJECT_REF)][];
      System.arraycopy(buffers, 0, newBuffers, 0, buffers.length);
      buffers = newBuffers;
    }
    buffer = buffers[1+bufferUpto] = docWriter.getCharBlock();
    bufferUpto++;
    charUpto = 0;
    charOffset += DocumentsWriter.CHAR_BLOCK_SIZE;
  }
}
