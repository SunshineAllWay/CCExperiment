package org.apache.lucene.index;
import java.io.IOException;
import java.util.Arrays;
import org.apache.lucene.store.IndexInput;
class DefaultSkipListReader extends MultiLevelSkipListReader {
  private boolean currentFieldStoresPayloads;
  private long freqPointer[];
  private long proxPointer[];
  private int payloadLength[];
  private long lastFreqPointer;
  private long lastProxPointer;
  private int lastPayloadLength;
  DefaultSkipListReader(IndexInput skipStream, int maxSkipLevels, int skipInterval) {
    super(skipStream, maxSkipLevels, skipInterval);
    freqPointer = new long[maxSkipLevels];
    proxPointer = new long[maxSkipLevels];
    payloadLength = new int[maxSkipLevels];
  }
  void init(long skipPointer, long freqBasePointer, long proxBasePointer, int df, boolean storesPayloads) {
    super.init(skipPointer, df);
    this.currentFieldStoresPayloads = storesPayloads;
    lastFreqPointer = freqBasePointer;
    lastProxPointer = proxBasePointer;
    Arrays.fill(freqPointer, freqBasePointer);
    Arrays.fill(proxPointer, proxBasePointer);
    Arrays.fill(payloadLength, 0);
  }
  long getFreqPointer() {
    return lastFreqPointer;
  }
  long getProxPointer() {
    return lastProxPointer;
  }
  int getPayloadLength() {
    return lastPayloadLength;
  }
  @Override
  protected void seekChild(int level) throws IOException {
    super.seekChild(level);
    freqPointer[level] = lastFreqPointer;
    proxPointer[level] = lastProxPointer;
    payloadLength[level] = lastPayloadLength;
  }
  @Override
  protected void setLastSkipData(int level) {
    super.setLastSkipData(level);
    lastFreqPointer = freqPointer[level];
    lastProxPointer = proxPointer[level];
    lastPayloadLength = payloadLength[level];
  }
  @Override
  protected int readSkipData(int level, IndexInput skipStream) throws IOException {
    int delta;
    if (currentFieldStoresPayloads) {
      delta = skipStream.readVInt();
      if ((delta & 1) != 0) {
        payloadLength[level] = skipStream.readVInt();
      }
      delta >>>= 1;
    } else {
      delta = skipStream.readVInt();
    }
    freqPointer[level] += skipStream.readVInt();
    proxPointer[level] += skipStream.readVInt();
    return delta;
  }
}
