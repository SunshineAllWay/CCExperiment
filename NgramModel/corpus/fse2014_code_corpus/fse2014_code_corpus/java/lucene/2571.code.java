package org.apache.solr.update;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import java.util.HashSet;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.net.URL;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.update.UpdateHandler;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
public class DirectUpdateHandler extends UpdateHandler {
  final HashSet<String> pset;
  IndexWriter writer;
  SolrIndexSearcher searcher;
  int numAdds=0;     
  int numPending=0;  
  int numDeleted=0;  
  public DirectUpdateHandler(SolrCore core) throws IOException {
    super(core);
    pset = new HashSet<String>(256);
  }
  protected void openWriter() throws IOException {
    if (writer==null) {
      writer = createMainIndexWriter("DirectUpdateHandler", false);
    }
  }
  protected void closeWriter() throws IOException {
    try {
      if (writer!=null) writer.close();
    } finally {
      writer=null;
    }
  }
  protected void openSearcher() throws IOException {
    if (searcher==null) {
      searcher = core.newSearcher("DirectUpdateHandler");
    }
  }
  protected void closeSearcher() throws IOException {
    try {
      if (searcher!=null) searcher.close();
    } finally {
      searcher=null;
    }
  }
  protected void doAdd(Document doc) throws IOException {
    closeSearcher(); openWriter();
    writer.addDocument(doc);
  }
  protected boolean existsInIndex(String indexedId) throws IOException {
    if (idField == null) throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Operation requires schema to have a unique key field");
    closeWriter();
    openSearcher();
    IndexReader ir = searcher.getReader();
    TermDocs tdocs = null;
    boolean exists=false;
    try {
      tdocs = ir.termDocs(idTerm(indexedId));
      if (tdocs.next()) exists=true;
    } finally {
      try { if (tdocs != null) tdocs.close(); } catch (Exception e) {}
    }
    return exists;
  }
  protected int deleteInIndex(String indexedId) throws IOException {
    if (idField == null) throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Operation requires schema to have a unique key field");
    closeWriter(); openSearcher();
    IndexReader ir = searcher.getReader();
    TermDocs tdocs = null;
    int num=0;
    try {
      Term term = new Term(idField.getName(), indexedId);
      num = ir.deleteDocuments(term);
      if (core.log.isTraceEnabled()) {
        core.log.trace( core.getLogId()+"deleted " + num + " docs matching id " + idFieldType.indexedToReadable(indexedId));
      }
    } finally {
      try { if (tdocs != null) tdocs.close(); } catch (Exception e) {}
    }
    return num;
  }
  protected void overwrite(String indexedId, Document doc) throws IOException {
    if (indexedId ==null) indexedId =getIndexedId(doc);
    deleteInIndex(indexedId);
    doAdd(doc);
  }
  public void delete(DeleteUpdateCommand cmd) throws IOException {
    if (!cmd.fromPending && !cmd.fromCommitted)
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"meaningless command: " + cmd);
    if (!cmd.fromPending || !cmd.fromCommitted)
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"operation not supported" + cmd);
    String indexedId = idFieldType.toInternal(cmd.id);
    synchronized(this) {
      deleteInIndex(indexedId);
      pset.remove(indexedId);
    }
  }
  public void deleteByQuery(DeleteUpdateCommand cmd) throws IOException {
    if (!cmd.fromPending && !cmd.fromCommitted)
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"meaningless command: " + cmd);
    if (!cmd.fromPending || !cmd.fromCommitted)
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"operation not supported: " + cmd);
    Query q = QueryParsing.parseQuery(cmd.query, schema);
    int totDeleted = 0;
    synchronized(this) {
      closeWriter(); openSearcher();
      final DeleteHitCollector deleter = new DeleteHitCollector(searcher);
      searcher.search(q, null, deleter);
      totDeleted = deleter.deleted;
    }
    if (core.log.isDebugEnabled()) {
      core.log.debug(core.getLogId()+"docs deleted:" + totDeleted);
    }
  }
  public int mergeIndexes(MergeIndexesCommand cmd) throws IOException {
    throw new SolrException(
        SolrException.ErrorCode.BAD_REQUEST,
        "DirectUpdateHandler doesn't support mergeIndexes. Use DirectUpdateHandler2 instead.");
  }
  public void commit(CommitUpdateCommand cmd) throws IOException {
    Future[] waitSearcher = null;
    if (cmd.waitSearcher) {
      waitSearcher = new Future[1];
    }
    synchronized (this) {
      pset.clear();
      closeSearcher();  
      if (cmd.optimize || cmd.expungeDeletes) {
        openWriter();  
        if(cmd.optimize) writer.optimize(cmd.maxOptimizeSegments);
        if(cmd.expungeDeletes) writer.expungeDeletes(cmd.expungeDeletes);
      }
      closeWriter();
      callPostCommitCallbacks();
      if (cmd.optimize) {
        callPostOptimizeCallbacks();
      }
      core.getSearcher(true,false,waitSearcher);
    }
    if (waitSearcher!=null && waitSearcher[0] != null) {
      try {
        waitSearcher[0].get();
      } catch (InterruptedException e) {
        SolrException.log(log,e);
      } catch (ExecutionException e) {
        SolrException.log(log,e);
      }
    }
    return;
  }
  public void rollback(RollbackUpdateCommand cmd) throws IOException {
    throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,
        "DirectUpdateHandler doesn't support rollback. Use DirectUpdateHandler2 instead.");
  }
  protected int addNoOverwriteNoDups(AddUpdateCommand cmd) throws IOException {
    if (cmd.indexedId ==null) {
      cmd.indexedId =getIndexedId(cmd.doc);
    }
    synchronized (this) {
      if (existsInIndex(cmd.indexedId)) return 0;
      doAdd(cmd.doc);
    }
    return 1;
  }
  protected int addConditionally(AddUpdateCommand cmd) throws IOException {
    if (cmd.indexedId ==null) {
      cmd.indexedId =getIndexedId(cmd.doc);
    }
    synchronized(this) {
      if (pset.contains(cmd.indexedId)) return 0;
      pset.add(cmd.indexedId);
      overwrite(cmd.indexedId,cmd.doc);
      return 1;
    }
  }
  protected synchronized int overwriteBoth(AddUpdateCommand cmd) throws IOException {
    overwrite(cmd.indexedId, cmd.doc);
    return 1;
  }
  protected synchronized int allowDups(AddUpdateCommand cmd) throws IOException {
    doAdd(cmd.doc);
    return 1;
  }
  public int addDoc(AddUpdateCommand cmd) throws IOException {
    if( idField == null ) {
      cmd.allowDups = true;
      cmd.overwriteCommitted = false;
      cmd.overwritePending = false;
    }
    if (!cmd.allowDups && !cmd.overwritePending && !cmd.overwriteCommitted) {
      return addNoOverwriteNoDups(cmd);
    } else if (!cmd.allowDups && !cmd.overwritePending && cmd.overwriteCommitted) {
      return addConditionally(cmd);
    } else if (!cmd.allowDups && cmd.overwritePending && !cmd.overwriteCommitted) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"unsupported param combo:" + cmd);
    } else if (!cmd.allowDups && cmd.overwritePending && cmd.overwriteCommitted) {
      return overwriteBoth(cmd);
    } else if (cmd.allowDups && !cmd.overwritePending && !cmd.overwriteCommitted) {
      return allowDups(cmd);
    } else if (cmd.allowDups && !cmd.overwritePending && cmd.overwriteCommitted) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"unsupported param combo:" + cmd);
    } else if (cmd.allowDups && cmd.overwritePending && !cmd.overwriteCommitted) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"unsupported param combo:" + cmd);
    } else if (cmd.allowDups && cmd.overwritePending && cmd.overwriteCommitted) {
      return overwriteBoth(cmd);
    }
    throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"unsupported param combo:" + cmd);
  }
  public void close() throws IOException {
    synchronized(this) {
      closeSearcher();
      closeWriter();
    }
  }
  public String getName() {
    return DirectUpdateHandler.class.getName();
  }
  public String getVersion() {
    return SolrCore.version;
  }
  public String getDescription() {
    return "Update handler that directly changes the on-disk main lucene index";
  }
  public Category getCategory() {
    return Category.CORE;
  }
  public String getSourceId() {
    return "$Id: DirectUpdateHandler.java 805774 2009-08-19 12:21:22Z noble $";
  }
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/update/DirectUpdateHandler.java $";
  }
  public URL[] getDocs() {
    return null;
  }
  public NamedList getStatistics() {
    NamedList lst = new SimpleOrderedMap();
    return lst;
  }
}
