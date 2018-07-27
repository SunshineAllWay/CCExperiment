package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.request.SolrQueryRequest;
import java.util.List;
public class LuceneQParserPlugin extends QParserPlugin {
  public static String NAME = "lucene";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new LuceneQParser(qstr, localParams, params, req);
  }
}
class LuceneQParser extends QParser {
  String sortStr;
  SolrQueryParser lparser;
  public LuceneQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }
  public Query parse() throws ParseException {
    String qstr = getString();
    String defaultField = getParam(CommonParams.DF);
    if (defaultField==null) {
      defaultField = getReq().getSchema().getDefaultSearchFieldName();
    }
    lparser = new SolrQueryParser(this, defaultField);
    String opParam = getParam(QueryParsing.OP);
    if (opParam != null) {
      lparser.setDefaultOperator("AND".equals(opParam) ? QueryParser.Operator.AND : QueryParser.Operator.OR);
    } else {
      QueryParser.Operator operator = getReq().getSchema().getSolrQueryParser(null).getDefaultOperator();
      lparser.setDefaultOperator(null == operator ? QueryParser.Operator.OR : operator);
    }
    return lparser.parse(qstr);
  }
  public String[] getDefaultHighlightFields() {
    return new String[]{lparser.getField()};
  }
}
class OldLuceneQParser extends LuceneQParser {
  String sortStr;
  public OldLuceneQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }
  public Query parse() throws ParseException {
    if (getLocalParams() == null) {
      String qstr = getString();
      sortStr = getParams().get(CommonParams.SORT);
      if (sortStr == null) {
        List<String> commands = StrUtils.splitSmart(qstr,';');
        if (commands.size() == 2) {
          qstr = commands.get(0);
          sortStr = commands.get(1);
        } else if (commands.size() == 1) {
          qstr = commands.get(0);
        }
        else if (commands.size() > 2) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "If you want to use multiple ';' in the query, use the 'sort' param.");
        }
      }
      setString(qstr);
    }
    return super.parse();
  }
  @Override
  public SortSpec getSort(boolean useGlobal) throws ParseException {
    SortSpec sort = super.getSort(useGlobal);
    if (sortStr != null && sortStr.length()>0 && sort.getSort()==null) {
      Sort oldSort = QueryParsing.parseSort(sortStr, getReq().getSchema());
      if( oldSort != null ) {
        sort.sort = oldSort;
      }
    }
    return sort;
  }
}
