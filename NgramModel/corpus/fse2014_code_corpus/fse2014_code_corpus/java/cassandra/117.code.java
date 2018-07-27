package org.apache.cassandra.cql;
import java.util.ArrayList;
import java.util.List;
public class SelectExpression
{
    public static final int MAX_COLUMNS_DEFAULT = 10000;
    private int numColumns = MAX_COLUMNS_DEFAULT;
    private boolean reverseColumns = false;
    private Term start, finish;
    private List<Term> columns;
    public SelectExpression(Term start, Term finish, int count, boolean reverse)
    {
        this.start = start;
        this.finish = finish;
        numColumns = count;
        reverseColumns = reverse;
    }
    public SelectExpression(Term first, int count, boolean reverse)
    {
        columns = new ArrayList<Term>();
        columns.add(first);
        numColumns = count;
        reverseColumns = reverse;
    }
    public void and(Term addTerm)
    {
        assert !isColumnRange();    
        columns.add(addTerm);
    }
    public boolean isColumnRange()
    {
        return (start != null);
    }
    public boolean isColumnList()
    {
        return !isColumnRange();
    }
    public int getColumnsLimit()
    {
        return numColumns;
    }
    public boolean isColumnsReversed()
    {
        return reverseColumns;
    }
    public void setColumnsReversed(boolean reversed)
    {
        reverseColumns = reversed;
    }
    public void setColumnsLimit(int limit)
    {
        numColumns = limit;
    }
    public Term getStart()
    {
        return start;
    }
    public Term getFinish()
    {
        return finish;
    }
    public List<Term> getColumns()
    {
        return columns;
    }
}
