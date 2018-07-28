package org.apache.log4j.util;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
public class Compare {
  static final int B1_NULL = -1;
  static final int B2_NULL = -2;
  static 
  public
  boolean compare(String file1, String file2) throws FileNotFoundException, 
                                                            IOException {
    BufferedReader in1 = new BufferedReader(new FileReader(file1));
    BufferedReader in2 = new BufferedReader(new FileReader(file2));
    String s1;
    int lineCounter = 0;
    while((s1 = in1.readLine()) != null) {
      lineCounter++;
      String s2 = in2.readLine();
      if(!s1.equals(s2)) {
	System.out.println("Files ["+file1+"] and ["+file2+"] differ on line " 
			   +lineCounter);
	System.out.println("One reads:  ["+s1+"].");
	System.out.println("Other reads:["+s2+"].");
	return false;
      }
    }
    if(in2.read() != -1) {
      System.out.println("File ["+file2+"] longer than file ["+file1+"].");
      return false;
    }
    return true;
  }
}
