package org.apache.lucene.messages;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
public class NLS {
  private static Map<String, Class<Object>> bundles = 
    new HashMap<String, Class<Object>>(0);
  protected NLS() {
  }
  public static String getLocalizedMessage(String key) {
    return getLocalizedMessage(key, Locale.getDefault());
  }
  public static String getLocalizedMessage(String key, Locale locale) {
    Object message = getResourceBundleObject(key, locale);
    if (message == null) {
      return "Message with key:" + key + " and locale: " + locale
          + " not found.";
    }
    return message.toString();
  }
  public static String getLocalizedMessage(String key, Locale locale,
      Object... args) {
    String str = getLocalizedMessage(key, locale);
    if (args.length > 0) {
      str = MessageFormat.format(str, args);
    }
    return str;
  }
  public static String getLocalizedMessage(String key, Object... args) {
    return getLocalizedMessage(key, Locale.getDefault(), args);
  }
  @SuppressWarnings("unchecked")
  protected static void initializeMessages(String bundleName, Class clazz) {
    try {
      load(clazz);
      if (!bundles.containsKey(bundleName))
        bundles.put(bundleName, clazz);
    } catch (Throwable e) {
    }
  }
  private static Object getResourceBundleObject(String messageKey, Locale locale) {
    for (Iterator<String> it = bundles.keySet().iterator(); it.hasNext();) {
      Class<Object> clazz = bundles.get(it.next());
      ResourceBundle resourceBundle = ResourceBundle.getBundle(clazz.getName(),
          locale);
      if (resourceBundle != null) {
        try {
          Object obj = resourceBundle.getObject(messageKey);
          if (obj != null)
            return obj;
        } catch (MissingResourceException e) {
        }
      }
    }
    return null;
  }
  private static void load(Class<Object> clazz) {
    final Field[] fieldArray = clazz.getDeclaredFields();
    boolean isFieldAccessible = (clazz.getModifiers() & Modifier.PUBLIC) != 0;
    final int len = fieldArray.length;
    Map<String, Field> fields = new HashMap<String, Field>(len * 2);
    for (int i = 0; i < len; i++) {
      fields.put(fieldArray[i].getName(), fieldArray[i]);
      loadfieldValue(fieldArray[i], isFieldAccessible, clazz);
    }
  }
  private static void loadfieldValue(Field field, boolean isFieldAccessible,
      Class<Object> clazz) {
    int MOD_EXPECTED = Modifier.PUBLIC | Modifier.STATIC;
    int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;
    if ((field.getModifiers() & MOD_MASK) != MOD_EXPECTED)
      return;
    if (!isFieldAccessible)
      makeAccessible(field);
    try {
      field.set(null, field.getName());
      validateMessage(field.getName(), clazz);
    } catch (IllegalArgumentException e) {
    } catch (IllegalAccessException e) {
    }
  }
  private static void validateMessage(String key, Class<Object> clazz) {
    try {
      ResourceBundle resourceBundle = ResourceBundle.getBundle(clazz.getName(),
          Locale.getDefault());
      if (resourceBundle != null) {
        Object obj = resourceBundle.getObject(key);
        if (obj == null)
          System.err.println("WARN: Message with key:" + key + " and locale: "
              + Locale.getDefault() + " not found.");
      }
    } catch (MissingResourceException e) {
      System.err.println("WARN: Message with key:" + key + " and locale: "
          + Locale.getDefault() + " not found.");
    } catch (Throwable e) {
    }
  }
  @SuppressWarnings("unchecked")
  private static void makeAccessible(final Field field) {
    if (System.getSecurityManager() == null) {
      field.setAccessible(true);
    } else {
      AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
          field.setAccessible(true);
          return null;
        }
      });
    }
  }
}
