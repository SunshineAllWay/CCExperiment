package org.apache.log4j.pattern;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
public final class PatternParser {
  private static final char ESCAPE_CHAR = '%';
  private static final int LITERAL_STATE = 0;
  private static final int CONVERTER_STATE = 1;
  private static final int DOT_STATE = 3;
  private static final int MIN_STATE = 4;
  private static final int MAX_STATE = 5;
  private static final Map PATTERN_LAYOUT_RULES;
  private static final Map FILENAME_PATTERN_RULES;
  static {
    Map rules = new HashMap(17);
    rules.put("c", LoggerPatternConverter.class);
    rules.put("logger", LoggerPatternConverter.class);
    rules.put("C", ClassNamePatternConverter.class);
    rules.put("class", ClassNamePatternConverter.class);
    rules.put("d", DatePatternConverter.class);
    rules.put("date", DatePatternConverter.class);
    rules.put("F", FileLocationPatternConverter.class);
    rules.put("file", FileLocationPatternConverter.class);
    rules.put("l", FullLocationPatternConverter.class);
    rules.put("L", LineLocationPatternConverter.class);
    rules.put("line", LineLocationPatternConverter.class);
    rules.put("m", MessagePatternConverter.class);
    rules.put("message", MessagePatternConverter.class);
    rules.put("n", LineSeparatorPatternConverter.class);
    rules.put("M", MethodLocationPatternConverter.class);
    rules.put("method", MethodLocationPatternConverter.class);
    rules.put("p", LevelPatternConverter.class);
    rules.put("level", LevelPatternConverter.class);
    rules.put("r", RelativeTimePatternConverter.class);
    rules.put("relative", RelativeTimePatternConverter.class);
    rules.put("t", ThreadPatternConverter.class);
    rules.put("thread", ThreadPatternConverter.class);
    rules.put("x", NDCPatternConverter.class);
    rules.put("ndc", NDCPatternConverter.class);
    rules.put("X", PropertiesPatternConverter.class);
    rules.put("properties", PropertiesPatternConverter.class);
    rules.put("sn", SequenceNumberPatternConverter.class);
    rules.put("sequenceNumber", SequenceNumberPatternConverter.class);
    rules.put("throwable", ThrowableInformationPatternConverter.class);
    PATTERN_LAYOUT_RULES = new ReadOnlyMap(rules);
    Map fnameRules = new HashMap(4);
    fnameRules.put("d", FileDatePatternConverter.class);
    fnameRules.put("date", FileDatePatternConverter.class);
    fnameRules.put("i", IntegerPatternConverter.class);
    fnameRules.put("index", IntegerPatternConverter.class);
    FILENAME_PATTERN_RULES = new ReadOnlyMap(fnameRules);
  }
  private PatternParser() {
  }
  public static Map getPatternLayoutRules() {
    return PATTERN_LAYOUT_RULES;
  }
  public static Map getFileNamePatternRules() {
    return FILENAME_PATTERN_RULES;
  }
  private static int extractConverter(
    char lastChar, final String pattern, int i, final StringBuffer convBuf,
    final StringBuffer currentLiteral) {
    convBuf.setLength(0);
    if (!Character.isUnicodeIdentifierStart(lastChar)) {
      return i;
    }
    convBuf.append(lastChar);
    while (
      (i < pattern.length())
        && Character.isUnicodeIdentifierPart(pattern.charAt(i))) {
      convBuf.append(pattern.charAt(i));
      currentLiteral.append(pattern.charAt(i));
      i++;
    }
    return i;
  }
  private static int extractOptions(String pattern, int i, List options) {
    while ((i < pattern.length()) && (pattern.charAt(i) == '{')) {
      int end = pattern.indexOf('}', i);
      if (end == -1) {
        break;
      }
      String r = pattern.substring(i + 1, end);
      options.add(r);
      i = end + 1;
    }
    return i;
  }
  public static void parse(
    final String pattern, final List patternConverters,
    final List formattingInfos, final Map converterRegistry, final Map rules) {
    if (pattern == null) {
      throw new NullPointerException("pattern");
    }
    StringBuffer currentLiteral = new StringBuffer(32);
    int patternLength = pattern.length();
    int state = LITERAL_STATE;
    char c;
    int i = 0;
    FormattingInfo formattingInfo = FormattingInfo.getDefault();
    while (i < patternLength) {
      c = pattern.charAt(i++);
      switch (state) {
      case LITERAL_STATE:
        if (i == patternLength) {
          currentLiteral.append(c);
          continue;
        }
        if (c == ESCAPE_CHAR) {
          switch (pattern.charAt(i)) {
          case ESCAPE_CHAR:
            currentLiteral.append(c);
            i++; 
            break;
          default:
            if (currentLiteral.length() != 0) {
              patternConverters.add(
                new LiteralPatternConverter(currentLiteral.toString()));
              formattingInfos.add(FormattingInfo.getDefault());
            }
            currentLiteral.setLength(0);
            currentLiteral.append(c); 
            state = CONVERTER_STATE;
            formattingInfo = FormattingInfo.getDefault();
          }
        } else {
          currentLiteral.append(c);
        }
        break;
      case CONVERTER_STATE:
        currentLiteral.append(c);
        switch (c) {
        case '-':
          formattingInfo =
            new FormattingInfo(
              true, formattingInfo.getMinLength(),
              formattingInfo.getMaxLength());
          break;
        case '.':
          state = DOT_STATE;
          break;
        default:
          if ((c >= '0') && (c <= '9')) {
            formattingInfo =
              new FormattingInfo(
                formattingInfo.isLeftAligned(), c - '0',
                formattingInfo.getMaxLength());
            state = MIN_STATE;
          } else {
            i = finalizeConverter(
                c, pattern, i, currentLiteral, formattingInfo,
                converterRegistry, rules, patternConverters, formattingInfos);
            state = LITERAL_STATE;
            formattingInfo = FormattingInfo.getDefault();
            currentLiteral.setLength(0);
          }
        } 
        break;
      case MIN_STATE:
        currentLiteral.append(c);
        if ((c >= '0') && (c <= '9')) {
          formattingInfo =
            new FormattingInfo(
              formattingInfo.isLeftAligned(),
              (formattingInfo.getMinLength() * 10) + (c - '0'),
              formattingInfo.getMaxLength());
        } else if (c == '.') {
          state = DOT_STATE;
        } else {
          i = finalizeConverter(
              c, pattern, i, currentLiteral, formattingInfo,
              converterRegistry, rules, patternConverters, formattingInfos);
          state = LITERAL_STATE;
          formattingInfo = FormattingInfo.getDefault();
          currentLiteral.setLength(0);
        }
        break;
      case DOT_STATE:
        currentLiteral.append(c);
        if ((c >= '0') && (c <= '9')) {
          formattingInfo =
            new FormattingInfo(
              formattingInfo.isLeftAligned(), formattingInfo.getMinLength(),
              c - '0');
          state = MAX_STATE;
        } else {
            LogLog.error(
              "Error occured in position " + i
              + ".\n Was expecting digit, instead got char \"" + c + "\".");
          state = LITERAL_STATE;
        }
        break;
      case MAX_STATE:
        currentLiteral.append(c);
        if ((c >= '0') && (c <= '9')) {
          formattingInfo =
            new FormattingInfo(
              formattingInfo.isLeftAligned(), formattingInfo.getMinLength(),
              (formattingInfo.getMaxLength() * 10) + (c - '0'));
        } else {
          i = finalizeConverter(
              c, pattern, i, currentLiteral, formattingInfo,
              converterRegistry, rules, patternConverters, formattingInfos);
          state = LITERAL_STATE;
          formattingInfo = FormattingInfo.getDefault();
          currentLiteral.setLength(0);
        }
        break;
      } 
    }
    if (currentLiteral.length() != 0) {
      patternConverters.add(
        new LiteralPatternConverter(currentLiteral.toString()));
      formattingInfos.add(FormattingInfo.getDefault());
    }
  }
  private static PatternConverter createConverter(
    final String converterId, final StringBuffer currentLiteral,
    final Map converterRegistry, final Map rules, final List options) {
    String converterName = converterId;
    Object converterObj = null;
    for (int i = converterId.length(); (i > 0) && (converterObj == null);
        i--) {
      converterName = converterName.substring(0, i);
      if (converterRegistry != null) {
        converterObj = converterRegistry.get(converterName);
      }
      if ((converterObj == null) && (rules != null)) {
        converterObj = rules.get(converterName);
      }
    }
    if (converterObj == null) {
        LogLog.error("Unrecognized format specifier [" + converterId + "]");
      return null;
    }
    Class converterClass = null;
    if (converterObj instanceof Class) {
      converterClass = (Class) converterObj;
    } else {
      if (converterObj instanceof String) {
        try {
          converterClass = Loader.loadClass((String) converterObj);
        } catch (ClassNotFoundException ex) {
            LogLog.warn(
              "Class for conversion pattern %" + converterName + " not found",
              ex);
          return null;
        }
      } else {
          LogLog.warn(
            "Bad map entry for conversion pattern %" +  converterName + ".");
        return null;
      }
    }
    try {
      Method factory =
        converterClass.getMethod(
          "newInstance",
          new Class[] {
            Class.forName("[Ljava.lang.String;")
          });
      String[] optionsArray = new String[options.size()];
      optionsArray = (String[]) options.toArray(optionsArray);
      Object newObj =
        factory.invoke(null, new Object[] { optionsArray });
      if (newObj instanceof PatternConverter) {
        currentLiteral.delete(
          0,
          currentLiteral.length()
          - (converterId.length() - converterName.length()));
        return (PatternConverter) newObj;
      } else {
          LogLog.warn(
            "Class " + converterClass.getName()
            + " does not extend PatternConverter.");
      }
    } catch (Exception ex) {
        LogLog.error("Error creating converter for " + converterId, ex);
      try {
        PatternConverter pc = (PatternConverter) converterClass.newInstance();
        currentLiteral.delete(
          0,
          currentLiteral.length()
          - (converterId.length() - converterName.length()));
        return pc;
      } catch (Exception ex2) {
          LogLog.error("Error creating converter for " + converterId, ex2);
      }
    }
    return null;
  }
  private static int finalizeConverter(
    char c, String pattern, int i,
    final StringBuffer currentLiteral, final FormattingInfo formattingInfo,
    final Map converterRegistry, final Map rules, final List patternConverters,
    final List formattingInfos) {
    StringBuffer convBuf = new StringBuffer();
    i = extractConverter(c, pattern, i, convBuf, currentLiteral);
    String converterId = convBuf.toString();
    List options = new ArrayList();
    i = extractOptions(pattern, i, options);
    PatternConverter pc =
      createConverter(
        converterId, currentLiteral, converterRegistry, rules, options);
    if (pc == null) {
      StringBuffer msg;
      if ((converterId == null) || (converterId.length() == 0)) {
        msg =
          new StringBuffer("Empty conversion specifier starting at position ");
      } else {
        msg = new StringBuffer("Unrecognized conversion specifier [");
        msg.append(converterId);
        msg.append("] starting at position ");
      }
      msg.append(Integer.toString(i));
      msg.append(" in conversion pattern.");
        LogLog.error(msg.toString());
      patternConverters.add(
        new LiteralPatternConverter(currentLiteral.toString()));
      formattingInfos.add(FormattingInfo.getDefault());
    } else {
      patternConverters.add(pc);
      formattingInfos.add(formattingInfo);
      if (currentLiteral.length() > 0) {
        patternConverters.add(
          new LiteralPatternConverter(currentLiteral.toString()));
        formattingInfos.add(FormattingInfo.getDefault());
      }
    }
    currentLiteral.setLength(0);
    return i;
  }
  private static class ReadOnlyMap implements Map {
    private final Map map;
    public ReadOnlyMap(Map src) {
      map = src;
    }
    public void clear() {
      throw new UnsupportedOperationException();
    }
    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }
    public boolean containsValue(Object value) {
      return map.containsValue(value);
    }
    public Set entrySet() {
      return map.entrySet();
    }
    public Object get(Object key) {
      return map.get(key);
    }
    public boolean isEmpty() {
      return map.isEmpty();
    }
    public Set keySet() {
      return map.keySet();
    }
    public Object put(Object key, Object value) {
      throw new UnsupportedOperationException();
    }
    public void putAll(Map t) {
      throw new UnsupportedOperationException();
    }
    public Object remove(Object key) {
      throw new UnsupportedOperationException();
    }
    public int size() {
      return map.size();
    }
    public Collection values() {
      return map.values();
    }
  }
}
