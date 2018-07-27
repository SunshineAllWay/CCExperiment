package org.apache.cassandra.service;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.thrift.UnavailableException;
public interface StorageServiceMBean
{    
    public List<String> getLiveNodes();
    public List<String> getUnreachableNodes();
    public List<String> getJoiningNodes();
    public List<String> getLeavingNodes();
    public String getToken();
    public String getReleaseVersion();
    public Map<Range, List<String>> getRangeToEndpointMap(String keyspace);
    public Map<Range, List<String>> getPendingRangeToEndpointMap(String keyspace);
    public Map<Token, String> getTokenToEndpointMap();
    public double getLoad();
    public String getLoadString();
    public Map<String, String> getLoadMap();
    public int getCurrentGenerationNumber();
    public List<InetAddress> getNaturalEndpoints(String table, ByteBuffer key);
    public void takeSnapshot(String tableName, String tag) throws IOException;
    public void takeAllSnapshot(String tag) throws IOException;
    public void clearSnapshot() throws IOException;
    public void forceTableCompaction(String tableName, String... columnFamilies) throws IOException, ExecutionException, InterruptedException;
    public void forceTableCleanup(String tableName, String... columnFamilies) throws IOException, ExecutionException, InterruptedException;
    public void forceTableFlush(String tableName, String... columnFamilies) throws IOException, ExecutionException, InterruptedException;
    public void forceTableRepair(String tableName, String... columnFamilies) throws IOException;
    public void decommission() throws InterruptedException;
    public void move(String newToken) throws IOException, InterruptedException;
    public void loadBalance() throws IOException, InterruptedException;
    public void removeToken(String token);
    public String getRemovalStatus();
    public void forceRemoveCompletion();
    public void setLog4jLevel(String classQualifier, String level);
    public String getOperationMode();
    public String getDrainProgress();
    public void drain() throws IOException, InterruptedException, ExecutionException;
    public void loadSchemaFromYAML() throws ConfigurationException, IOException;
    public String exportSchema() throws IOException;
    public void truncate(String keyspace, String columnFamily) throws UnavailableException, TimeoutException, IOException;
    public void deliverHints(String host) throws UnknownHostException;
    public void saveCaches() throws ExecutionException, InterruptedException;
    public Map<Token, Float> getOwnership();
    public List<String> getKeyspaces();
    public void updateSnitch(String epSnitchClassName, Boolean dynamic, Integer dynamicUpdateInterval, Integer dynamicResetInterval, Double dynamicBadnessThreshold) throws ConfigurationException;
    public void stopGossiping();
    public void startGossiping();
    public boolean isInitialized();
    public void invalidateKeyCaches(String ks, String... cfs) throws IOException;
    public void invalidateRowCaches(String ks, String... cfs) throws IOException;
}
