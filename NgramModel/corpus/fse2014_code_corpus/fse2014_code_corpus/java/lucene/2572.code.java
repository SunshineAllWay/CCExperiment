package org.apache.solr.update;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicLong;
import java.io.IOException;
import java.net.URL;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
public class DirectUpdateHandler2 extends UpdateHandler {
  AtomicLong addCommands = new AtomicLong();
  AtomicLong addCommandsCumulative = new AtomicLong();
  AtomicLong deleteByIdCommands= new AtomicLong();
  AtomicLong deleteByIdCommandsCumulative= new AtomicLong();
  AtomicLong deleteByQueryCommands= new AtomicLong();
  AtomicLong deleteByQueryCommandsCumulative= new AtomicLong();
  AtomicLong expungeDeleteCommands = new AtomicLong();
  AtomicLong mergeIndexesCommands = new AtomicLong();
  AtomicLong commitCommands= new AtomicLong();
  AtomicLong optimizeCommands= new AtomicLong();
  AtomicLong rollbackCommands= new AtomicLong();
  AtomicLong numDocsPending= new AtomicLong();
  AtomicLong numErrors = new AtomicLong();
  AtomicLong numErrorsCumulative = new AtomicLong();
  protected final CommitTracker tracker;
  protected final Lock iwAccess, iwCommit;
  protected IndexWriter writer;
  public DirectUpdateHandler2(SolrCore core) throws IOException {
    super(core);
    ReadWriteLock rwl = new ReentrantReadWriteLock();
    iwAccess = rwl.readLock();
    iwCommit = rwl.writeLock();
    tracker = new CommitTracker();
  }
  private void deleteAll() throws IOException {
    core.log.info(core.getLogId()+"REMOVING ALL DOCUMENTS FROM INDEX");
    closeWriter();
    writer = createMainIndexWriter("DirectUpdateHandler2", true);
  }
  protected void openWriter() throws IOException {
    if (writer==null) {
      writer = createMainIndexWriter("DirectUpdateHandler2", false);
    }
  }
  protected void closeWriter() throws IOException {
    try {
      numDocsPending.set(0);
      if (writer!=null) writer.close();
    } finally {
      writer=null;
    }
  }
  protected void rollbackWriter() throws IOException {
    try {
      numDocsPending.set(0);
      if (writer!=null) writer.rollback();
    } finally {
      writer = null;
    }
  }
  public int addDoc(AddUpdateCommand cmd) throws IOException {
    addCommands.incrementAndGet();
    addCommandsCumulative.incrementAndGet();
    int rc=-1;
    if( idField == null ) {
      cmd.allowDups = true;
      cmd.overwriteCommitted = false;
      cmd.overwritePending = false;
    }
    iwAccess.lock();
    try {
      synchronized (this) {
        openWriter();
        tracker.addedDocument( cmd.commitWithin );
      } 
			Term updateTerm = null;
      if (cmd.overwriteCommitted || cmd.overwritePending) {
        if (cmd.indexedId == null) {
          cmd.indexedId = getIndexedId(cmd.doc);
        }
        Term idTerm = this.idTerm.createTerm(cmd.indexedId);
        boolean del = false;
        if (cmd.updateTerm == null) {
          updateTerm = idTerm;
        } else {
          del = true;
        	updateTerm = cmd.updateTerm;
        }
        writer.updateDocument(updateTerm, cmd.getLuceneDocument(schema));
        if(del) { 
          BooleanQuery bq = new BooleanQuery();
          bq.add(new BooleanClause(new TermQuery(updateTerm), Occur.MUST_NOT));
          bq.add(new BooleanClause(new TermQuery(idTerm), Occur.MUST));
          writer.deleteDocuments(bq);
        }
      } else {
        writer.addDocument(cmd.getLuceneDocument(schema));
      }
      rc = 1;
    } finally {
      iwAccess.unlock();
      if (rc!=1) {
        numErrors.incrementAndGet();
        numErrorsCumulative.incrementAndGet();
      } else {
        numDocsPending.incrementAndGet();
      }
    }
    return rc;
  }
  public void delete(DeleteUpdateCommand cmd) throws IOException {
    deleteByIdCommands.incrementAndGet();
    deleteByIdCommandsCumulative.incrementAndGet();
    if (!cmd.fromPending && !cmd.fromCommitted) {
      numErrors.incrementAndGet();
      numErrorsCumulative.incrementAndGet();
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"meaningless command: " + cmd);
    }
    if (!cmd.fromPending || !cmd.fromCommitted) {
      numErrors.incrementAndGet();
      numErrorsCumulative.incrementAndGet();
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"operation not supported" + cmd);
    }
    iwCommit.lock();
    try {
      openWriter();
      writer.deleteDocuments(idTerm.createTerm(idFieldType.toInternal(cmd.id)));
    } finally {
      iwCommit.unlock();
    }
    if( tracker.timeUpperBound > 0 ) {
      tracker.scheduleCommitWithin( tracker.timeUpperBound );
    }
  }
   public void deleteByQuery(DeleteUpdateCommand cmd) throws IOException {
     deleteByQueryCommands.incrementAndGet();
     deleteByQueryCommandsCumulative.incrementAndGet();
     if (!cmd.fromPending && !cmd.fromCommitted) {
       numErrors.incrementAndGet();
       numErrorsCumulative.incrementAndGet();
       throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"meaningless command: " + cmd);
     }
     if (!cmd.fromPending || !cmd.fromCommitted) {
       numErrors.incrementAndGet();
       numErrorsCumulative.incrementAndGet();
       throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"operation not supported" + cmd);
     }
    boolean madeIt=false;
    boolean delAll=false;
    try {
     Query q = QueryParsing.parseQuery(cmd.query, schema);
     delAll = MatchAllDocsQuery.class == q.getClass();
     iwCommit.lock();
     try {
       if (delAll) {
         deleteAll();
       } else {
        openWriter();
        writer.deleteDocuments(q);
       }
     } finally {
       iwCommit.unlock();
     }
     madeIt=true;
     if( tracker.timeUpperBound > 0 ) {
       tracker.scheduleCommitWithin( tracker.timeUpperBound );
     }
    } finally {
      if (!madeIt) {
        numErrors.incrementAndGet();
        numErrorsCumulative.incrementAndGet();
      }
    }
  }
  public int mergeIndexes(MergeIndexesCommand cmd) throws IOException {
    mergeIndexesCommands.incrementAndGet();
    int rc = -1;
    iwCommit.lock();
    try {
      log.info("start " + cmd);
      Directory[] dirs = cmd.dirs;
      if (dirs != null && dirs.length > 0) {
        openWriter();
        writer.addIndexesNoOptimize(dirs);
        rc = 1;
      } else {
        rc = 0;
      }
      log.info("end_mergeIndexes");
    } finally {
      iwCommit.unlock();
    }
    if (rc == 1 && tracker.timeUpperBound > 0) {
      tracker.scheduleCommitWithin(tracker.timeUpperBound);
    }
    return rc;
  }
   public void forceOpenWriter() throws IOException  {
    iwCommit.lock();
    try {
      openWriter();
    } finally {
      iwCommit.unlock();
    }
  }
  public void commit(CommitUpdateCommand cmd) throws IOException {
    if (cmd.optimize) {
      optimizeCommands.incrementAndGet();
    } else {
      commitCommands.incrementAndGet();
      if (cmd.expungeDeletes) expungeDeleteCommands.incrementAndGet();
    }
    Future[] waitSearcher = null;
    if (cmd.waitSearcher) {
      waitSearcher = new Future[1];
    }
    boolean error=true;
    iwCommit.lock();
    try {
      log.info("start "+cmd);
      if (cmd.optimize) {
        openWriter();
        writer.optimize(cmd.maxOptimizeSegments);
      } else if (cmd.expungeDeletes) {
        openWriter();
        writer.expungeDeletes();
      }
      closeWriter();
      callPostCommitCallbacks();
      if (cmd.optimize) {
        callPostOptimizeCallbacks();
      }
      core.getSearcher(true,false,waitSearcher);
      tracker.didCommit();
      log.info("end_commit_flush");
      error=false;
    }
    finally {
      iwCommit.unlock();
      addCommands.set(0);
      deleteByIdCommands.set(0);
      deleteByQueryCommands.set(0);
      numErrors.set(error ? 1 : 0);
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
  }
  public void rollback(RollbackUpdateCommand cmd) throws IOException {
    rollbackCommands.incrementAndGet();
    boolean error=true;
    iwCommit.lock();
    try {
      log.info("start "+cmd);
      rollbackWriter();
      tracker.didRollback();
      log.info("end_rollback");
      error=false;
    }
    finally {
      iwCommit.unlock();
      addCommandsCumulative.set(
          addCommandsCumulative.get() - addCommands.getAndSet( 0 ) );
      deleteByIdCommandsCumulative.set(
          deleteByIdCommandsCumulative.get() - deleteByIdCommands.getAndSet( 0 ) );
      deleteByQueryCommandsCumulative.set(
          deleteByQueryCommandsCumulative.get() - deleteByQueryCommands.getAndSet( 0 ) );
      numErrors.set(error ? 1 : 0);
    }
  }
  public void close() throws IOException {
    log.info("closing " + this);
    iwCommit.lock();
    try{
      if( tracker.pending != null ) {
        tracker.pending.cancel( true );
        tracker.pending = null;
      }
      tracker.scheduler.shutdown();
      closeWriter();
    } finally {
      iwCommit.unlock();
    }
    log.info("closed " + this);
  }
  class CommitTracker implements Runnable
  {
    public final int DOC_COMMIT_DELAY_MS = 250;
    int docsUpperBound;
    long timeUpperBound;
    private final ScheduledExecutorService scheduler =
       Executors.newScheduledThreadPool(1);
    private ScheduledFuture pending;
    long docsSinceCommit;
    int autoCommitCount = 0;
    long lastAddedTime = -1;
    public CommitTracker() {
      docsSinceCommit = 0;
      pending = null;
      docsUpperBound = core.getSolrConfig().getUpdateHandlerInfo().autoCommmitMaxDocs;   
      timeUpperBound = core.getSolrConfig().getUpdateHandlerInfo().autoCommmitMaxTime;    
      SolrCore.log.info("AutoCommit: " + this);
    }
    public synchronized void scheduleCommitWithin(long commitMaxTime)
    {
      _scheduleCommitWithin( commitMaxTime );
    }
    private void _scheduleCommitWithin(long commitMaxTime)
    {
      if( pending != null &&
          pending.getDelay(TimeUnit.MILLISECONDS) >= commitMaxTime )
      {
        pending.cancel(false);
        pending = null;
      }
      if( pending == null ) {
        pending = scheduler.schedule( this, commitMaxTime, TimeUnit.MILLISECONDS );
      }
    }
    public void addedDocument( int commitWithin ) {
      docsSinceCommit++;
      lastAddedTime = System.currentTimeMillis();
      if( docsUpperBound > 0 && (docsSinceCommit > docsUpperBound) ) {
        _scheduleCommitWithin( DOC_COMMIT_DELAY_MS );
      }
      long ctime = (commitWithin>0) ? commitWithin : timeUpperBound;
      if( ctime > 0 ) {
        _scheduleCommitWithin( ctime );
      }
    }
    public void didCommit() {
      if( pending != null ) {
        pending.cancel(false);
        pending = null; 
      }
      docsSinceCommit = 0;
    }
    public void didRollback() {
      if( pending != null ) {
        pending.cancel(false);
        pending = null; 
      }
      docsSinceCommit = 0;
    }
    public synchronized void run() {
      long started = System.currentTimeMillis();
      try {
        CommitUpdateCommand command = new CommitUpdateCommand( false );
        command.waitFlush = true;
        command.waitSearcher = true;
        commit( command );
        autoCommitCount++;
      }
      catch (Exception e) {
        log.error( "auto commit error..." );
        e.printStackTrace();
      }
      finally {
        pending = null;
      }
      if( lastAddedTime > started ) {
        if( docsUpperBound > 0 && docsSinceCommit > docsUpperBound ) {
          pending = scheduler.schedule( this, 100, TimeUnit.MILLISECONDS );
        }
        else if( timeUpperBound > 0 ) {
          pending = scheduler.schedule( this, timeUpperBound, TimeUnit.MILLISECONDS );
        }
      }
    }
    public synchronized int getCommitCount() { return autoCommitCount; }
    public String toString() {
      if(timeUpperBound > 0 || docsUpperBound > 0) {
        return
          (timeUpperBound > 0 ? ("if uncommited for " + timeUpperBound + "ms; ") : "") +
          (docsUpperBound > 0 ? ("if " + docsUpperBound + " uncommited docs ") : "");
      } else {
        return "disabled";
      }
    }
  }
  public String getName() {
    return DirectUpdateHandler2.class.getName();
  }
  public String getVersion() {
    return SolrCore.version;
  }
  public String getDescription() {
    return "Update handler that efficiently directly updates the on-disk main lucene index";
  }
  public Category getCategory() {
    return Category.UPDATEHANDLER;
  }
  public String getSourceId() {
    return "$Id: DirectUpdateHandler2.java 824380 2009-10-12 15:18:08Z koji $";
  }
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/update/DirectUpdateHandler2.java $";
  }
  public URL[] getDocs() {
    return null;
  }
  public NamedList getStatistics() {
    NamedList lst = new SimpleOrderedMap();
    lst.add("commits", commitCommands.get());
    if (tracker.docsUpperBound > 0) {
      lst.add("autocommit maxDocs", tracker.docsUpperBound);
    }
    if (tracker.timeUpperBound > 0) {
      lst.add("autocommit maxTime", "" + tracker.timeUpperBound + "ms");
    }
    lst.add("autocommits", tracker.autoCommitCount);
    lst.add("optimizes", optimizeCommands.get());
    lst.add("rollbacks", rollbackCommands.get());
    lst.add("expungeDeletes", expungeDeleteCommands.get());
    lst.add("docsPending", numDocsPending.get());
    lst.add("adds", addCommands.get());
    lst.add("deletesById", deleteByIdCommands.get());
    lst.add("deletesByQuery", deleteByQueryCommands.get());
    lst.add("errors", numErrors.get());
    lst.add("cumulative_adds", addCommandsCumulative.get());
    lst.add("cumulative_deletesById", deleteByIdCommandsCumulative.get());
    lst.add("cumulative_deletesByQuery", deleteByQueryCommandsCumulative.get());
    lst.add("cumulative_errors", numErrorsCumulative.get());
    return lst;
  }
  public String toString() {
    return "DirectUpdateHandler2" + getStatistics();
  }
}
