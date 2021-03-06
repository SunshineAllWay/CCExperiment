package org.apache.cassandra.cql;
import java.util.List;
import org.apache.cassandra.thrift.ConsistencyLevel;
public class BatchUpdateStatement
{
    private ConsistencyLevel consistency;
    private List<UpdateStatement> updates;
    public BatchUpdateStatement(List<UpdateStatement> updates, ConsistencyLevel consistency)
    {
        this.updates = updates;
        this.consistency = consistency;
    }
    public ConsistencyLevel getConsistencyLevel()
    {
        return consistency;
    }
    public List<UpdateStatement> getUpdates()
    {
        return updates;
    }
    public String toString()
    {
        return String.format("BatchUpdateStatement(updates=%s, consistency=%s)",
                             updates,
                             consistency);
    }
}
