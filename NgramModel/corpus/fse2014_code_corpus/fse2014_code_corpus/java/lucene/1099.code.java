package org.apache.lucene.queryParser.core.builders;
import java.util.HashMap;
import java.util.List;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.FieldableNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.parser.EscapeQuerySyntaxImpl;
public class QueryTreeBuilder implements QueryBuilder {
  public static final String QUERY_TREE_BUILDER_TAGID = QueryTreeBuilder.class
      .getName();
  private HashMap<Class<? extends QueryNode>, QueryBuilder> queryNodeBuilders;
  private HashMap<CharSequence, QueryBuilder> fieldNameBuilders;
  public QueryTreeBuilder() {
  }
  public void setBuilder(CharSequence fieldName, QueryBuilder builder) {
    if (this.fieldNameBuilders == null) {
      this.fieldNameBuilders = new HashMap<CharSequence, QueryBuilder>();
    }
    this.fieldNameBuilders.put(fieldName, builder);
  }
  public void setBuilder(Class<? extends QueryNode> queryNodeClass,
      QueryBuilder builder) {
    if (this.queryNodeBuilders == null) {
      this.queryNodeBuilders = new HashMap<Class<? extends QueryNode>, QueryBuilder>();
    }
    this.queryNodeBuilders.put(queryNodeClass, builder);
  }
  private void process(QueryNode node) throws QueryNodeException {
    if (node != null) {
      QueryBuilder builder = getBuilder(node);
      if (!(builder instanceof QueryTreeBuilder)) {
        List<QueryNode> children = node.getChildren();
        if (children != null) {
          for (QueryNode child : children) {
            process(child);
          }
        }
      }
      processNode(node, builder);
    }
  }
  private QueryBuilder getBuilder(QueryNode node) {
    QueryBuilder builder = null;
    if (this.fieldNameBuilders != null && node instanceof FieldableNode) {
      builder = this.fieldNameBuilders.get(((FieldableNode) node).getField());
    }
    if (builder == null && this.queryNodeBuilders != null) {
      Class<?> clazz = node.getClass();
      do {
        builder = getQueryBuilder(clazz);
        if (builder == null) {
          Class<?>[] classes = node.getClass().getInterfaces();
          for (Class<?> actualClass : classes) {
            builder = getQueryBuilder(actualClass);
            if (builder != null) {
              break;
            }
          }
        }
      } while (builder == null && (clazz = clazz.getSuperclass()) != null);
    }
    return builder;
  }
  private void processNode(QueryNode node, QueryBuilder builder)
      throws QueryNodeException {
    if (builder == null) {
      throw new QueryNodeException(new MessageImpl(
          QueryParserMessages.LUCENE_QUERY_CONVERSION_ERROR, node
              .toQueryString(new EscapeQuerySyntaxImpl()), node.getClass()
              .getName()));
    }
    Object obj = builder.build(node);
    if (obj != null) {
      node.setTag(QUERY_TREE_BUILDER_TAGID, obj);
    }
  }
  private QueryBuilder getQueryBuilder(Class<?> clazz) {
    if (QueryNode.class.isAssignableFrom(clazz)) {
      return this.queryNodeBuilders.get(clazz);
    }
    return null;
  }
  public Object build(QueryNode queryNode) throws QueryNodeException {
    process(queryNode);
    return queryNode.getTag(QUERY_TREE_BUILDER_TAGID);
  }
}
