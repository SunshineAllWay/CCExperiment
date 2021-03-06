package org.apache.solr.handler.admin;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.DirectoryFactory;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;
import org.apache.solr.update.MergeIndexesCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.lucene.store.Directory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
public class CoreAdminHandler extends RequestHandlerBase {
  protected final CoreContainer coreContainer;
  public CoreAdminHandler() {
    super();
    this.coreContainer = null;
  }
  public CoreAdminHandler(final CoreContainer coreContainer) {
    this.coreContainer = coreContainer;
  }
  @Override
  final public void init(NamedList args) {
    throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
            "CoreAdminHandler should not be configured in solrconf.xml\n" +
                    "it is a special Handler configured directly by the RequestDispatcher");
  }
  public CoreContainer getCoreContainer() {
    return this.coreContainer;
  }
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    CoreContainer cores = getCoreContainer();
    if (cores == null) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "Core container instance missing");
    }
    boolean doPersist = false;
    SolrParams params = req.getParams();
    CoreAdminAction action = CoreAdminAction.STATUS;
    String a = params.get(CoreAdminParams.ACTION);
    if (a != null) {
      action = CoreAdminAction.get(a);
      if (action == null) {
        doPersist = this.handleCustomAction(req, rsp);
      }
    }
    if (action != null) {
      switch (action) {
        case CREATE: {
          doPersist = this.handleCreateAction(req, rsp);
          break;
        }
        case RENAME: {
          doPersist = this.handleRenameAction(req, rsp);
          break;
        }
        case ALIAS: {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'ALIAS' is not supported " +
            req.getParams().get(CoreAdminParams.ACTION));
        }
        case UNLOAD: {
          doPersist = this.handleUnloadAction(req, rsp);
          break;
        }
        case STATUS: {
          doPersist = this.handleStatusAction(req, rsp);
          break;
        }
        case PERSIST: {
          doPersist = this.handlePersistAction(req, rsp);
          break;
        }
        case RELOAD: {
          doPersist = this.handleReloadAction(req, rsp);
          break;
        }
        case SWAP: {
          doPersist = this.handleSwapAction(req, rsp);
          break;
        }
        case MERGEINDEXES: {
          doPersist = this.handleMergeAction(req, rsp);
          break;
        }
        default: {
          doPersist = this.handleCustomAction(req, rsp);
          break;
        }
        case LOAD:
          break;
      }
    }
    if (doPersist) {
      cores.persist();
      rsp.add("saved", cores.getConfigFile().getAbsolutePath());
    }
    rsp.setHttpCaching(false);
  }
  protected boolean handleMergeAction(SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    boolean doPersist = false;
    SolrParams params = req.getParams();
    SolrParams required = params.required();
    String cname = required.get(CoreAdminParams.CORE);
    SolrCore core = coreContainer.getCore(cname);
    if (core != null) {
      try {
        doPersist = coreContainer.isPersistent();
        String[] dirNames = required.getParams(CoreAdminParams.INDEX_DIR);
        DirectoryFactory dirFactory = core.getDirectoryFactory();
        Directory[] dirs = new Directory[dirNames.length];
        for (int i = 0; i < dirNames.length; i++) {
          dirs[i] = dirFactory.open(dirNames[i]);
        }
        UpdateRequestProcessorChain processorChain =
                core.getUpdateProcessingChain(params.get(UpdateParams.UPDATE_PROCESSOR));
        SolrQueryRequest wrappedReq = new LocalSolrQueryRequest(core, req.getParams());
        UpdateRequestProcessor processor =
                processorChain.createProcessor(wrappedReq, rsp);
        processor.processMergeIndexes(new MergeIndexesCommand(dirs));
      } finally {
        core.close();
      }
    }
    return doPersist;
  }
  protected boolean handleCustomAction(SolrQueryRequest req, SolrQueryResponse rsp) {
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unsupported operation: " +
            req.getParams().get(CoreAdminParams.ACTION));
  }
  protected boolean handleCreateAction(SolrQueryRequest req, SolrQueryResponse rsp) throws SolrException {
    try {
      SolrParams params = req.getParams();
      String name = params.get(CoreAdminParams.NAME);
      CoreDescriptor dcore = new CoreDescriptor(coreContainer, name, params.get(CoreAdminParams.INSTANCE_DIR));
      String opts = params.get(CoreAdminParams.CONFIG);
      if (opts != null)
        dcore.setConfigName(opts);
      opts = params.get(CoreAdminParams.SCHEMA);
      if (opts != null)
        dcore.setSchemaName(opts);
      opts = params.get(CoreAdminParams.DATA_DIR);
      if (opts != null)
        dcore.setDataDir(opts);
      dcore.setCoreProperties(null);
      SolrCore core = coreContainer.create(dcore);
      coreContainer.register(name, core, false);
      rsp.add("core", core.getName());
      return coreContainer.isPersistent();
    } catch (Exception ex) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "Error executing default implementation of CREATE", ex);
    }
  }
  protected boolean handleRenameAction(SolrQueryRequest req, SolrQueryResponse rsp) throws SolrException {
    SolrParams params = req.getParams();
    String name = params.get(CoreAdminParams.OTHER);
    String cname = params.get(CoreAdminParams.CORE);
    boolean doPersist = false;
    if (cname.equals(name)) return doPersist;
    SolrCore core = coreContainer.getCore(cname);
    if (core != null) {
      doPersist = coreContainer.isPersistent();
      coreContainer.register(name, core, false);
      coreContainer.remove(cname);
      core.close();
    }
    return doPersist;
  }
  @Deprecated
  protected boolean handleAliasAction(SolrQueryRequest req, SolrQueryResponse rsp) {
    SolrParams params = req.getParams();
    String name = params.get(CoreAdminParams.OTHER);
    String cname = params.get(CoreAdminParams.CORE);
    boolean doPersist = false;
    if (cname.equals(name)) return doPersist;
    SolrCore core = coreContainer.getCore(cname);
    if (core != null) {
      doPersist = coreContainer.isPersistent();
      coreContainer.register(name, core, false);
    }
    return doPersist;
  }
  protected boolean handleUnloadAction(SolrQueryRequest req, SolrQueryResponse rsp) throws SolrException {
    SolrParams params = req.getParams();
    String cname = params.get(CoreAdminParams.CORE);
    SolrCore core = coreContainer.remove(cname);
    if(core == null){
       throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "No such core exists '"+cname+"'");
    }
    core.close();
    return coreContainer.isPersistent();
  }
  protected boolean handleStatusAction(SolrQueryRequest req, SolrQueryResponse rsp)
          throws SolrException {
    SolrParams params = req.getParams();
    String cname = params.get(CoreAdminParams.CORE);
    boolean doPersist = false;
    NamedList<Object> status = new SimpleOrderedMap<Object>();
    try {
      if (cname == null) {
        for (String name : coreContainer.getCoreNames()) {
          status.add(name, getCoreStatus(coreContainer, name));
        }
      } else {
        status.add(cname, getCoreStatus(coreContainer, cname));
      }
      rsp.add("status", status);
      doPersist = false; 
      return doPersist;
    } catch (Exception ex) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
              "Error handling 'status' action ", ex);
    }
  }
  protected boolean handlePersistAction(SolrQueryRequest req, SolrQueryResponse rsp)
          throws SolrException {
    SolrParams params = req.getParams();
    boolean doPersist = false;
    String fileName = params.get(CoreAdminParams.FILE);
    if (fileName != null) {
      File file = new File(coreContainer.getConfigFile().getParentFile(), fileName);
      coreContainer.persistFile(file);
      rsp.add("saved", file.getAbsolutePath());
      doPersist = false;
    } else if (!coreContainer.isPersistent()) {
      throw new SolrException(SolrException.ErrorCode.FORBIDDEN, "Persistence is not enabled");
    } else
      doPersist = true;
    return doPersist;
  }
  protected boolean handleReloadAction(SolrQueryRequest req, SolrQueryResponse rsp) {
    SolrParams params = req.getParams();
    String cname = params.get(CoreAdminParams.CORE);
    try {
      coreContainer.reload(cname);
      return false; 
    } catch (Exception ex) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Error handling 'reload' action", ex);
    }
  }
  protected boolean handleSwapAction(SolrQueryRequest req, SolrQueryResponse rsp) {
    final SolrParams params = req.getParams();
    final SolrParams required = params.required();
    final String cname = params.get(CoreAdminParams.CORE);
    boolean doPersist = params.getBool(CoreAdminParams.PERSISTENT, coreContainer.isPersistent());
    String other = required.get(CoreAdminParams.OTHER);
    coreContainer.swap(cname, other);
    return doPersist;
  }
  protected NamedList<Object> getCoreStatus(CoreContainer cores, String cname) throws IOException {
    NamedList<Object> info = new SimpleOrderedMap<Object>();
    SolrCore core = cores.getCore(cname);
    if (core != null) {
      try {
        info.add("name", core.getName());
        info.add("instanceDir", normalizePath(core.getResourceLoader().getInstanceDir()));
        info.add("dataDir", normalizePath(core.getDataDir()));
        info.add("startTime", new Date(core.getStartTime()));
        info.add("uptime", System.currentTimeMillis() - core.getStartTime());
        RefCounted<SolrIndexSearcher> searcher = core.getSearcher();
        try {
          info.add("index", LukeRequestHandler.getIndexInfo(searcher.get().getReader(), false));
        } finally {
          searcher.decref();
        }
      } finally {
        core.close();
      }
    }
    return info;
  }
  protected static String normalizePath(String path) {
    if (path == null)
      return null;
    path = path.replace('/', File.separatorChar);
    path = path.replace('\\', File.separatorChar);
    return path;
  }
  @Override
  public String getDescription() {
    return "Manage Multiple Solr Cores";
  }
  @Override
  public String getVersion() {
    return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: CoreAdminHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/admin/CoreAdminHandler.java $";
  }
}
