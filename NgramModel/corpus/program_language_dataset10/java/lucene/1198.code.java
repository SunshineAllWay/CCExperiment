package org.apache.lucene.queryParser.standard.nodes;
import java.util.List;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Similarity;
public class StandardBooleanQueryNode extends BooleanQueryNode {
  private static final long serialVersionUID = 1938287817191138787L;
  private boolean disableCoord;
  public StandardBooleanQueryNode(List<QueryNode> clauses, boolean disableCoord) {
    super(clauses);
    this.disableCoord = disableCoord;
  }
  public boolean isDisableCoord() {
    return this.disableCoord;
  }
}
