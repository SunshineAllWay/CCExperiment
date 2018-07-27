package org.apache.cassandra.cql;
import java.util.List;
import org.apache.cassandra.thrift.ConsistencyLevel;
public class DeleteStatement
{
    private List<Term> columns;
    private String columnFamily;
    private ConsistencyLevel cLevel;
    private List<Term> keys;
    public DeleteStatement(List<Term> columns, String columnFamily, ConsistencyLevel cLevel, List<Term> keys)
    {
        this.columns = columns;
        this.columnFamily = columnFamily;
        this.cLevel = cLevel;
        this.keys = keys;
    }
    public List<Term> getColumns()
    {
        return columns;
    }
    public String getColumnFamily()
    {
        return columnFamily;
    }
    public ConsistencyLevel getConsistencyLevel()
    {
        return cLevel;
    }
    public List<Term> getKeys()
    {
        return keys;
    }
    public String toString()
    {
        return String.format("DeleteStatement(columns=%s, columnFamily=%s, consistency=%s keys=%s)",
                             columns,
                             columnFamily,
                             cLevel,
                             keys);
    }
}
