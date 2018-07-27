package org.apache.solr.schema;
import java.util.Map;
import java.util.HashMap;
abstract class FieldProperties {
  final static int INDEXED             = 0x00000001;
  final static int TOKENIZED           = 0x00000002;
  final static int STORED              = 0x00000004;
  final static int BINARY              = 0x00000008;
  final static int OMIT_NORMS          = 0x00000010;
  final static int OMIT_TF_POSITIONS   = 0x00000020;
  final static int STORE_TERMVECTORS   = 0x00000040;
  final static int STORE_TERMPOSITIONS = 0x00000080;
  final static int STORE_TERMOFFSETS   = 0x00000100;
  final static int MULTIVALUED         = 0x00000200;
  final static int SORT_MISSING_FIRST  = 0x00000400;
  final static int SORT_MISSING_LAST   = 0x00000800;
  final static int REQUIRED            = 0x00001000;
  static final String[] propertyNames = {
          "indexed", "tokenized", "stored",
          "binary", "omitNorms", "omitTermFreqAndPositions",
          "termVectors", "termPositions", "termOffsets",
          "multiValued",
          "sortMissingFirst","sortMissingLast","required"
  };
  static final Map<String,Integer> propertyMap = new HashMap<String,Integer>();
  static {
    for (String prop : propertyNames) {
      propertyMap.put(prop, propertyNameToInt(prop));
    }
  }
  static String getPropertyName(int property) {
    return propertyNames[ Integer.numberOfTrailingZeros(property) ];
  }
  static int propertyNameToInt(String name) {
    for (int i=0; i<propertyNames.length; i++) {
      if (propertyNames[i].equals(name)) {
        return 1 << i;
      }
    }
    return 0;
  }
  static String propertiesToString(int properties) {
    StringBuilder sb = new StringBuilder();
    boolean first=true;
    while (properties != 0) {
      if (!first) sb.append(',');
      first=false;
      int bitpos = Integer.numberOfTrailingZeros(properties);
      sb.append(getPropertyName(1 << bitpos));
      properties &= ~(1<<bitpos);  
    }
    return sb.toString();
  }
  static boolean on(int bitfield, int props) {
    return (bitfield & props) != 0;
  }
  static boolean off(int bitfield, int props) {
    return (bitfield & props) == 0;
  }
  static int parseProperties(Map<String,String> properties, boolean which) {
    int props = 0;
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String val = entry.getValue();
      if(val == null) continue;
      if (Boolean.parseBoolean(val) == which) {
        props |= propertyNameToInt(entry.getKey());
      }
    }
    return props;
  }
}
