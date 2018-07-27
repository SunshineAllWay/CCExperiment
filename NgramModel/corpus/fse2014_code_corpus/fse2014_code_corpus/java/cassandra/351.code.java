package org.apache.cassandra.service;
import java.io.IOException;
public interface CassandraDaemon
{
    public void init(String[] arguments) throws IOException;
    public void start() throws IOException;
    public void stop();
    public void destroy();
    public void activate();
    public void deactivate();
}
