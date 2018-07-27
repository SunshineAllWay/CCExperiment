package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.GroupQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface DefaultOperatorAttribute extends Attribute {	
  public static enum Operator {
	    AND, OR;
	  }
  public void setOperator(Operator operator);
  public Operator getOperator();
}
