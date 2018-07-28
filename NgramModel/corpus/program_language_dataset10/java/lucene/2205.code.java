package org.apache.solr.analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public abstract class BaseTokenFilterFactory extends BaseTokenStreamFactory implements TokenFilterFactory {
  public static final Logger log = LoggerFactory.getLogger(BaseTokenFilterFactory.class);
}
