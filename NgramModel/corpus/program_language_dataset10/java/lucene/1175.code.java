package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.core.config.FieldConfigListener;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
public class FieldBoostMapFCListener implements FieldConfigListener {
  private static final long serialVersionUID = -5929802948798314067L;
  private QueryConfigHandler config = null;
  public FieldBoostMapFCListener(QueryConfigHandler config) {
    this.config = config;
  }
  public void buildFieldConfig(FieldConfig fieldConfig) {    
    if (this.config.hasAttribute(FieldBoostMapAttribute.class)) {
      FieldBoostMapAttribute fieldBoostMapAttr = this.config.getAttribute(FieldBoostMapAttribute.class);
      BoostAttribute boostAttr = fieldConfig.addAttribute(BoostAttribute.class);
      Float boost = fieldBoostMapAttr.getFieldBoostMap().get(fieldConfig.getFieldName());
      if (boost != null) {
        boostAttr.setBoost(boost.floatValue());
      }
    }
  }
}
