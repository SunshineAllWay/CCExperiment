package org.apache.lucene.queryParser.standard.builders;
import java.util.List;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode.Modifier;
import org.apache.lucene.queryParser.standard.nodes.StandardBooleanQueryNode;
import org.apache.lucene.queryParser.standard.parser.EscapeQuerySyntaxImpl;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
public class StandardBooleanQueryNodeBuilder implements StandardQueryBuilder {
  public StandardBooleanQueryNodeBuilder() {
  }
  public BooleanQuery build(QueryNode queryNode) throws QueryNodeException {
    StandardBooleanQueryNode booleanNode = (StandardBooleanQueryNode) queryNode;
    BooleanQuery bQuery = new BooleanQuery(booleanNode.isDisableCoord());
    List<QueryNode> children = booleanNode.getChildren();
    if (children != null) {
      for (QueryNode child : children) {
        Object obj = child.getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
        if (obj != null) {
          Query query = (Query) obj;
          try {
            bQuery.add(query, getModifierValue(child));
          } catch (TooManyClauses ex) {
            throw new QueryNodeException(new MessageImpl(
                QueryParserMessages.TOO_MANY_BOOLEAN_CLAUSES, BooleanQuery
                    .getMaxClauseCount(), queryNode
                    .toQueryString(new EscapeQuerySyntaxImpl())), ex);
          }
        }
      }
    }
    return bQuery;
  }
  private static BooleanClause.Occur getModifierValue(QueryNode node)
      throws QueryNodeException {
    if (node instanceof ModifierQueryNode) {
      ModifierQueryNode mNode = ((ModifierQueryNode) node);
      Modifier modifier = mNode.getModifier();
      if (Modifier.MOD_NONE.equals(modifier)) {
        return BooleanClause.Occur.SHOULD;
      } else if (Modifier.MOD_NOT.equals(modifier)) {
        return BooleanClause.Occur.MUST_NOT;
      } else {
        return BooleanClause.Occur.MUST;
      }
    }
    return BooleanClause.Occur.SHOULD;
  }
}
