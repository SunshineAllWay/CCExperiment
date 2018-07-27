package org.apache.lucene.analysis;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
public class WordlistLoader {
  public static Set<String> getWordSet(Class<?> aClass, String stopwordResource)
      throws IOException {
    final Reader reader = new BufferedReader(new InputStreamReader(aClass
        .getResourceAsStream(stopwordResource), "UTF-8"));
    try {
      return getWordSet(reader);
    } finally {
      reader.close();
    }
  }
  public static Set<String> getWordSet(Class<?> aClass,
      String stopwordResource, String comment) throws IOException {
    final Reader reader = new BufferedReader(new InputStreamReader(aClass
        .getResourceAsStream(stopwordResource), "UTF-8"));
    try {
      return getWordSet(reader, comment);
    } finally {
      reader.close();
    }
  }
  public static HashSet<String> getWordSet(File wordfile) throws IOException {
    FileReader reader = null;
    try {
      reader = new FileReader(wordfile);
      return getWordSet(reader);
    }
    finally {
      if (reader != null)
        reader.close();
    }
  }
  public static HashSet<String> getWordSet(File wordfile, String comment) throws IOException {
    FileReader reader = null;
    try {
      reader = new FileReader(wordfile);
      return getWordSet(reader, comment);
    }
    finally {
      if (reader != null)
        reader.close();
    }
  }
  public static HashSet<String> getWordSet(Reader reader) throws IOException {
    final HashSet<String> result = new HashSet<String>();
    BufferedReader br = null;
    try {
      if (reader instanceof BufferedReader) {
        br = (BufferedReader) reader;
      } else {
        br = new BufferedReader(reader);
      }
      String word = null;
      while ((word = br.readLine()) != null) {
        result.add(word.trim());
      }
    }
    finally {
      if (br != null)
        br.close();
    }
    return result;
  }
  public static HashSet<String> getWordSet(Reader reader, String comment) throws IOException {
    final HashSet<String> result = new HashSet<String>();
    BufferedReader br = null;
    try {
      if (reader instanceof BufferedReader) {
        br = (BufferedReader) reader;
      } else {
        br = new BufferedReader(reader);
      }
      String word = null;
      while ((word = br.readLine()) != null) {
        if (word.startsWith(comment) == false){
          result.add(word.trim());
        }
      }
    }
    finally {
      if (br != null)
        br.close();
    }
    return result;
  }
  public static Set<String> getSnowballWordSet(Class<?> aClass,
      String stopwordResource) throws IOException {
    final Reader reader = new BufferedReader(new InputStreamReader(aClass
        .getResourceAsStream(stopwordResource), "UTF-8"));
    try {
      return getSnowballWordSet(reader);
    } finally {
      reader.close();
    }
  }
  public static Set<String> getSnowballWordSet(Reader reader)
      throws IOException {
    final Set<String> result = new HashSet<String>();
    BufferedReader br = null;
    try {
      if (reader instanceof BufferedReader) {
        br = (BufferedReader) reader;
      } else {
        br = new BufferedReader(reader);
      }
      String line = null;
      while ((line = br.readLine()) != null) {
        int comment = line.indexOf('|');
        if (comment >= 0) line = line.substring(0, comment);
        String words[] = line.split("\\s+");
        for (int i = 0; i < words.length; i++)
          if (words[i].length() > 0) result.add(words[i]);
      }
    } finally {
      if (br != null) br.close();
    }
    return result;
  }
  public static HashMap<String, String> getStemDict(File wordstemfile) throws IOException {
    if (wordstemfile == null)
      throw new NullPointerException("wordstemfile may not be null");
    final HashMap<String, String> result = new HashMap<String,String>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(wordstemfile));
      String line;
      while ((line = br.readLine()) != null) {
        String[] wordstem = line.split("\t", 2);
        result.put(wordstem[0], wordstem[1]);
      }
    } finally {
      if(br != null)
        br.close();
    }
    return result;
  }
}
