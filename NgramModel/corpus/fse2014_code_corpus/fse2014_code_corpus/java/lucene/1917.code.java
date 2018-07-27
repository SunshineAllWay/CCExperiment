package org.apache.lucene.messages;
public class MessagesTestBundle extends NLS {
  private static final String BUNDLE_NAME = MessagesTestBundle.class.getName();
  private MessagesTestBundle() {
  }
  static {
    NLS.initializeMessages(BUNDLE_NAME, MessagesTestBundle.class);
  }
  public static String Q0001E_INVALID_SYNTAX;
  public static String Q0004E_INVALID_SYNTAX_ESCAPE_UNICODE_TRUNCATION;
  public static String Q0005E_MESSAGE_NOT_IN_BUNDLE;
}
