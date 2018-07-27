package org.apache.cassandra.cql;
import java.util.List;
import org.apache.cassandra.thrift.ConsistencyLevel;
public class SelectStatement
{
    private final SelectExpression expression;
    private final boolean isCountOper;
    private final String columnFamily;
    private final ConsistencyLevel cLevel;
    private final WhereClause clause;
    private final int numRecords;
    public SelectStatement(SelectExpression expression, boolean isCountOper, String columnFamily,
            ConsistencyLevel cLevel, WhereClause clause, int numRecords)
    {
        this.expression = expression;
        this.isCountOper = isCountOper;
        this.columnFamily = columnFamily;
        this.cLevel = cLevel;
        this.clause = (clause != null) ? clause : new WhereClause();
        this.numRecords = numRecords;
    }
    public boolean isKeyRange()
    {
        return clause.isKeyRange();
    }
    public List<Term> getKeys()
    {
        return clause.getKeys();
    }
    public Term getKeyStart()
    {
        return clause.getStartKey();
    }
    public Term getKeyFinish()
    {
        return clause.getFinishKey();
    }
    public List<Relation> getColumnRelations()
    {
        return clause.getColumnRelations();
    }
    public boolean isColumnRange()
    {
        return expression.isColumnRange();
    }
    public List<Term> getColumnNames()
    {
        return expression.getColumns();
    }
    public Term getColumnStart()
    {
        return expression.getStart();
    }
    public Term getColumnFinish()
    {
        return expression.getFinish();
    }
    public String getColumnFamily()
    {
        return columnFamily;
    }
    public boolean isColumnsReversed()
    {
        return expression.isColumnsReversed();
    }
    public ConsistencyLevel getConsistencyLevel()
    {
        return cLevel;
    }
    public int getNumRecords()
    {
        return numRecords;
    }
    public int getColumnsLimit()
    {
        return expression.getColumnsLimit();
    }
    public boolean isCountOperation()
    {
        return isCountOper;
    }
}
