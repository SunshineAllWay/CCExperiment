package org.apache.cassandra.gms;
public interface FailureDetectorMBean
{
    public void dumpInterArrivalTimes();
    public void setPhiConvictThreshold(int phi);
    public int getPhiConvictThreshold();
    public String getAllEndpointStates();
}
