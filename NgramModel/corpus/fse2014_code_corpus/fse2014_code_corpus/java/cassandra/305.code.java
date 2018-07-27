package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.Map;
public interface DynamicEndpointSnitchMBean {
    public Map<InetAddress, Double> getScores();
    public int getUpdateInterval();
    public int getResetInterval();
    public double getBadnessThreshold();
    public String getSubsnitchClassName();
}
