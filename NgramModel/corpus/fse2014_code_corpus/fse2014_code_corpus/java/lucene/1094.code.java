package org.apache.lucene.queryParser.core;
import org.apache.lucene.messages.Message;
import org.apache.lucene.messages.NLSException;
public class QueryNodeError extends Error implements NLSException {
  private static final long serialVersionUID = 1804855832182710327L;
  private Message message;
  public QueryNodeError(Message message) {
    super(message.getKey());
    this.message = message;
  }
  public QueryNodeError(Throwable throwable) {
    super(throwable);
  }
  public QueryNodeError(Message message, Throwable throwable) {
    super(message.getKey(), throwable);
    this.message = message;
  }
  public Message getMessageObject() {
    return this.message;
  }
}
