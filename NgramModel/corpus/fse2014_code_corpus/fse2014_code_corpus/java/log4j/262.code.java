package org.apache.log4j.performance;
public class NewVsSetLen {
  static String s;
  static int BIGBUF_LEN = 1048576;
  static int SBUF_LEN = 256;
  static int RUN_LENGTH = BIGBUF_LEN/4;
  static char[] sbuf = new char[SBUF_LEN];
  static char[] bigbuf = new char[BIGBUF_LEN];
  {
    for(int i = 0; i < SBUF_LEN; i++) {
      sbuf[i] = (char) (i);
    }
    for(int i = 0; i < BIGBUF_LEN; i++) {
      bigbuf[i] = (char) (i);
    }
  }
  static
  public 
  void main(String[] args) {    
    int t;
    for(int len = SBUF_LEN; len <= BIGBUF_LEN; len*=4, RUN_LENGTH /= 4) {
      System.out.println("<td>"+len+"\n");
      for(int second = 0; second < 16;) {
	System.out.println("SECOND loop="+second +", RUN_LENGTH="
			   +RUN_LENGTH+", len="+len);
	t = (int)newBuffer(len, second);
	System.out.print("<td>" + t);
	t = (int)setLen(len, second);
	System.out.println(" <td>" + t + " \n");
	if(second == 0) {
	  second = 1;
	} else {
	  second *= 2;
	}
      }
    }
  }
  static
  double newBuffer(int size, int second) {    
    long before = System.currentTimeMillis();
    for(int i = 0; i < RUN_LENGTH; i++) {
      StringBuffer buf = new StringBuffer(SBUF_LEN);
      buf.append(sbuf, 0, sbuf.length);
      buf.append(bigbuf, 0, size);
      s = buf.toString();
    }
    for(int x = 0; x <  second; x++) {
      StringBuffer buf = new StringBuffer(SBUF_LEN);
      buf.append(sbuf, 0, SBUF_LEN);
      s = buf.toString();
    }
    return (System.currentTimeMillis() - before)*1000.0/RUN_LENGTH;    
  }
  static
  double setLen(int size, int second) {
    long before = System.currentTimeMillis();
    StringBuffer buf = new StringBuffer(SBUF_LEN);
    for(int i = 0; i < RUN_LENGTH; i++) {
      buf.append(sbuf, 0, sbuf.length);
      buf.append(bigbuf, 0, size);
      s = buf.toString();
      buf.setLength(0);
    }
    for(int x = 0; x < second; x++) {
      buf.append(sbuf, 0, SBUF_LEN);
      s = buf.toString();
      buf.setLength(0);
    }
    return (System.currentTimeMillis() - before)*1000.0/RUN_LENGTH;    
  }  
}
