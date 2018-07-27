package org.apache.solr.common.params;
public interface CoreAdminParams 
{
  public final static String CORE = "core";
  public final static String PERSISTENT = "persistent";
  public final static String NAME = "name";
  public final static String DATA_DIR = "dataDir";
  public final static String OTHER = "other";
  public final static String ACTION = "action";
  public final static String SCHEMA = "schema";
  public final static String CONFIG = "config";
  public final static String INSTANCE_DIR = "instanceDir";
  public final static String FILE = "file";
  public final static String INDEX_DIR = "indexDir";
  public enum CoreAdminAction {
    STATUS,  
    LOAD,
    UNLOAD,
    RELOAD,
    CREATE,
    PERSIST,
    SWAP,
    RENAME,
    @Deprecated
    ALIAS,
    MERGEINDEXES;
    public static CoreAdminAction get( String p )
    {
      if( p != null ) {
        try {
          return CoreAdminAction.valueOf( p.toUpperCase() );
        }
        catch( Exception ex ) {}
      }
      return null; 
    }
  }
}
