package org.apache.solr;
import junit.framework.TestCase;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.handler.StandardRequestHandler;
import org.apache.solr.handler.admin.LukeRequestHandler;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.search.LRUCache;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
public class SolrInfoMBeanTest extends TestCase 
{
  public void testCallMBeanInfo() throws Exception {
    List<Class> classes = new ArrayList<Class>();
    classes.addAll(getClassesForPackage(StandardRequestHandler.class.getPackage().getName()));
    classes.addAll(getClassesForPackage(SearchHandler.class.getPackage().getName()));
    classes.addAll(getClassesForPackage(SearchComponent.class.getPackage().getName()));
    classes.addAll(getClassesForPackage(LukeRequestHandler.class.getPackage().getName()));
    classes.addAll(getClassesForPackage(DefaultSolrHighlighter.class.getPackage().getName()));
    classes.addAll(getClassesForPackage(LRUCache.class.getPackage().getName()));
    int checked = 0;
    for( Class clazz : classes ) {
      if( SolrInfoMBean.class.isAssignableFrom( clazz ) ) {
        try {
          SolrInfoMBean info = (SolrInfoMBean)clazz.newInstance();
          assertNotNull( info.getName() );
          assertNotNull( info.getDescription() );
          assertNotNull( info.getSource() );
          assertNotNull( info.getSourceId() );
          assertNotNull( info.getVersion() );
          assertNotNull( info.getCategory() );
          if( info instanceof LRUCache ) {
            continue;
          }
          assertNotNull( info.toString() );
          assertNotNull( info.getDocs() + "" );
          assertNotNull( info.getStatistics()+"" );
          checked++;
        }
        catch( InstantiationException ex ) {
        }
      }
    }
    assertTrue( "there are at least 10 SolrInfoMBean that should be found in the classpath, found " + checked, checked > 10 );
  }
  private static List<Class> getClassesForPackage(String pckgname) throws Exception {
    ArrayList<File> directories = new ArrayList<File>();
    ClassLoader cld = Thread.currentThread().getContextClassLoader();
    String path = pckgname.replace('.', '/');
    Enumeration<URL> resources = cld.getResources(path);
    while (resources.hasMoreElements()) {
      directories.add(new File(resources.nextElement().getPath()));
    }
    ArrayList<Class> classes = new ArrayList<Class>();
    for (File directory : directories) {
      if (directory.exists()) {
        String[] files = directory.list();
        for (String file : files) {
          if (file.endsWith(".class")) {
             classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
          }
        }
      }
    }
    return classes;
  }
}
