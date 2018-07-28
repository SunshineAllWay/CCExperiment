package org.apache.solr.update;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.PluginInfo;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
public class SolrIndexConfig {
  public static final Logger log = LoggerFactory.getLogger(SolrIndexConfig.class);
  public static final String defaultsName ="indexDefaults";
  public static final String DEFAULT_MERGE_POLICY_CLASSNAME = LogByteSizeMergePolicy.class.getName();
  public static final String DEFAULT_MERGE_SCHEDULER_CLASSNAME = ConcurrentMergeScheduler.class.getName();
  static final SolrIndexConfig defaultDefaults = new SolrIndexConfig();
  private SolrIndexConfig() {
    useCompoundFile = true;
    maxBufferedDocs = -1;
    maxMergeDocs = -1;
    mergeFactor = -1;
    ramBufferSizeMB = 16;
    maxFieldLength = -1;
    writeLockTimeout = -1;
    commitLockTimeout = -1;
    lockType = null;
    luceneAutoCommit = false;
    termIndexInterval = IndexWriter.DEFAULT_TERM_INDEX_INTERVAL;
    mergePolicyInfo = null;
    mergeSchedulerInfo = null;
  }
  public final boolean useCompoundFile;
  public final int maxBufferedDocs;
  public final int maxMergeDocs;
  public final int mergeFactor;
  public final double ramBufferSizeMB;
  public final int maxFieldLength;
  public final int writeLockTimeout;
  public final int commitLockTimeout;
  public final String lockType;
  public final PluginInfo mergePolicyInfo;
  public final PluginInfo mergeSchedulerInfo;
  public final boolean luceneAutoCommit;
  public final int termIndexInterval;
  public String infoStreamFile = null;
  public SolrIndexConfig(SolrConfig solrConfig, String prefix, SolrIndexConfig def)  {
    if (prefix == null)
      prefix = defaultsName;
    if (def == null)
      def = defaultDefaults;
    useCompoundFile=solrConfig.getBool(prefix+"/useCompoundFile", def.useCompoundFile);
    maxBufferedDocs=solrConfig.getInt(prefix+"/maxBufferedDocs",def.maxBufferedDocs);
    maxMergeDocs=solrConfig.getInt(prefix+"/maxMergeDocs",def.maxMergeDocs);
    mergeFactor=solrConfig.getInt(prefix+"/mergeFactor",def.mergeFactor);
    ramBufferSizeMB = solrConfig.getDouble(prefix+"/ramBufferSizeMB", def.ramBufferSizeMB);
    maxFieldLength=solrConfig.getInt(prefix+"/maxFieldLength",def.maxFieldLength);
    writeLockTimeout=solrConfig.getInt(prefix+"/writeLockTimeout", def.writeLockTimeout);
    commitLockTimeout=solrConfig.getInt(prefix+"/commitLockTimeout", def.commitLockTimeout);
    lockType=solrConfig.get(prefix+"/lockType", def.lockType);
    String str =  solrConfig.get(prefix+"/mergeScheduler/text()",null);
    if(str != null && str.trim().length() >0){
      log.warn("deprecated syntax : <mergeScheduler>[classname]</mergeScheduler>");
      Map<String,String> atrs = new HashMap<String, String>();
      atrs.put("class",str.trim());
      mergeSchedulerInfo = new PluginInfo("mergeScheduler",atrs,null,null);
    } else {
      mergeSchedulerInfo = getPluginInfo(prefix + "/mergeScheduler", solrConfig, def.mergeSchedulerInfo);
    }
    str =  solrConfig.get(prefix+"/mergePolicy/text()",null);
    if(str != null && str.trim().length() >0){
      log.warn("deprecated syntax : <mergePolicy>[classname]</mergePolicy>");
      Map<String,String> atrs = new HashMap<String, String>();
      atrs.put("class",str.trim());
      mergePolicyInfo = new PluginInfo("mergePolicy",atrs,null,null);
    } else {
      mergePolicyInfo = getPluginInfo(prefix + "/mergePolicy", solrConfig, def.mergePolicyInfo);
    }
    luceneAutoCommit = solrConfig.getBool(prefix + "/luceneAutoCommit", def.luceneAutoCommit);
    termIndexInterval = solrConfig.getInt(prefix + "/termIndexInterval", def.termIndexInterval);
    boolean infoStreamEnabled = solrConfig.getBool(prefix + "/infoStream", false);
    if(infoStreamEnabled) {
      infoStreamFile= solrConfig.get(prefix + "/infoStream/@file", null);
      log.info("IndexWriter infoStream debug log is enabled: " + infoStreamFile);
    }
  }
  private PluginInfo getPluginInfo(String path, SolrConfig solrConfig, PluginInfo def)  {
    List<PluginInfo> l = solrConfig.readPluginInfos(path, false, true);
    return l.isEmpty() ? def : l.get(0);
  }
}
