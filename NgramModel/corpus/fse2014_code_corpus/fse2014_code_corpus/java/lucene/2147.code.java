package org.apache.solr.response;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocSlice;
import org.apache.solr.common.SolrDocumentList;
public class PageTool {
  private long start;
  private int results_per_page = 10;
  private long results_found;
  private int page_count;
  private int current_page_number;
  public PageTool(SolrQueryRequest request, SolrQueryResponse response) {
    String rows = request.getParams().get("rows");
    if (rows != null) {
      results_per_page = new Integer(rows);
    }
    Object docs = response.getValues().get("response");
    if (docs != null) {
      if (docs instanceof DocSlice) {
        DocSlice doc_slice = (DocSlice) docs;
        results_found = doc_slice.matches();
        start = doc_slice.offset();
      } else {
        SolrDocumentList doc_list = (SolrDocumentList) docs;
        results_found = doc_list.getNumFound();
        start = doc_list.getStart();
      }
    }
    page_count = (int) Math.ceil(results_found / (double) results_per_page);
    current_page_number = (int) Math.ceil(start / (double) results_per_page) + (page_count > 0 ? 1 : 0);
  }
  public long getStart() {
    return start;
  }
  public int getResults_per_page() {
    return results_per_page;
  }
  public long getResults_found() {
    return results_found;
  }
  public int getPage_count() {
    return page_count;
  }
  public int getCurrent_page_number() {
    return current_page_number;
  }
  public String toString() {
    return "Found " + results_found +
           " Page " + current_page_number + " of " + page_count +
           " Starting at " + start + " per page " + results_per_page;
  }
}
