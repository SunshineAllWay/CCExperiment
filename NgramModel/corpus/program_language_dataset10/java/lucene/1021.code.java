package org.apache.lucene.store.instantiated;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.io.Serializable;
class FieldSettings implements Serializable {
  FieldSettings() {
  }
  private Map<String, FieldSetting> fieldSettings = new HashMap<String, FieldSetting>();
  synchronized FieldSetting merge(FieldSetting fieldSetting) {
    FieldSetting setting = fieldSettings.get(fieldSetting.fieldName);
    if (setting == null) {
      setting = new FieldSetting(fieldSetting.fieldName);
      fieldSettings.put(fieldSetting.fieldName, setting);
    }
    if (fieldSetting.stored) {
      setting.stored = true;
    }
    if ("b3".equals(fieldSetting.fieldName)) {
      System.currentTimeMillis();
    }
    if (fieldSetting.indexed) {
      setting.indexed = true;
    }
    if (fieldSetting.tokenized) {
      setting.tokenized = true;
    }
    if (fieldSetting.storeTermVector) {
      setting.storeTermVector = true;
    }
    if (fieldSetting.storeOffsetWithTermVector) {
      setting.storeOffsetWithTermVector = true;
    }
    if (fieldSetting.storePositionWithTermVector) {
      setting.storePositionWithTermVector = true;
    }
    if (fieldSetting.storePayloads) {
      setting.storePayloads = true;
    }
    return setting;
  }
  FieldSetting get(String name) {
    return fieldSettings.get(name);
  }
  FieldSetting get(String name, boolean create) {
    FieldSetting fieldSetting = fieldSettings.get(name);
    if (create && fieldSetting == null) {
      fieldSetting = new FieldSetting(name);
      fieldSettings.put(name, fieldSetting);
    }
    return fieldSetting;
  }
  Collection<FieldSetting> values() {
    return fieldSettings.values();
  }
}
