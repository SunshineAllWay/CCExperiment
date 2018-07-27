package org.apache.xerces.dom3.as;
public interface CharacterDataEditAS extends NodeEditAS {
    public boolean getIsWhitespaceOnly();
    public boolean canSetData(int offset, 
                              int count);
    public boolean canAppendData(String arg);
    public boolean canReplaceData(int offset, 
                                  int count, 
                                  String arg);
    public boolean canInsertData(int offset, 
                                 String arg);
    public boolean canDeleteData(int offset, 
                                 int count);
}
