package org.apache.cassandra.service;
import java.io.IOException;
import org.apache.cassandra.net.Message;
public interface IResponseResolver<T> {
	public T resolve() throws DigestMismatchException, IOException;
	public boolean isDataPresent();
    public T getData() throws IOException;
    public void preprocess(Message message);
    public Iterable<Message> getMessages();
    public int getMessageCount();
}
