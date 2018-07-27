package org.apache.lucene.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.CacheEntry;
public final class FieldCacheSanityChecker {
  private RamUsageEstimator ramCalc = null;
  public FieldCacheSanityChecker() {
  }
  public void setRamUsageEstimator(RamUsageEstimator r) {
    ramCalc = r;
  }
  public static Insanity[] checkSanity(FieldCache cache) {
    return checkSanity(cache.getCacheEntries());
  }
  public static Insanity[] checkSanity(CacheEntry... cacheEntries) {
    FieldCacheSanityChecker sanityChecker = new FieldCacheSanityChecker();
    sanityChecker.setRamUsageEstimator(new RamUsageEstimator(false));
    return sanityChecker.check(cacheEntries);
  }
  public Insanity[] check(CacheEntry... cacheEntries) {
    if (null == cacheEntries || 0 == cacheEntries.length) 
      return new Insanity[0];
    if (null != ramCalc) {
      for (int i = 0; i < cacheEntries.length; i++) {
        cacheEntries[i].estimateSize(ramCalc);
      }
    }
    final MapOfSets<Integer, CacheEntry> valIdToItems = new MapOfSets<Integer, CacheEntry>(new HashMap<Integer, Set<CacheEntry>>(17));
    final MapOfSets<ReaderField, Integer> readerFieldToValIds = new MapOfSets<ReaderField, Integer>(new HashMap<ReaderField, Set<Integer>>(17));
    final Set<ReaderField> valMismatchKeys = new HashSet<ReaderField>();
    for (int i = 0; i < cacheEntries.length; i++) {
      final CacheEntry item = cacheEntries[i];
      final Object val = item.getValue();
      if (val instanceof FieldCache.CreationPlaceholder)
        continue;
      final ReaderField rf = new ReaderField(item.getReaderKey(), 
                                            item.getFieldName());
      final Integer valId = Integer.valueOf(System.identityHashCode(val));
      valIdToItems.put(valId, item);
      if (1 < readerFieldToValIds.put(rf, valId)) {
        valMismatchKeys.add(rf);
      }
    }
    final List<Insanity> insanity = new ArrayList<Insanity>(valMismatchKeys.size() * 3);
    insanity.addAll(checkValueMismatch(valIdToItems, 
                                       readerFieldToValIds, 
                                       valMismatchKeys));
    insanity.addAll(checkSubreaders(valIdToItems, 
                                    readerFieldToValIds));
    return insanity.toArray(new Insanity[insanity.size()]);
  }
  private Collection<Insanity> checkValueMismatch(MapOfSets<Integer, CacheEntry> valIdToItems,
                                        MapOfSets<ReaderField, Integer> readerFieldToValIds,
                                        Set<ReaderField> valMismatchKeys) {
    final List<Insanity> insanity = new ArrayList<Insanity>(valMismatchKeys.size() * 3);
    if (! valMismatchKeys.isEmpty() ) { 
      final Map<ReaderField, Set<Integer>> rfMap = readerFieldToValIds.getMap();
      final Map<Integer, Set<CacheEntry>> valMap = valIdToItems.getMap();
      for (final ReaderField rf : valMismatchKeys) {
        final List<CacheEntry> badEntries = new ArrayList<CacheEntry>(valMismatchKeys.size() * 2);
        for(final Integer value: rfMap.get(rf)) {
          for (final CacheEntry cacheEntry : valMap.get(value)) {
            badEntries.add(cacheEntry);
          }
        }
        CacheEntry[] badness = new CacheEntry[badEntries.size()];
        badness = badEntries.toArray(badness);
        insanity.add(new Insanity(InsanityType.VALUEMISMATCH,
                                  "Multiple distinct value objects for " + 
                                  rf.toString(), badness));
      }
    }
    return insanity;
  }
  private Collection<Insanity> checkSubreaders( MapOfSets<Integer, CacheEntry>  valIdToItems,
                                      MapOfSets<ReaderField, Integer> readerFieldToValIds) {
    final List<Insanity> insanity = new ArrayList<Insanity>(23);
    Map<ReaderField, Set<ReaderField>> badChildren = new HashMap<ReaderField, Set<ReaderField>>(17);
    MapOfSets<ReaderField, ReaderField> badKids = new MapOfSets<ReaderField, ReaderField>(badChildren); 
    Map<Integer, Set<CacheEntry>> viToItemSets = valIdToItems.getMap();
    Map<ReaderField, Set<Integer>> rfToValIdSets = readerFieldToValIds.getMap();
    Set<ReaderField> seen = new HashSet<ReaderField>(17);
    Set<ReaderField> readerFields = rfToValIdSets.keySet();
    for (final ReaderField rf : readerFields) {
      if (seen.contains(rf)) continue;
      List<Object> kids = getAllDecendentReaderKeys(rf.readerKey);
      for (Object kidKey : kids) {
        ReaderField kid = new ReaderField(kidKey, rf.fieldName);
        if (badChildren.containsKey(kid)) {
          badKids.put(rf, kid);
          badKids.putAll(rf, badChildren.get(kid));
          badChildren.remove(kid);
        } else if (rfToValIdSets.containsKey(kid)) {
          badKids.put(rf, kid);
        }
        seen.add(kid);
      }
      seen.add(rf);
    }
    for (final ReaderField parent : badChildren.keySet()) {
      Set<ReaderField> kids = badChildren.get(parent);
      List<CacheEntry> badEntries = new ArrayList<CacheEntry>(kids.size() * 2);
      {
        for (final Integer value  : rfToValIdSets.get(parent)) {
          badEntries.addAll(viToItemSets.get(value));
        }
      }
      for (final ReaderField kid : kids) {
        for (final Integer value : rfToValIdSets.get(kid)) {
          badEntries.addAll(viToItemSets.get(value));
        }
      }
      CacheEntry[] badness = new CacheEntry[badEntries.size()];
      badness = badEntries.toArray(badness);
      insanity.add(new Insanity(InsanityType.SUBREADER,
                                "Found caches for decendents of " + 
                                parent.toString(),
                                badness));
    }
    return insanity;
  }
  private List<Object> getAllDecendentReaderKeys(Object seed) {
    List<Object> all = new ArrayList<Object>(17); 
    all.add(seed);
    for (int i = 0; i < all.size(); i++) {
      Object obj = all.get(i);
      if (obj instanceof IndexReader) {
        IndexReader[] subs = ((IndexReader)obj).getSequentialSubReaders();
        for (int j = 0; (null != subs) && (j < subs.length); j++) {
          all.add(subs[j].getFieldCacheKey());
        }
      }
    }
    return all.subList(1, all.size());
  }
  private final static class ReaderField {
    public final Object readerKey;
    public final String fieldName;
    public ReaderField(Object readerKey, String fieldName) {
      this.readerKey = readerKey;
      this.fieldName = fieldName;
    }
    @Override
    public int hashCode() {
      return System.identityHashCode(readerKey) * fieldName.hashCode();
    }
    @Override
    public boolean equals(Object that) {
      if (! (that instanceof ReaderField)) return false;
      ReaderField other = (ReaderField) that;
      return (this.readerKey == other.readerKey &&
              this.fieldName.equals(other.fieldName));
    }
    @Override
    public String toString() {
      return readerKey.toString() + "+" + fieldName;
    }
  }
  public final static class Insanity {
    private final InsanityType type;
    private final String msg;
    private final CacheEntry[] entries;
    public Insanity(InsanityType type, String msg, CacheEntry... entries) {
      if (null == type) {
        throw new IllegalArgumentException
          ("Insanity requires non-null InsanityType");
      }
      if (null == entries || 0 == entries.length) {
        throw new IllegalArgumentException
          ("Insanity requires non-null/non-empty CacheEntry[]");
      }
      this.type = type;
      this.msg = msg;
      this.entries = entries;
    }
    public InsanityType getType() { return type; }
    public String getMsg() { return msg; }
    public CacheEntry[] getCacheEntries() { return entries; }
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append(getType()).append(": ");
      String m = getMsg();
      if (null != m) buf.append(m);
      buf.append('\n');
      CacheEntry[] ce = getCacheEntries();
      for (int i = 0; i < ce.length; i++) {
        buf.append('\t').append(ce[i].toString()).append('\n');
      }
      return buf.toString();
    }
  }
  public final static class InsanityType {
    private final String label;
    private InsanityType(final String label) {
      this.label = label;
    }
    @Override
    public String toString() { return label; }
    public final static InsanityType SUBREADER 
      = new InsanityType("SUBREADER");
    public final static InsanityType VALUEMISMATCH 
      = new InsanityType("VALUEMISMATCH");
    public final static InsanityType EXPECTED
      = new InsanityType("EXPECTED");
  }
}
