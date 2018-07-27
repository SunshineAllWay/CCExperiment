package org.apache.lucene.analysis.cn.smart.hhmm;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.lucene.analysis.cn.smart.AnalyzerProfile;
import org.apache.lucene.analysis.cn.smart.Utility;
class WordDictionary extends AbstractDictionary {
  private WordDictionary() {
  }
  private static WordDictionary singleInstance;
  public static final int PRIME_INDEX_LENGTH = 12071;
  private short[] wordIndexTable;
  private char[] charIndexTable;
  private char[][][] wordItem_charArrayTable;
  private int[][] wordItem_frequencyTable;
  public synchronized static WordDictionary getInstance() {
    if (singleInstance == null) {
      singleInstance = new WordDictionary();
      try {
        singleInstance.load();
      } catch (IOException e) {
        String wordDictRoot = AnalyzerProfile.ANALYSIS_DATA_DIR;
        singleInstance.load(wordDictRoot);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return singleInstance;
  }
  public void load(String dctFileRoot) {
    String dctFilePath = dctFileRoot + "/coredict.dct";
    File serialObj = new File(dctFileRoot + "/coredict.mem");
    if (serialObj.exists() && loadFromObj(serialObj)) {
    } else {
      try {
        wordIndexTable = new short[PRIME_INDEX_LENGTH];
        charIndexTable = new char[PRIME_INDEX_LENGTH];
        for (int i = 0; i < PRIME_INDEX_LENGTH; i++) {
          charIndexTable[i] = 0;
          wordIndexTable[i] = -1;
        }
        wordItem_charArrayTable = new char[GB2312_CHAR_NUM][][];
        wordItem_frequencyTable = new int[GB2312_CHAR_NUM][];
        loadMainDataFromFile(dctFilePath);
        expandDelimiterData();
        mergeSameWords();
        sortEachItems();
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
      saveToObj(serialObj);
    }
  }
  public void load() throws IOException, ClassNotFoundException {
    InputStream input = this.getClass().getResourceAsStream("coredict.mem");
    loadFromObjectInputStream(input);
  }
  private boolean loadFromObj(File serialObj) {
    try {
      loadFromObjectInputStream(new FileInputStream(serialObj));
      return true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return false;
  }
  private void loadFromObjectInputStream(InputStream serialObjectInputStream)
      throws IOException, ClassNotFoundException {
    ObjectInputStream input = new ObjectInputStream(serialObjectInputStream);
    wordIndexTable = (short[]) input.readObject();
    charIndexTable = (char[]) input.readObject();
    wordItem_charArrayTable = (char[][][]) input.readObject();
    wordItem_frequencyTable = (int[][]) input.readObject();
    input.close();
  }
  private void saveToObj(File serialObj) {
    try {
      ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(
          serialObj));
      output.writeObject(wordIndexTable);
      output.writeObject(charIndexTable);
      output.writeObject(wordItem_charArrayTable);
      output.writeObject(wordItem_frequencyTable);
      output.close();
    } catch (Exception e) {
    }
  }
  private int loadMainDataFromFile(String dctFilePath)
      throws FileNotFoundException, IOException, UnsupportedEncodingException {
    int i, cnt, length, total = 0;
    int[] buffer = new int[3];
    byte[] intBuffer = new byte[4];
    String tmpword;
    RandomAccessFile dctFile = new RandomAccessFile(dctFilePath, "r");
    for (i = GB2312_FIRST_CHAR; i < GB2312_FIRST_CHAR + CHAR_NUM_IN_FILE; i++) {
      dctFile.read(intBuffer);
      cnt = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
      if (cnt <= 0) {
        wordItem_charArrayTable[i] = null;
        wordItem_frequencyTable[i] = null;
        continue;
      }
      wordItem_charArrayTable[i] = new char[cnt][];
      wordItem_frequencyTable[i] = new int[cnt];
      total += cnt;
      int j = 0;
      while (j < cnt) {
        dctFile.read(intBuffer);
        buffer[0] = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN)
            .getInt();
        dctFile.read(intBuffer);
        buffer[1] = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN)
            .getInt();
        dctFile.read(intBuffer);
        buffer[2] = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN)
            .getInt();
        wordItem_frequencyTable[i][j] = buffer[0];
        length = buffer[1];
        if (length > 0) {
          byte[] lchBuffer = new byte[length];
          dctFile.read(lchBuffer);
          tmpword = new String(lchBuffer, "GB2312");
          wordItem_charArrayTable[i][j] = tmpword.toCharArray();
        } else {
          wordItem_charArrayTable[i][j] = null;
        }
        j++;
      }
      String str = getCCByGB2312Id(i);
      setTableIndex(str.charAt(0), i);
    }
    dctFile.close();
    return total;
  }
  private void expandDelimiterData() {
    int i;
    int cnt;
    int delimiterIndex = 3755 + GB2312_FIRST_CHAR;
    i = 0;
    while (i < wordItem_charArrayTable[delimiterIndex].length) {
      char c = wordItem_charArrayTable[delimiterIndex][i][0];
      int j = getGB2312Id(c);
      if (wordItem_charArrayTable[j] == null) {
        int k = i;
        while (k < wordItem_charArrayTable[delimiterIndex].length
            && wordItem_charArrayTable[delimiterIndex][k][0] == c) {
          k++;
        }
        cnt = k - i;
        if (cnt != 0) {
          wordItem_charArrayTable[j] = new char[cnt][];
          wordItem_frequencyTable[j] = new int[cnt];
        }
        for (k = 0; k < cnt; k++, i++) {
          wordItem_frequencyTable[j][k] = wordItem_frequencyTable[delimiterIndex][i];
          wordItem_charArrayTable[j][k] = new char[wordItem_charArrayTable[delimiterIndex][i].length - 1];
          System.arraycopy(wordItem_charArrayTable[delimiterIndex][i], 1,
              wordItem_charArrayTable[j][k], 0,
              wordItem_charArrayTable[j][k].length);
        }
        setTableIndex(c, j);
      }
    }
    wordItem_charArrayTable[delimiterIndex] = null;
    wordItem_frequencyTable[delimiterIndex] = null;
  }
  private void mergeSameWords() {
    int i;
    for (i = 0; i < GB2312_FIRST_CHAR + CHAR_NUM_IN_FILE; i++) {
      if (wordItem_charArrayTable[i] == null)
        continue;
      int len = 1;
      for (int j = 1; j < wordItem_charArrayTable[i].length; j++) {
        if (Utility.compareArray(wordItem_charArrayTable[i][j], 0,
            wordItem_charArrayTable[i][j - 1], 0) != 0)
          len++;
      }
      if (len < wordItem_charArrayTable[i].length) {
        char[][] tempArray = new char[len][];
        int[] tempFreq = new int[len];
        int k = 0;
        tempArray[0] = wordItem_charArrayTable[i][0];
        tempFreq[0] = wordItem_frequencyTable[i][0];
        for (int j = 1; j < wordItem_charArrayTable[i].length; j++) {
          if (Utility.compareArray(wordItem_charArrayTable[i][j], 0,
              tempArray[k], 0) != 0) {
            k++;
            tempArray[k] = wordItem_charArrayTable[i][j];
            tempFreq[k] = wordItem_frequencyTable[i][j];
          } else {
            tempFreq[k] += wordItem_frequencyTable[i][j];
          }
        }
        wordItem_charArrayTable[i] = tempArray;
        wordItem_frequencyTable[i] = tempFreq;
      }
    }
  }
  private void sortEachItems() {
    char[] tmpArray;
    int tmpFreq;
    for (int i = 0; i < wordItem_charArrayTable.length; i++) {
      if (wordItem_charArrayTable[i] != null
          && wordItem_charArrayTable[i].length > 1) {
        for (int j = 0; j < wordItem_charArrayTable[i].length - 1; j++) {
          for (int j2 = j + 1; j2 < wordItem_charArrayTable[i].length; j2++) {
            if (Utility.compareArray(wordItem_charArrayTable[i][j], 0,
                wordItem_charArrayTable[i][j2], 0) > 0) {
              tmpArray = wordItem_charArrayTable[i][j];
              tmpFreq = wordItem_frequencyTable[i][j];
              wordItem_charArrayTable[i][j] = wordItem_charArrayTable[i][j2];
              wordItem_frequencyTable[i][j] = wordItem_frequencyTable[i][j2];
              wordItem_charArrayTable[i][j2] = tmpArray;
              wordItem_frequencyTable[i][j2] = tmpFreq;
            }
          }
        }
      }
    }
  }
  private boolean setTableIndex(char c, int j) {
    int index = getAvaliableTableIndex(c);
    if (index != -1) {
      charIndexTable[index] = c;
      wordIndexTable[index] = (short) j;
      return true;
    } else
      return false;
  }
  private short getAvaliableTableIndex(char c) {
    int hash1 = (int) (hash1(c) % PRIME_INDEX_LENGTH);
    int hash2 = hash2(c) % PRIME_INDEX_LENGTH;
    if (hash1 < 0)
      hash1 = PRIME_INDEX_LENGTH + hash1;
    if (hash2 < 0)
      hash2 = PRIME_INDEX_LENGTH + hash2;
    int index = hash1;
    int i = 1;
    while (charIndexTable[index] != 0 && charIndexTable[index] != c
        && i < PRIME_INDEX_LENGTH) {
      index = (hash1 + i * hash2) % PRIME_INDEX_LENGTH;
      i++;
    }
    if (i < PRIME_INDEX_LENGTH
        && (charIndexTable[index] == 0 || charIndexTable[index] == c)) {
      return (short) index;
    } else
      return -1;
  }
  private short getWordItemTableIndex(char c) {
    int hash1 = (int) (hash1(c) % PRIME_INDEX_LENGTH);
    int hash2 = hash2(c) % PRIME_INDEX_LENGTH;
    if (hash1 < 0)
      hash1 = PRIME_INDEX_LENGTH + hash1;
    if (hash2 < 0)
      hash2 = PRIME_INDEX_LENGTH + hash2;
    int index = hash1;
    int i = 1;
    while (charIndexTable[index] != 0 && charIndexTable[index] != c
        && i < PRIME_INDEX_LENGTH) {
      index = (hash1 + i * hash2) % PRIME_INDEX_LENGTH;
      i++;
    }
    if (i < PRIME_INDEX_LENGTH && charIndexTable[index] == c) {
      return (short) index;
    } else
      return -1;
  }
  private int findInTable(short knownHashIndex, char[] charArray) {
    if (charArray == null || charArray.length == 0)
      return -1;
    char[][] items = wordItem_charArrayTable[wordIndexTable[knownHashIndex]];
    int start = 0, end = items.length - 1;
    int mid = (start + end) / 2, cmpResult;
    while (start <= end) {
      cmpResult = Utility.compareArray(items[mid], 0, charArray, 1);
      if (cmpResult == 0)
        return mid;
      else if (cmpResult < 0)
        start = mid + 1;
      else if (cmpResult > 0)
        end = mid - 1;
      mid = (start + end) / 2;
    }
    return -1;
  }
  public int getPrefixMatch(char[] charArray) {
    return getPrefixMatch(charArray, 0);
  }
  public int getPrefixMatch(char[] charArray, int knownStart) {
    short index = getWordItemTableIndex(charArray[0]);
    if (index == -1)
      return -1;
    char[][] items = wordItem_charArrayTable[wordIndexTable[index]];
    int start = knownStart, end = items.length - 1;
    int mid = (start + end) / 2, cmpResult;
    while (start <= end) {
      cmpResult = Utility.compareArrayByPrefix(charArray, 1, items[mid], 0);
      if (cmpResult == 0) {
        while (mid >= 0
            && Utility.compareArrayByPrefix(charArray, 1, items[mid], 0) == 0)
          mid--;
        mid++;
        return mid;
      } else if (cmpResult < 0)
        end = mid - 1;
      else
        start = mid + 1;
      mid = (start + end) / 2;
    }
    return -1;
  }
  public int getFrequency(char[] charArray) {
    short hashIndex = getWordItemTableIndex(charArray[0]);
    if (hashIndex == -1)
      return 0;
    int itemIndex = findInTable(hashIndex, charArray);
    if (itemIndex != -1)
      return wordItem_frequencyTable[wordIndexTable[hashIndex]][itemIndex];
    return 0;
  }
  public boolean isEqual(char[] charArray, int itemIndex) {
    short hashIndex = getWordItemTableIndex(charArray[0]);
    return Utility.compareArray(charArray, 1,
        wordItem_charArrayTable[wordIndexTable[hashIndex]][itemIndex], 0) == 0;
  }
}
