package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.StandardQueryNodeProcessorPipeline;
public class StandardQueryConfigHandler extends QueryConfigHandler {
  public StandardQueryConfigHandler() {
    addFieldConfigListener(new FieldBoostMapFCListener(this));
    addFieldConfigListener(new FieldDateResolutionFCListener(this));
    addAttribute(RangeCollatorAttribute.class);
    addAttribute(DefaultOperatorAttribute.class);
    addAttribute(AnalyzerAttribute.class);
    addAttribute(FuzzyAttribute.class);
    addAttribute(LowercaseExpandedTermsAttribute.class);
    addAttribute(MultiTermRewriteMethodAttribute.class);
    addAttribute(AllowLeadingWildcardAttribute.class);
    addAttribute(PositionIncrementsAttribute.class);
    addAttribute(LocaleAttribute.class);
    addAttribute(DefaultPhraseSlopAttribute.class);
    addAttribute(MultiTermRewriteMethodAttribute.class);   
  }
}
