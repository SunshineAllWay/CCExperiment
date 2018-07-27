package org.apache.cassandra.cql;
import java.util.ArrayList;
import java.util.List;
public class WhereClause
{
    private List<Term> keys = new ArrayList<Term>();
    private Term startKey, finishKey;
    private List<Relation> columns = new ArrayList<Relation>();
    public WhereClause(Relation firstRelation)
    {
        and(firstRelation);
    }
    public WhereClause()
    {
    }
    public void and(Relation relation)
    {
        if (relation.isKey())
        {
            if (relation.operator().equals(RelationType.EQ))
                keys.add(relation.getValue());
            else if ((relation.operator().equals(RelationType.GT) || relation.operator().equals(RelationType.GTE)))
                startKey = relation.getValue();
            else if ((relation.operator().equals(RelationType.LT) || relation.operator().equals(RelationType.LTE)))
                finishKey = relation.getValue();
        }
        else
            columns.add(relation);
    }
    public List<Relation> getColumnRelations()
    {
        return columns;
    }
    public boolean isKeyRange()
    {
        return startKey != null;
    }
    public boolean isKeyList()
    {
        return !isKeyRange();
    }
    public Term getStartKey()
    {
        return startKey;
    }
    public Term getFinishKey()
    {
        return finishKey;
    }
    public List<Term> getKeys()
    {
        return keys;
    }
}
