package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.core.config.FieldConfigListener;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
public class FieldDateResolutionFCListener implements FieldConfigListener {
  private static final long serialVersionUID = -5929802948798314067L;
  private QueryConfigHandler config = null;
  public FieldDateResolutionFCListener(QueryConfigHandler config) {
    this.config = config;
  }
  public void buildFieldConfig(FieldConfig fieldConfig) {
    DateResolutionAttribute fieldDateResAttr = fieldConfig
        .addAttribute(DateResolutionAttribute.class);
    DateTools.Resolution dateRes = null;
    if (this.config.hasAttribute(FieldDateResolutionMapAttribute.class)) {
      FieldDateResolutionMapAttribute dateResMapAttr = this.config
          .addAttribute(FieldDateResolutionMapAttribute.class);
      dateRes = dateResMapAttr.getFieldDateResolutionMap().get(
          fieldConfig.getFieldName().toString());
    }
    if (dateRes == null) {
      if (this.config.hasAttribute(DateResolutionAttribute.class)) {
        DateResolutionAttribute dateResAttr = this.config
            .addAttribute(DateResolutionAttribute.class);
        dateRes = dateResAttr.getDateResolution();
      }
    }
    fieldDateResAttr.setDateResolution(dateRes);
  }
}
