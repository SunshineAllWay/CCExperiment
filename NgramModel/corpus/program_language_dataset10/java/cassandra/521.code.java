package org.apache.cassandra.service;
import org.junit.Test;
import java.io.IOException;
import org.apache.cassandra.config.ConfigurationException;
public class InitClientTest 
{
    @Test
    public void testInitClientStartup() throws IOException, ConfigurationException
    {
        StorageService.instance.initClient();
    }
}
