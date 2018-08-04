package org.apache.batik.xml;
public interface LexicalUnits {
    int EOF = 0;
    int S = 1;
    int XML_DECL_START = 2;
    int DOCTYPE_START = 3;
    int COMMENT = 4;
    int PI_START = 5;
    int PI_DATA = 6;
    int PI_END = 7;
    int CHARACTER_DATA = 8;
    int START_TAG = 9;
    int END_TAG = 10;
    int CDATA_START = 11;
    int CHARACTER_REFERENCE = 12;
    int ENTITY_REFERENCE = 13;
    int NAME = 14;
    int EQ = 15;
    int FIRST_ATTRIBUTE_FRAGMENT = 16;
    int ATTRIBUTE_FRAGMENT = 17;
    int LAST_ATTRIBUTE_FRAGMENT = 18;
    int EMPTY_ELEMENT_END = 19;
    int END_CHAR = 20;
    int SECTION_END = 21;
    int VERSION_IDENTIFIER = 22;
    int ENCODING_IDENTIFIER = 23;
    int STANDALONE_IDENTIFIER = 24;
    int STRING = 25;
    int SYSTEM_IDENTIFIER = 26;
    int PUBLIC_IDENTIFIER = 27;
    int LSQUARE_BRACKET = 28;
    int RSQUARE_BRACKET = 29;
    int ELEMENT_DECLARATION_START = 30;
    int ATTLIST_START = 31;
    int ENTITY_START = 32;
    int NOTATION_START = 33;
    int PARAMETER_ENTITY_REFERENCE = 34;
    int EMPTY_IDENTIFIER = 35;
    int ANY_IDENTIFIER = 36;
    int QUESTION = 37;
    int PLUS = 38;
    int STAR = 39;
    int LEFT_BRACE = 40;
    int RIGHT_BRACE = 41;
    int PIPE = 42;
    int COMMA = 43;
    int PCDATA_IDENTIFIER = 44;
    int CDATA_IDENTIFIER = 45;
    int ID_IDENTIFIER = 46;
    int IDREF_IDENTIFIER = 47;
    int IDREFS_IDENTIFIER = 48;
    int NMTOKEN_IDENTIFIER = 49;
    int NMTOKENS_IDENTIFIER = 50;
    int ENTITY_IDENTIFIER = 51;
    int ENTITIES_IDENTIFIER = 52;
    int REQUIRED_IDENTIFIER = 53;
    int IMPLIED_IDENTIFIER = 54;
    int FIXED_IDENTIFIER = 55;
    int NMTOKEN = 56;
    int NOTATION_IDENTIFIER = 57;
    int PERCENT = 58;
    int NDATA_IDENTIFIER = 59;
}