package org.apache.lucene.store.db;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.Environment;
public class SanityLoadLibrary {
  public static void main(String[] ignored) throws Exception {
    EnvironmentConfig envConfig = EnvironmentConfig.DEFAULT;
    envConfig.setAllowCreate(false);
    new Environment(null, envConfig);
  }
}
