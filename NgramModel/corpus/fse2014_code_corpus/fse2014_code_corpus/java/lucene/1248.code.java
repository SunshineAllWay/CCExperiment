package org.apache.regexp;
public class RegexpTunnel {
  public static char[] getPrefix(RE regexp) {
    REProgram program = regexp.getProgram();
    return program.prefix;
  }
}
