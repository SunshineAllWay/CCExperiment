package org.apache.solr.util;
import java.io.Reader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.jar.*;
public class SuggestMissingFactories {
    public static void main(String[] args) throws ClassNotFoundException, IOException, NoSuchMethodException {
        final File[] files = new File[args.length];
        for (int i = 0; i < args.length; i++) {
            files[i] = new File(args[i]);
        }
        final FindClasses finder = new FindClasses(files);
        final ClassLoader cl = finder.getClassLoader();
        final Class TOKENSTREAM
            = cl.loadClass("org.apache.lucene.analysis.TokenStream");
        final Class TOKENIZER
            = cl.loadClass("org.apache.lucene.analysis.Tokenizer");
        final Class TOKENFILTER
            = cl.loadClass("org.apache.lucene.analysis.TokenFilter");
        final Class TOKENIZERFACTORY
            = cl.loadClass("org.apache.solr.analysis.TokenizerFactory");
        final Class TOKENFILTERFACTORY
            = cl.loadClass("org.apache.solr.analysis.TokenFilterFactory");
        final HashSet<Class> result
            = new HashSet<Class>(finder.findExtends(TOKENIZER));
        result.addAll(finder.findExtends(TOKENFILTER));
        result.removeAll(finder.findMethodReturns
                         (finder.findExtends(TOKENIZERFACTORY),
                          "create",
                          Reader.class).values());
        result.removeAll(finder.findMethodReturns
                         (finder.findExtends(TOKENFILTERFACTORY),
                          "create",
                          TOKENSTREAM).values());
        for (final Class c : result) {
            System.out.println(c.getName());
        }
    }
}
class FindClasses {
  public static void main(String[] args)
    throws ClassNotFoundException, IOException, NoSuchMethodException {
    FindClasses finder = new FindClasses(new File(args[1]));
    ClassLoader cl = finder.getClassLoader();
    Class clazz = cl.loadClass(args[0]);
    if (args.length == 2) {
      System.out.println("Finding all extenders of " + clazz.getName());
      for (Class c : finder.findExtends(clazz)) {
        System.out.println(c.getName());
      }
    } else {
      String methName = args[2];
      System.out.println("Finding all extenders of " + clazz.getName() +
                         " with method: " + methName);
      Class[] methArgs = new Class[args.length-3];
      for (int i = 3; i < args.length; i++) {
        methArgs[i-3] = cl.loadClass(args[i]);
      }
      Map<Class,Class> map = finder.findMethodReturns
        (finder.findExtends(clazz),methName, methArgs);
      for (Class key : map.keySet()) {
        System.out.println(key.getName() + " => " + map.get(key).getName());
      }
    }
  }
  private JarFile[] jarFiles;
  private ClassLoader cl;
  public FindClasses(File... jars) throws IOException {
    jarFiles = new JarFile[jars.length];
    URL[] urls = new URL[jars.length];
    try {
      for (int i =0; i < jars.length; i++) {
        jarFiles[i] = new JarFile(jars[i]);
        urls[i] = jars[i].toURI().toURL();
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException
        ("WTF, how can JarFile.toURL() be malformed?", e);
    }
    this.cl = new URLClassLoader(urls, this.getClass().getClassLoader());
  }
  public ClassLoader getClassLoader() {
    return this.cl;
  }
  public Collection<Class> findExtends(Class<?> clazz)
    throws ClassNotFoundException {
    HashSet<Class> results = new HashSet<Class>();
    for (JarFile jarFile : jarFiles) {
      for (Enumeration<JarEntry> e = jarFile.entries();
           e.hasMoreElements() ;) {
        String n = e.nextElement().getName();
        if (n.endsWith(".class")) {
          String cn = n.replace("/",".").substring(0,n.length()-6);
          Class<?> target;
          try {
            target = cl.loadClass(cn);
          } catch (NoClassDefFoundError e1) {
            throw new ClassNotFoundException
              ("Can't load: " + cn, e1);
          }
          if (clazz.isAssignableFrom(target)
              && !target.isAnonymousClass()) {
            int mods = target.getModifiers();
            if (!(Modifier.isAbstract(mods) ||
                  Modifier.isInterface(mods))) {
              results.add(target);
            }
          }
        }
      }
    }
    return results;
  }
  public Map<Class,Class> findMethodReturns(Collection<Class> clazzes,
                                            String methodName,
                                            Class... parameterTypes)
    throws NoSuchMethodException{
    HashMap<Class,Class> results = new HashMap<Class,Class>();
    for (Class clazz : clazzes) {
      try {
        Method m = clazz.getMethod(methodName, parameterTypes);
        results.put(clazz, m.getReturnType());
      } catch (NoSuchMethodException e) {
      }
    }
    return results;
  }
}
