package org.apache.log4j.util;
public interface Filter {
  final String BASIC_PAT = "\\[main\\] (FATAL|ERROR|WARN|INFO|DEBUG)";
  final String ISO8601_PAT = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}";  
  static public final String ABSOLUTE_DATE_AND_TIME_PAT = 
                           "^\\d{1,2} .{2,6}\\.? 2\\d{3} \\d{2}:\\d{2}:\\d{2},\\d{3}";
  static public final String ABSOLUTE_TIME_PAT = 
                           "^\\d{2}:\\d{2}:\\d{2},\\d{3}";
  static public final String RELATIVE_TIME_PAT = "^\\d{1,10}";
  String filter(String in) throws UnexpectedFormatException;
}
