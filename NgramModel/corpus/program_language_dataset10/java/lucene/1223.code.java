package org.apache.lucene.queryParser.standard.processors;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.QuotedFieldQueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.core.util.UnescapedCharSequence;
import org.apache.lucene.queryParser.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryParser.standard.parser.StandardSyntaxParser;
import org.apache.lucene.search.PrefixQuery;
public class WildcardQueryNodeProcessor extends QueryNodeProcessorImpl {
  public WildcardQueryNodeProcessor() {
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof FieldQueryNode || node instanceof FuzzyQueryNode) {      
      FieldQueryNode fqn = (FieldQueryNode) node;      
      CharSequence text = fqn.getText(); 
      if (fqn instanceof ParametricQueryNode 
          || fqn instanceof QuotedFieldQueryNode 
          || text.length() <= 0){
        return node;
      }
      if (isPrefixWildcard(text)) {        
        PrefixWildcardQueryNode prefixWildcardQN = new PrefixWildcardQueryNode(fqn);
        return prefixWildcardQN;
      } else if (isWildcard(text)){
        WildcardQueryNode wildcardQN = new WildcardQueryNode(fqn);
        return wildcardQN;
      }
    }
    return node;
  }
  private boolean isWildcard(CharSequence text) {
    if (text ==null || text.length() <= 0) return false;
    for(int i=text.length()-1; i>=0; i--){
      if ((text.charAt(i) == '*' || text.charAt(i) == '?') && !UnescapedCharSequence.wasEscaped(text, i)){
        return true;
      }
    }
    return false;
  }
  private boolean isPrefixWildcard(CharSequence text) {
    if (text == null || text.length() <= 0 || !isWildcard(text)) return false;
    if (text.charAt(text.length()-1) != '*') return false;
    if (UnescapedCharSequence.wasEscaped(text, text.length()-1)) return false;
    if (text.length() == 1) return false;
    for(int i=0; i<text.length(); i++){
      if (text.charAt(i) == '?') return false;
      if (text.charAt(i) == '*' && !UnescapedCharSequence.wasEscaped(text, i)){        
        if (i == text.length()-1) 
          return true;
        else 
          return false;
      }
    }
    return false;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    return children;
  }
}
