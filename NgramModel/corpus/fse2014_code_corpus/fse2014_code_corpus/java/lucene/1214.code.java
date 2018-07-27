package org.apache.lucene.queryParser.standard.processors;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.AndQueryNode;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.GroupQueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.OrQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode.Modifier;
import org.apache.lucene.queryParser.core.parser.SyntaxParser;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute;
import org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute.Operator;
import org.apache.lucene.queryParser.standard.nodes.BooleanModifierNode;
public class GroupQueryNodeProcessor implements QueryNodeProcessor {
  private ArrayList<QueryNode> queryNodeList;
  private boolean latestNodeVerified;
  private QueryConfigHandler queryConfig;
  private Boolean usingAnd = false;
  public GroupQueryNodeProcessor() {
  }
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    if (!getQueryConfigHandler().hasAttribute(DefaultOperatorAttribute.class)) {
      throw new IllegalArgumentException(
          "DefaultOperatorAttribute should be set on the QueryConfigHandler");
    }
    this.usingAnd = Operator.AND == getQueryConfigHandler()
        .getAttribute(DefaultOperatorAttribute.class).getOperator();
    if (queryTree instanceof GroupQueryNode) {
      queryTree = ((GroupQueryNode) queryTree).getChild();
    }
    this.queryNodeList = new ArrayList<QueryNode>();
    this.latestNodeVerified = false;
    readTree(queryTree);
    List<QueryNode> actualQueryNodeList = this.queryNodeList;
    for (int i = 0; i < actualQueryNodeList.size(); i++) {
      QueryNode node = actualQueryNodeList.get(i);
      if (node instanceof GroupQueryNode) {
        actualQueryNodeList.set(i, process(node));
      }
    }
    this.usingAnd = false;
    if (queryTree instanceof BooleanQueryNode) {
      queryTree.set(actualQueryNodeList);
      return queryTree;
    } else {
      return new BooleanQueryNode(actualQueryNodeList);
    }
  }
  private QueryNode applyModifier(QueryNode node, QueryNode parent) {
    if (this.usingAnd) {
      if (parent instanceof OrQueryNode) {
        if (node instanceof ModifierQueryNode) {
          ModifierQueryNode modNode = (ModifierQueryNode) node;
          if (modNode.getModifier() == Modifier.MOD_REQ) {
            return modNode.getChild();
          }
        }
      } else {
        if (node instanceof ModifierQueryNode) {
          ModifierQueryNode modNode = (ModifierQueryNode) node;
          if (modNode.getModifier() == Modifier.MOD_NONE) {
            return new BooleanModifierNode(modNode.getChild(), Modifier.MOD_REQ);
          }
        } else {
          return new BooleanModifierNode(node, Modifier.MOD_REQ);
        }
      }
    } else {
      if (node.getParent() instanceof AndQueryNode) {
        if (node instanceof ModifierQueryNode) {
          ModifierQueryNode modNode = (ModifierQueryNode) node;
          if (modNode.getModifier() == Modifier.MOD_NONE) {
            return new BooleanModifierNode(modNode.getChild(), Modifier.MOD_REQ);
          }
        } else {
          return new BooleanModifierNode(node, Modifier.MOD_REQ);
        }
      }
    }
    return node;
  }
  private void readTree(QueryNode node) {
    if (node instanceof BooleanQueryNode) {
      List<QueryNode> children = node.getChildren();
      if (children != null && children.size() > 0) {
        for (int i = 0; i < children.size() - 1; i++) {
          readTree(children.get(i));
        }
        processNode(node);
        readTree(children.get(children.size() - 1));
      } else {
        processNode(node);
      }
    } else {
      processNode(node);
    }
  }
  private void processNode(QueryNode node) {
    if (node instanceof AndQueryNode || node instanceof OrQueryNode) {
      if (!this.latestNodeVerified && !this.queryNodeList.isEmpty()) {
        this.queryNodeList.add(applyModifier(this.queryNodeList
            .remove(this.queryNodeList.size() - 1), node));
        this.latestNodeVerified = true;
      }
    } else if (!(node instanceof BooleanQueryNode)) {
      this.queryNodeList.add(applyModifier(node, node.getParent()));
      this.latestNodeVerified = false;
    }
  }
  public QueryConfigHandler getQueryConfigHandler() {
    return this.queryConfig;
  }
  public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler) {
    this.queryConfig = queryConfigHandler;
  }
}
