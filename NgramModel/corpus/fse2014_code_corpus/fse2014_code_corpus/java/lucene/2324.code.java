package org.apache.solr.handler;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
public abstract class ContentStreamHandlerBase extends RequestHandlerBase {
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    SolrParams params = req.getParams();
    UpdateRequestProcessorChain processorChain =
            req.getCore().getUpdateProcessingChain(params.get(UpdateParams.UPDATE_PROCESSOR));
    UpdateRequestProcessor processor = processorChain.createProcessor(req, rsp);
    try {
      ContentStreamLoader documentLoader = newLoader(req, processor);
      Iterable<ContentStream> streams = req.getContentStreams();
      if (streams == null) {
        if (!RequestHandlerUtils.handleCommit(processor, params, false) && !RequestHandlerUtils.handleRollback(processor, params, false)) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "missing content stream");
        }
      } else {
        for (ContentStream stream : streams) {
          documentLoader.load(req, rsp, stream);
        }
        RequestHandlerUtils.handleCommit(processor, params, false);
        RequestHandlerUtils.handleRollback(processor, params, false);
      }
    } finally {
      processor.finish();
    }
  }
  protected abstract ContentStreamLoader newLoader(SolrQueryRequest req, UpdateRequestProcessor processor);
}
