package org.apache.lucene.wordnet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
public class SynonymMap {
  private final HashMap<String,String[]> table;
  private static final String[] EMPTY = new String[0];
  private static final boolean DEBUG = false;
  public SynonymMap(InputStream input) throws IOException {
    this.table = input == null ? new HashMap<String,String[]>(0) : read(toByteArray(input));
  }
  public String[] getSynonyms(String word) {
    String[] synonyms = table.get(word);
    if (synonyms == null) return EMPTY;
    String[] copy = new String[synonyms.length]; 
    System.arraycopy(synonyms, 0, copy, 0, synonyms.length);
    return copy;
  }
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    Iterator<String> iter = new TreeMap<String,String[]>(table).keySet().iterator();
    int count = 0;
    int f0 = 0;
    int f1 = 0;
    int f2 = 0;
    int f3 = 0;
    while (iter.hasNext()) {
      String word = iter.next();
      buf.append(word + ":");
      String[] synonyms = getSynonyms(word);
      buf.append(Arrays.asList(synonyms));
      buf.append("\n");
      count += synonyms.length;
      if (synonyms.length == 0) f0++;
      if (synonyms.length == 1) f1++;
      if (synonyms.length == 2) f2++;
      if (synonyms.length == 3) f3++;
    }
    buf.append("\n\nkeys=" + table.size() + ", synonyms=" + count + ", f0=" + f0 +", f1=" + f1 + ", f2=" + f2 + ", f3=" + f3);
    return buf.toString();
  }
  protected String analyze(String word) {
    return word.toLowerCase();
  }
  private static boolean isValid(String str) {
    for (int i=str.length(); --i >= 0; ) {
      if (!Character.isLetter(str.charAt(i))) return false;
    }
    return true;
  }
  private HashMap<String,String[]> read(byte[] data) {
    int WORDS  = (int) (76401 / 0.7); 
    int GROUPS = (int) (88022 / 0.7); 
    HashMap<String,ArrayList<Integer>> word2Groups = new HashMap<String,ArrayList<Integer>>(WORDS);  
    HashMap<Integer,ArrayList<String>> group2Words = new HashMap<Integer,ArrayList<String>>(GROUPS); 
    HashMap<String,String> internedWords = new HashMap<String,String>(WORDS);
    Charset charset = Charset.forName("UTF-8");
    int lastNum = -1;
    Integer lastGroup = null;
    int len = data.length;
    int i=0;
    while (i < len) { 
      while (i < len && data[i] != '(') i++;
      if (i >= len) break; 
      i++;
      int num = 0;
      while (i < len && data[i] != ',') {
        num = 10*num + (data[i] - 48);
        i++;
      }
      i++;
      while (i < len && data[i] != '\'') i++;
      i++;
      int start = i;
      do {
        while (i < len && data[i] != '\'') i++;
        i++;
      } while (i < len && data[i] != ','); 
      if (i >= len) break; 
      String word = charset.decode(ByteBuffer.wrap(data, start, i-start-1)).toString();
      if (!isValid(word)) continue; 
      word = analyze(word);
      if (word == null || word.length() == 0) continue; 
      String w = internedWords.get(word);
      if (w == null) {
        word = new String(word); 
        internedWords.put(word, word);
      } else {
        word = w;
      }
      Integer group = lastGroup;
      if (num != lastNum) {
        group = Integer.valueOf(num);
        lastGroup = group;
        lastNum = num;
      }
      ArrayList<Integer> groups =  word2Groups.get(word);
      if (groups == null) {
        groups = new ArrayList<Integer>(1);
        word2Groups.put(word, groups);
      }
      groups.add(group);
      ArrayList<String> words = group2Words.get(group);
      if (words == null) {
        words = new ArrayList<String>(1);
        group2Words.put(group, words);
      } 
      words.add(word);
    }
    HashMap<String,String[]> word2Syns = createIndex(word2Groups, group2Words);    
    word2Groups = null; 
    group2Words = null; 
    return optimize(word2Syns, internedWords);
  }
  private HashMap<String,String[]> createIndex(Map<String,ArrayList<Integer>> word2Groups, Map<Integer,ArrayList<String>> group2Words) {
    HashMap<String,String[]> word2Syns = new HashMap<String,String[]>();
    for (final Map.Entry<String,ArrayList<Integer>> entry : word2Groups.entrySet()) { 
      ArrayList<Integer> group = entry.getValue();     
      String word = entry.getKey();
      TreeSet<String> synonyms = new TreeSet<String>();
      for (int i=group.size(); --i >= 0; ) { 
        ArrayList<String> words = group2Words.get(group.get(i));
        for (int j=words.size(); --j >= 0; ) { 
          String synonym = words.get(j); 
          if (synonym != word) { 
            synonyms.add(synonym);
          }
        }
      }
      int size = synonyms.size();
      if (size > 0) {
        String[] syns = new String[size];
        if (size == 1)  
          syns[0] = synonyms.first();
        else
          synonyms.toArray(syns);
        word2Syns.put(word, syns);
      }
    }
    return word2Syns;
  }
  private HashMap<String,String[]> optimize(HashMap<String,String[]> word2Syns, HashMap<String,String> internedWords) {
    if (DEBUG) {
      System.err.println("before gc");
      for (int i=0; i < 10; i++) System.gc();
      System.err.println("after gc");
    }
    int len = 0;
    int size = word2Syns.size();
    String[][] allSynonyms = new String[size][];
    String[] words = new String[size];
    Iterator<Map.Entry<String,String[]>> iter = word2Syns.entrySet().iterator();
    for (int j=0; j < size; j++) {
      Map.Entry<String,String[]> entry = iter.next();
      allSynonyms[j] = entry.getValue(); 
      words[j] = entry.getKey();
      len += words[j].length();
    }
    StringBuilder buf = new StringBuilder(len);
    for (int j=0; j < size; j++) buf.append(words[j]);
    String allWords = new String(buf.toString()); 
    buf = null;
    for (int p=0, j=0; j < size; j++) {
      String word = words[j];
      internedWords.put(word, allWords.substring(p, p + word.length()));
      p += word.length();
    }
    for (int j=0; j < size; j++) {
      String[] syns = allSynonyms[j];
      for (int k=syns.length; --k >= 0; ) {
        syns[k] = internedWords.get(syns[k]);
      }
      word2Syns.remove(words[j]);
      word2Syns.put(internedWords.get(words[j]), syns);
    }
    if (DEBUG) {
      words = null;
      allSynonyms = null;
      internedWords = null;
      allWords = null;
      System.err.println("before gc");
      for (int i=0; i < 10; i++) System.gc();
      System.err.println("after gc");
    }
    return word2Syns;
  }
  private static byte[] toByteArray(InputStream input) throws IOException {
    try {
      int len = Math.max(256, input.available());
      byte[] buffer = new byte[len];
      byte[] output = new byte[len];
      len = 0;
      int n;
      while ((n = input.read(buffer)) >= 0) {
        if (len + n > output.length) { 
          byte tmp[] = new byte[Math.max(output.length << 1, len + n)];
          System.arraycopy(output, 0, tmp, 0, len);
          System.arraycopy(buffer, 0, tmp, len, n);
          buffer = output; 
          output = tmp;
        } else {
          System.arraycopy(buffer, 0, output, len, n);
        }
        len += n;
      }
      if (len == output.length) return output;
      buffer = null; 
      buffer = new byte[len];
      System.arraycopy(output, 0, buffer, 0, len);
      return buffer;
    } finally {
      input.close();
    }
  }
}