package org.apache.lucene.queryParser.standard.processors;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryParser.core.nodes.GroupQueryNode;
import org.apache.lucene.queryParser.core.nodes.NoTokenFoundQueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.TextableQueryNode;
import org.apache.lucene.queryParser.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.config.AnalyzerAttribute;
import org.apache.lucene.queryParser.standard.config.PositionIncrementsAttribute;
import org.apache.lucene.queryParser.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.queryParser.standard.nodes.StandardBooleanQueryNode;
import org.apache.lucene.queryParser.standard.nodes.WildcardQueryNode;
public class AnalyzerQueryNodeProcessor extends QueryNodeProcessorImpl {
  private Analyzer analyzer;
  private boolean positionIncrementsEnabled;
  public AnalyzerQueryNodeProcessor() {
  }
  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    if (getQueryConfigHandler().hasAttribute(AnalyzerAttribute.class)) {
      this.analyzer = getQueryConfigHandler().getAttribute(
          AnalyzerAttribute.class).getAnalyzer();
      this.positionIncrementsEnabled = false;
      if (getQueryConfigHandler().hasAttribute(
          PositionIncrementsAttribute.class)) {
        if (getQueryConfigHandler().getAttribute(
            PositionIncrementsAttribute.class).isPositionIncrementsEnabled()) {
          this.positionIncrementsEnabled = true;
        }
      }
      if (this.analyzer != null) {
        return super.process(queryTree);
      }
    }
    return queryTree;
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof TextableQueryNode
        && !(node instanceof WildcardQueryNode)
        && !(node instanceof FuzzyQueryNode)
        && !(node instanceof ParametricQueryNode)) {
      FieldQueryNode fieldNode = ((FieldQueryNode) node);
      String text = fieldNode.getTextAsString();
      String field = fieldNode.getFieldAsString();
      TokenStream source = this.analyzer.tokenStream(field, new StringReader(
          text));
      CachingTokenFilter buffer = new CachingTokenFilter(source);
      PositionIncrementAttribute posIncrAtt = null;
      int numTokens = 0;
      int positionCount = 0;
      boolean severalTokensAtSamePosition = false;
      if (buffer.hasAttribute(PositionIncrementAttribute.class)) {
        posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);
      }
      try {
        while (buffer.incrementToken()) {
          numTokens++;
          int positionIncrement = (posIncrAtt != null) ? posIncrAtt
              .getPositionIncrement() : 1;
          if (positionIncrement != 0) {
            positionCount += positionIncrement;
          } else {
            severalTokensAtSamePosition = true;
          }
        }
      } catch (IOException e) {
      }
      try {
        buffer.reset();
        source.close();
      } catch (IOException e) {
      }
      if (!buffer.hasAttribute(TermAttribute.class)) {
        return new NoTokenFoundQueryNode();
      }
      TermAttribute termAtt = buffer.getAttribute(TermAttribute.class);
      if (numTokens == 0) {
        return new NoTokenFoundQueryNode();
      } else if (numTokens == 1) {
        String term = null;
        try {
          boolean hasNext;
          hasNext = buffer.incrementToken();
          assert hasNext == true;
          term = termAtt.term();
        } catch (IOException e) {
        }
        fieldNode.setText(term);
        return fieldNode;
      } else if (severalTokensAtSamePosition) {
        if (positionCount == 1) {
          LinkedList<QueryNode> children = new LinkedList<QueryNode>();
          for (int i = 0; i < numTokens; i++) {
            String term = null;
            try {
              boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.term();
            } catch (IOException e) {
            }
            children.add(new FieldQueryNode(field, term, -1, -1));
          }
          return new GroupQueryNode(
              new StandardBooleanQueryNode(children, true));
        } else {
          MultiPhraseQueryNode mpq = new MultiPhraseQueryNode();
          List<FieldQueryNode> multiTerms = new ArrayList<FieldQueryNode>();
          int position = -1;
          int i = 0;
          int termGroupCount = 0;
          for (; i < numTokens; i++) {
            String term = null;
            int positionIncrement = 1;
            try {
              boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.term();
              if (posIncrAtt != null) {
                positionIncrement = posIncrAtt.getPositionIncrement();
              }
            } catch (IOException e) {
            }
            if (positionIncrement > 0 && multiTerms.size() > 0) {
              for (FieldQueryNode termNode : multiTerms) {
                if (this.positionIncrementsEnabled) {
                  termNode.setPositionIncrement(position);
                } else {
                  termNode.setPositionIncrement(termGroupCount);
                }
                mpq.add(termNode);
              }
              termGroupCount++;
              multiTerms.clear();
            }
            position += positionIncrement;
            multiTerms.add(new FieldQueryNode(field, term, -1, -1));
          }
          for (FieldQueryNode termNode : multiTerms) {
            if (this.positionIncrementsEnabled) {
              termNode.setPositionIncrement(position);
            } else {
              termNode.setPositionIncrement(termGroupCount);
            }
            mpq.add(termNode);
          }
          return mpq;
        }
      } else {
        TokenizedPhraseQueryNode pq = new TokenizedPhraseQueryNode();
        int position = -1;
        for (int i = 0; i < numTokens; i++) {
          String term = null;
          int positionIncrement = 1;
          try {
            boolean hasNext = buffer.incrementToken();
            assert hasNext == true;
            term = termAtt.term();
            if (posIncrAtt != null) {
              positionIncrement = posIncrAtt.getPositionIncrement();
            }
          } catch (IOException e) {
          }
          FieldQueryNode newFieldNode = new FieldQueryNode(field, term, -1, -1);
          if (this.positionIncrementsEnabled) {
            position += positionIncrement;
            newFieldNode.setPositionIncrement(position);
          } else {
            newFieldNode.setPositionIncrement(i);
          }
          pq.add(newFieldNode);
        }
        return pq;
      }
    }
    return node;
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
