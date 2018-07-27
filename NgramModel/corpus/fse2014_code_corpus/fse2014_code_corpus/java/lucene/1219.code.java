package org.apache.lucene.queryParser.standard.processors;
import java.text.Collator;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricRangeQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode.CompareOperator;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.config.DateResolutionAttribute;
import org.apache.lucene.queryParser.standard.config.LocaleAttribute;
import org.apache.lucene.queryParser.standard.config.RangeCollatorAttribute;
import org.apache.lucene.queryParser.standard.nodes.RangeQueryNode;
public class ParametricRangeQueryNodeProcessor extends QueryNodeProcessorImpl {
  public ParametricRangeQueryNodeProcessor() {
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof ParametricRangeQueryNode) {
      ParametricRangeQueryNode parametricRangeNode = (ParametricRangeQueryNode) node;
      ParametricQueryNode upper = parametricRangeNode.getUpperBound();
      ParametricQueryNode lower = parametricRangeNode.getLowerBound();
      Locale locale = Locale.getDefault();
      Collator collator = null;
      DateTools.Resolution dateRes = null;
      boolean inclusive = false;
      if (getQueryConfigHandler().hasAttribute(RangeCollatorAttribute.class)) {
        collator = getQueryConfigHandler().getAttribute(
            RangeCollatorAttribute.class).getRangeCollator();
      }
      if (getQueryConfigHandler().hasAttribute(LocaleAttribute.class)) {
        locale = getQueryConfigHandler().getAttribute(LocaleAttribute.class)
            .getLocale();
      }
      FieldConfig fieldConfig = getQueryConfigHandler().getFieldConfig(
          parametricRangeNode.getField());
      if (fieldConfig != null) {
        if (fieldConfig.hasAttribute(DateResolutionAttribute.class)) {
          dateRes = fieldConfig.getAttribute(DateResolutionAttribute.class)
              .getDateResolution();
        }
      }
      if (upper.getOperator() == CompareOperator.LE) {
        inclusive = true;
      } else if (lower.getOperator() == CompareOperator.GE) {
        inclusive = true;
      }
      String part1 = lower.getTextAsString();
      String part2 = upper.getTextAsString();
      try {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        df.setLenient(true);
        Date d1 = df.parse(part1);
        Date d2 = df.parse(part2);
        if (inclusive) {
          Calendar cal = Calendar.getInstance(locale);
          cal.setTime(d2);
          cal.set(Calendar.HOUR_OF_DAY, 23);
          cal.set(Calendar.MINUTE, 59);
          cal.set(Calendar.SECOND, 59);
          cal.set(Calendar.MILLISECOND, 999);
          d2 = cal.getTime();
        }
        if (dateRes == null) {
          part1 = DateField.dateToString(d1);
          part2 = DateField.dateToString(d2);
        } else {
          part1 = DateTools.dateToString(d1, dateRes);
          part2 = DateTools.dateToString(d2, dateRes);
        }
      } catch (Exception e) {
      }
      lower.setText(part1);
      upper.setText(part2);
      return new RangeQueryNode(lower, upper, collator);
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
