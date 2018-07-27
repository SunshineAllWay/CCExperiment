package org.apache.lucene.queryParser;
public interface CharStream {
  char readChar() throws java.io.IOException;
  int getColumn();
  int getLine();
  int getEndColumn();
  int getEndLine();
  int getBeginColumn();
  int getBeginLine();
  void backup(int amount);
  char BeginToken() throws java.io.IOException;
  String GetImage();
  char[] GetSuffix(int len);
  void Done();
}
