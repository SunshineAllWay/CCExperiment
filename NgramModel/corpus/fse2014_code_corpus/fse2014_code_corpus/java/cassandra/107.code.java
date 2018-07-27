package org.apache.cassandra.config;
import org.apache.cassandra.db.ColumnFamilyType;
public class RawColumnFamily
{
    public String name;            
    public ColumnFamilyType column_type;
    public String compare_with;
    public String compare_subcolumns_with;
    public String comment;
    public double rows_cached = CFMetaData.DEFAULT_ROW_CACHE_SIZE; 
    public double keys_cached = CFMetaData.DEFAULT_KEY_CACHE_SIZE; 
    public double read_repair_chance = CFMetaData.DEFAULT_READ_REPAIR_CHANCE;
    public boolean replicate_on_write = CFMetaData.DEFAULT_REPLICATE_ON_WRITE;
    public int gc_grace_seconds = CFMetaData.DEFAULT_GC_GRACE_SECONDS;
    public String default_validation_class;
    public int min_compaction_threshold = CFMetaData.DEFAULT_MIN_COMPACTION_THRESHOLD;
    public int max_compaction_threshold = CFMetaData.DEFAULT_MAX_COMPACTION_THRESHOLD;
    public RawColumnDefinition[] column_metadata = new RawColumnDefinition[0];
    public int row_cache_save_period_in_seconds = CFMetaData.DEFAULT_ROW_CACHE_SAVE_PERIOD_IN_SECONDS;
    public int key_cache_save_period_in_seconds = CFMetaData.DEFAULT_KEY_CACHE_SAVE_PERIOD_IN_SECONDS;
    public int memtable_flush_after_mins = CFMetaData.DEFAULT_MEMTABLE_LIFETIME_IN_MINS;
    public Integer memtable_throughput_in_mb;
    public Double memtable_operations_in_millions;
}
