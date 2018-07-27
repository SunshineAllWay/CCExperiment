package org.apache.log4j.util;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
public class Transformer {
  public 
  static 
  void transform(String in, String out, Filter[] filters) throws IOException,
                                                                 UnexpectedFormatException {
    String line;
    BufferedReader input = new BufferedReader(new FileReader(in));
    PrintStream output = new PrintStream(new FileOutputStream(out, false));
    while((line = input.readLine()) != null) {
      for(int i = 0; i < filters.length; i++) {
	line = filters[i].filter(line);
      }
      if(line != null) {
	output.println(line);
      }
    }
  }
  public 
  static 
  void transform(String in, String out, Filter filter) throws IOException,
                                                              UnexpectedFormatException {
    String line;
    BufferedReader input = new BufferedReader(new FileReader(in));
    PrintStream output = new PrintStream(new FileOutputStream(out));
    while((line = input.readLine()) != null) {
      line = filter.filter(line);
      output.println(line);
    }
  }
}
