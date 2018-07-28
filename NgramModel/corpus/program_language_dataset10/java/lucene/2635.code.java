package org.apache.solr.client.solrj.beans;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface Field {
  public static final String DEFAULT ="#default";
  String value() default DEFAULT;
}
