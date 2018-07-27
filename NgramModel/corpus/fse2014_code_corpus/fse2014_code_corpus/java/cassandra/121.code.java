package org.apache.cassandra.cql;
import java.util.Map;
import org.apache.cassandra.thrift.ConsistencyLevel;
public class UpdateStatement
{
    public static final ConsistencyLevel defaultConsistency = ConsistencyLevel.ONE;
    private String columnFamily;
    private ConsistencyLevel cLevel = null;
    private Map<Term, Term> columns;
    private Term key;
    public UpdateStatement(String columnFamily, ConsistencyLevel cLevel, Map<Term, Term> columns, Term key)
    {
        this.columnFamily = columnFamily;
        this.cLevel = cLevel;
        this.columns = columns;
        this.key = key;
    }
    public UpdateStatement(String columnFamily, Map<Term, Term> columns, Term key)
    {
        this(columnFamily, null, columns, key);
    }
    public ConsistencyLevel getConsistencyLevel()
    {
        return (cLevel != null) ? cLevel : defaultConsistency;
    }
    public boolean isSetConsistencyLevel()
    {
        return (cLevel != null);
    }
    public String getColumnFamily()
    {
        return columnFamily;
    }
    public Term getKey()
    {
        return key;
    }
    public Map<Term, Term> getColumns()
    {
        return columns;
    }
    public String toString()
    {
        return String.format("UpdateStatement(columnFamily=%s, key=%s, columns=%s, consistency=%s)",
                             columnFamily,
                             key,
                             columns,
                             cLevel);
    }
}
