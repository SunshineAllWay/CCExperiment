package org.apache.solr.client.solrj.request;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class ContentStreamUpdateRequest extends AbstractUpdateRequest {
  List<ContentStream> contentStreams;
  public ContentStreamUpdateRequest(String url) {
    super(METHOD.POST, url);
    contentStreams = new ArrayList<ContentStream>();
  }
  @Override
  public Collection<ContentStream> getContentStreams() throws IOException {
    return contentStreams;
  }
  public void addFile(File file) throws IOException {
    addContentStream(new ContentStreamBase.FileStream(file));
  }
  public void addContentStream(ContentStream contentStream){
    contentStreams.add(contentStream);
  }
}
