package org.apache.log4j.xml;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.or.RendererMap;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RendererSupport;
import org.apache.log4j.spi.ThrowableRenderer;
import org.apache.log4j.spi.ThrowableRendererSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Properties;
public class DOMConfigurator implements Configurator {
  static final String CONFIGURATION_TAG = "log4j:configuration";
  static final String OLD_CONFIGURATION_TAG = "configuration";
  static final String RENDERER_TAG      = "renderer";
  private static final String THROWABLE_RENDERER_TAG = "throwableRenderer";
  static final String APPENDER_TAG 	= "appender";
  static final String APPENDER_REF_TAG 	= "appender-ref";  
  static final String PARAM_TAG    	= "param";
  static final String LAYOUT_TAG	= "layout";
  static final String CATEGORY		= "category";
  static final String LOGGER		= "logger";
  static final String LOGGER_REF	= "logger-ref";
  static final String CATEGORY_FACTORY_TAG  = "categoryFactory";
  static final String LOGGER_FACTORY_TAG  = "loggerFactory";
  static final String NAME_ATTR		= "name";
  static final String CLASS_ATTR        = "class";
  static final String VALUE_ATTR	= "value";
  static final String ROOT_TAG		= "root";
  static final String ROOT_REF		= "root-ref";
  static final String LEVEL_TAG	        = "level";
  static final String PRIORITY_TAG      = "priority";
  static final String FILTER_TAG	= "filter";
  static final String ERROR_HANDLER_TAG	= "errorHandler";
  static final String REF_ATTR		= "ref";
  static final String ADDITIVITY_ATTR    = "additivity";  
  static final String THRESHOLD_ATTR       = "threshold";
  static final String CONFIG_DEBUG_ATTR  = "configDebug";
  static final String INTERNAL_DEBUG_ATTR  = "debug";
  private static final String RESET_ATTR  = "reset";
  static final String RENDERING_CLASS_ATTR = "renderingClass";
  static final String RENDERED_CLASS_ATTR = "renderedClass";
  static final String EMPTY_STR = "";
  static final Class[] ONE_STRING_PARAM = new Class[] {String.class};
  final static String dbfKey = "javax.xml.parsers.DocumentBuilderFactory";
  Hashtable appenderBag;
  Properties props;
  LoggerRepository repository;
  protected LoggerFactory catFactory = null;
  public
  DOMConfigurator () { 
    appenderBag = new Hashtable();
  }
  protected
  Appender findAppenderByName(Document doc, String appenderName)  {      
    Appender appender = (Appender) appenderBag.get(appenderName);
    if(appender != null) {
      return appender;
    } else {
      Element element = null;
      NodeList list = doc.getElementsByTagName("appender");
      for (int t=0; t < list.getLength(); t++) {
	Node node = list.item(t);
	NamedNodeMap map= node.getAttributes();
	Node attrNode = map.getNamedItem("name");
	if (appenderName.equals(attrNode.getNodeValue())) {
	  element = (Element) node;
	  break;
	}
      }
      if(element == null) {
	LogLog.error("No appender named ["+appenderName+"] could be found."); 
	return null;
      } else {
	      appender = parseAppender(element);
          if (appender != null) {
            appenderBag.put(appenderName, appender);
          }
    return appender;
      }
    } 
  }
  protected
  Appender findAppenderByReference(Element appenderRef) {    
    String appenderName = subst(appenderRef.getAttribute(REF_ATTR));    
    Document doc = appenderRef.getOwnerDocument();
    return findAppenderByName(doc, appenderName);
  }
  private static void parseUnrecognizedElement(final Object instance,
                                        final Element element,
                                        final Properties props) throws Exception {
      boolean recognized = false;
      if (instance instanceof UnrecognizedElementHandler) {
          recognized = ((UnrecognizedElementHandler) instance).parseUnrecognizedElement(
                  element, props);
      }
      if (!recognized) {
          LogLog.warn("Unrecognized element " + element.getNodeName());
      }
  }
   private static void quietParseUnrecognizedElement(final Object instance,
                                          final Element element,
                                          final Properties props) {
      try {
          parseUnrecognizedElement(instance, element, props);
      } catch (Exception ex) {
          if (ex instanceof InterruptedException || ex instanceof InterruptedIOException) {
              Thread.currentThread().interrupt();
          }
          LogLog.error("Error in extension content: ", ex);
      }
  }
  protected
  Appender parseAppender (Element appenderElement) {
    String className = subst(appenderElement.getAttribute(CLASS_ATTR));
    LogLog.debug("Class name: [" + className+']');    
    try {
      Object instance 	= Loader.loadClass(className).newInstance();
      Appender appender	= (Appender)instance;
      PropertySetter propSetter = new PropertySetter(appender);
      appender.setName(subst(appenderElement.getAttribute(NAME_ATTR)));
      NodeList children	= appenderElement.getChildNodes();
      final int length 	= children.getLength();
      for (int loop = 0; loop < length; loop++) {
	Node currentNode = children.item(loop);
	if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	  Element currentElement = (Element)currentNode;
	  if (currentElement.getTagName().equals(PARAM_TAG)) {
            setParameter(currentElement, propSetter);
	  }
	  else if (currentElement.getTagName().equals(LAYOUT_TAG)) {
	    appender.setLayout(parseLayout(currentElement));
	  }
	  else if (currentElement.getTagName().equals(FILTER_TAG)) {
	    parseFilters(currentElement, appender);
	  }
	  else if (currentElement.getTagName().equals(ERROR_HANDLER_TAG)) {
	    parseErrorHandler(currentElement, appender);
	  }
	  else if (currentElement.getTagName().equals(APPENDER_REF_TAG)) {
	    String refName = subst(currentElement.getAttribute(REF_ATTR));
	    if(appender instanceof AppenderAttachable) {
	      AppenderAttachable aa = (AppenderAttachable) appender;
	      LogLog.debug("Attaching appender named ["+ refName+
			   "] to appender named ["+ appender.getName()+"].");
	      aa.addAppender(findAppenderByReference(currentElement));
	    } else {
	      LogLog.error("Requesting attachment of appender named ["+
			   refName+ "] to appender named ["+ appender.getName()+
                "] which does not implement org.apache.log4j.spi.AppenderAttachable.");
	    }
	  } else {
          parseUnrecognizedElement(instance, currentElement, props);
      }
	}
      }
      propSetter.activate();
      return appender;
    }
    catch (Exception oops) {
        if (oops instanceof InterruptedException || oops instanceof InterruptedIOException) {
            Thread.currentThread().interrupt();
        }
      LogLog.error("Could not create an Appender. Reported error follows.",
		   oops);
      return null;
    }
  }
  protected
  void parseErrorHandler(Element element, Appender appender) {
    ErrorHandler eh = (ErrorHandler) OptionConverter.instantiateByClassName(
                                       subst(element.getAttribute(CLASS_ATTR)),
                                       org.apache.log4j.spi.ErrorHandler.class, 
 				       null);
    if(eh != null) {
      eh.setAppender(appender);
      PropertySetter propSetter = new PropertySetter(eh);
      NodeList children = element.getChildNodes();
      final int length 	= children.getLength();
      for (int loop = 0; loop < length; loop++) {
	Node currentNode = children.item(loop);
	if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	  Element currentElement = (Element) currentNode;
	  String tagName = currentElement.getTagName();
	  if(tagName.equals(PARAM_TAG)) {
            setParameter(currentElement, propSetter);
	  } else if(tagName.equals(APPENDER_REF_TAG)) {
	    eh.setBackupAppender(findAppenderByReference(currentElement));
	  } else if(tagName.equals(LOGGER_REF)) {
	    String loggerName = currentElement.getAttribute(REF_ATTR);	    
	    Logger logger = (catFactory == null) ? repository.getLogger(loggerName)
                : repository.getLogger(loggerName, catFactory);
	    eh.setLogger(logger);
	  } else if(tagName.equals(ROOT_REF)) {
	    Logger root = repository.getRootLogger();
	    eh.setLogger(root);
	  } else {
          quietParseUnrecognizedElement(eh, currentElement, props);
      }
	}
      }
      propSetter.activate();
      appender.setErrorHandler(eh);
    }
  }
  protected
  void parseFilters(Element element, Appender appender) {
    String clazz = subst(element.getAttribute(CLASS_ATTR));
    Filter filter = (Filter) OptionConverter.instantiateByClassName(clazz,
                                                Filter.class, null);
    if(filter != null) {
      PropertySetter propSetter = new PropertySetter(filter);
      NodeList children = element.getChildNodes();
      final int length 	= children.getLength();
      for (int loop = 0; loop < length; loop++) {
	Node currentNode = children.item(loop);
	if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	  Element currentElement = (Element) currentNode;
	  String tagName = currentElement.getTagName();
	  if(tagName.equals(PARAM_TAG)) {
            setParameter(currentElement, propSetter);
	  } else {
            quietParseUnrecognizedElement(filter, currentElement, props);
      }
	}
      }
      propSetter.activate();
      LogLog.debug("Adding filter of type ["+filter.getClass()
		   +"] to appender named ["+appender.getName()+"].");
      appender.addFilter(filter);
    }    
  }
  protected
  void parseCategory (Element loggerElement) {
    String catName = subst(loggerElement.getAttribute(NAME_ATTR));
    Logger cat;    
    String className = subst(loggerElement.getAttribute(CLASS_ATTR));
    if(EMPTY_STR.equals(className)) {
      LogLog.debug("Retreiving an instance of org.apache.log4j.Logger.");
      cat = (catFactory == null) ? repository.getLogger(catName) : repository.getLogger(catName, catFactory);
    }
    else {
      LogLog.debug("Desired logger sub-class: ["+className+']');
       try {	 
	 Class clazz = Loader.loadClass(className);
	 Method getInstanceMethod = clazz.getMethod("getLogger", 
						    ONE_STRING_PARAM);
	 cat = (Logger) getInstanceMethod.invoke(null, new Object[] {catName});
       } catch (InvocationTargetException oops) {
          if (oops.getTargetException() instanceof InterruptedException
                  || oops.getTargetException() instanceof InterruptedIOException) {
              Thread.currentThread().interrupt();
          }
          LogLog.error("Could not retrieve category ["+catName+
		      "]. Reported error follows.", oops);
	      return;
       } catch (Exception oops) {
	      LogLog.error("Could not retrieve category ["+catName+
		      "]. Reported error follows.", oops);
	      return;
       }
    }
    synchronized(cat) {
      boolean additivity = OptionConverter.toBoolean(
                           subst(loggerElement.getAttribute(ADDITIVITY_ATTR)),
			   true);
      LogLog.debug("Setting ["+cat.getName()+"] additivity to ["+additivity+"].");
      cat.setAdditivity(additivity);
      parseChildrenOfLoggerElement(loggerElement, cat, false);
    }
  }
  protected
  void parseCategoryFactory(Element factoryElement) {
    String className = subst(factoryElement.getAttribute(CLASS_ATTR));
    if(EMPTY_STR.equals(className)) {
      LogLog.error("Category Factory tag " + CLASS_ATTR + " attribute not found.");
      LogLog.debug("No Category Factory configured.");
    }
    else {
      LogLog.debug("Desired category factory: ["+className+']');
      Object factory = OptionConverter.instantiateByClassName(className,
                                                                 LoggerFactory.class, 
                                                                 null);
      if (factory instanceof LoggerFactory) {
          catFactory = (LoggerFactory) factory;
      } else {
          LogLog.error("Category Factory class " + className + " does not implement org.apache.log4j.LoggerFactory");
      }
      PropertySetter propSetter = new PropertySetter(factory);
      Element  currentElement = null;
      Node     currentNode    = null;
      NodeList children       = factoryElement.getChildNodes();
      final int length        = children.getLength();
      for (int loop=0; loop < length; loop++) {
        currentNode = children.item(loop);
	if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	  currentElement = (Element)currentNode;
	  if (currentElement.getTagName().equals(PARAM_TAG)) {
	    setParameter(currentElement, propSetter);
	  } else {
           quietParseUnrecognizedElement(factory, currentElement, props);
      }
	}
      }
    }
  }
  protected
  void parseRoot (Element rootElement) {
    Logger root = repository.getRootLogger();
    synchronized(root) {    
      parseChildrenOfLoggerElement(rootElement, root, true);
    }
  }
  protected
  void parseChildrenOfLoggerElement(Element catElement,
				      Logger cat, boolean isRoot) {
    PropertySetter propSetter = new PropertySetter(cat);
    cat.removeAllAppenders();
    NodeList children 	= catElement.getChildNodes();
    final int length 	= children.getLength();
    for (int loop = 0; loop < length; loop++) {
      Node currentNode = children.item(loop);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	Element currentElement = (Element) currentNode;
	String tagName = currentElement.getTagName();
	if (tagName.equals(APPENDER_REF_TAG)) {
	  Element appenderRef = (Element) currentNode;
	  Appender appender = findAppenderByReference(appenderRef);
	  String refName =  subst(appenderRef.getAttribute(REF_ATTR));
	  if(appender != null)
	    LogLog.debug("Adding appender named ["+ refName+ 
			 "] to category ["+cat.getName()+"].");
	  else 
	    LogLog.debug("Appender named ["+ refName + "] not found.");
	  cat.addAppender(appender);
	} else if(tagName.equals(LEVEL_TAG)) {
	  parseLevel(currentElement, cat, isRoot);	
	} else if(tagName.equals(PRIORITY_TAG)) {
	  parseLevel(currentElement, cat, isRoot);
	} else if(tagName.equals(PARAM_TAG)) {
          setParameter(currentElement, propSetter);
	} else {
        quietParseUnrecognizedElement(cat, currentElement, props);
    }
      }
    }
    propSetter.activate();
  }
  protected
  Layout parseLayout (Element layout_element) {
    String className = subst(layout_element.getAttribute(CLASS_ATTR));
    LogLog.debug("Parsing layout of class: \""+className+"\"");		 
    try {
      Object instance 	= Loader.loadClass(className).newInstance();
      Layout layout   	= (Layout)instance;
      PropertySetter propSetter = new PropertySetter(layout);
      NodeList params 	= layout_element.getChildNodes();
      final int length 	= params.getLength();
      for (int loop = 0; loop < length; loop++) {
	Node currentNode = (Node)params.item(loop);
	if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	  Element currentElement = (Element) currentNode;
	  String tagName = currentElement.getTagName();
	  if(tagName.equals(PARAM_TAG)) {
            setParameter(currentElement, propSetter);
	  } else {
          parseUnrecognizedElement(instance, currentElement, props);
      }
	}
      }
      propSetter.activate();
      return layout;
    }
    catch (Exception oops) {
        if (oops instanceof InterruptedException || oops instanceof InterruptedIOException) {
            Thread.currentThread().interrupt();
        }
      LogLog.error("Could not create the Layout. Reported error follows.",
		   oops);
      return null;
    }
  }
  protected 
  void parseRenderer(Element element) {
    String renderingClass = subst(element.getAttribute(RENDERING_CLASS_ATTR));
    String renderedClass = subst(element.getAttribute(RENDERED_CLASS_ATTR));
    if(repository instanceof RendererSupport) {
      RendererMap.addRenderer((RendererSupport) repository, renderedClass, 
			      renderingClass);
    }
  }
    protected ThrowableRenderer parseThrowableRenderer(final Element element) {
        String className = subst(element.getAttribute(CLASS_ATTR));
        LogLog.debug("Parsing throwableRenderer of class: \""+className+"\"");
        try {
          Object instance 	= Loader.loadClass(className).newInstance();
          ThrowableRenderer tr   	= (ThrowableRenderer)instance;
          PropertySetter propSetter = new PropertySetter(tr);
          NodeList params 	= element.getChildNodes();
          final int length 	= params.getLength();
          for (int loop = 0; loop < length; loop++) {
                Node currentNode = (Node)params.item(loop);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element currentElement = (Element) currentNode;
                    String tagName = currentElement.getTagName();
                    if(tagName.equals(PARAM_TAG)) {
                        setParameter(currentElement, propSetter);
                    } else {
                        parseUnrecognizedElement(instance, currentElement, props);
                    }
                }
          }
          propSetter.activate();
          return tr;
        }
        catch (Exception oops) {
            if (oops instanceof InterruptedException || oops instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LogLog.error("Could not create the ThrowableRenderer. Reported error follows.",
               oops);
          return null;
        }
    }
  protected
  void parseLevel(Element element, Logger logger, boolean isRoot) {
    String catName = logger.getName();
    if(isRoot) {
      catName = "root";
    }
    String priStr = subst(element.getAttribute(VALUE_ATTR));
    LogLog.debug("Level value for "+catName+" is  ["+priStr+"].");
    if(INHERITED.equalsIgnoreCase(priStr) || NULL.equalsIgnoreCase(priStr)) {
      if(isRoot) {
	LogLog.error("Root level cannot be inherited. Ignoring directive.");
      } else {
	logger.setLevel(null);
      }
    } else {
      String className = subst(element.getAttribute(CLASS_ATTR));      
      if(EMPTY_STR.equals(className)) {	
	logger.setLevel(OptionConverter.toLevel(priStr, Level.DEBUG));
      } else {
	LogLog.debug("Desired Level sub-class: ["+className+']');
	try {	 
	  Class clazz = Loader.loadClass(className);
	  Method toLevelMethod = clazz.getMethod("toLevel", 
						    ONE_STRING_PARAM);
	  Level pri = (Level) toLevelMethod.invoke(null, 
						    new Object[] {priStr});
	  logger.setLevel(pri);
	} catch (Exception oops) {
        if (oops instanceof InterruptedException || oops instanceof InterruptedIOException) {
            Thread.currentThread().interrupt();
        }
	  LogLog.error("Could not create level ["+priStr+
		       "]. Reported error follows.", oops);
	  return;
	}
      }
    }
    LogLog.debug(catName + " level set to " + logger.getLevel());    
  }
  protected
  void setParameter(Element elem, PropertySetter propSetter) {
      String name = subst(elem.getAttribute(NAME_ATTR));
      String value = (elem.getAttribute(VALUE_ATTR));
      value = subst(OptionConverter.convertSpecialChars(value));
      propSetter.setProperty(name, value);
  }
  static
  public
  void configure (Element element) {
    DOMConfigurator configurator = new DOMConfigurator();
    configurator.doConfigure(element,  LogManager.getLoggerRepository());
  }
  static
  public
  void configureAndWatch(String configFilename) {
    configureAndWatch(configFilename, FileWatchdog.DEFAULT_DELAY);
  }
  static
  public
  void configureAndWatch(String configFilename, long delay) {
    XMLWatchdog xdog = new XMLWatchdog(configFilename);
    xdog.setDelay(delay);
    xdog.start();
  }
  private interface ParseAction {
      Document parse(final DocumentBuilder parser) throws SAXException, IOException;
  }
  public
  void doConfigure(final String filename, LoggerRepository repository) {
    ParseAction action = new ParseAction() {
          public Document parse(final DocumentBuilder parser) throws SAXException, IOException {
              return parser.parse(new File(filename));
          }
          public String toString() { 
              return "file [" + filename + "]"; 
          }
    };
    doConfigure(action, repository);
  }
  public
  void doConfigure(final URL url, LoggerRepository repository) {
      ParseAction action = new ParseAction() {
          public Document parse(final DocumentBuilder parser) throws SAXException, IOException {
              URLConnection uConn = url.openConnection();
              uConn.setUseCaches(false);
              InputStream stream = uConn.getInputStream();
              try {
                InputSource src = new InputSource(stream);
                src.setSystemId(url.toString());
                return parser.parse(src);
              } finally {
                stream.close();
              }
          }
          public String toString() { 
              return "url [" + url.toString() + "]"; 
          }
      };
      doConfigure(action, repository);
  }
  public
  void doConfigure(final InputStream inputStream, LoggerRepository repository) 
                                          throws FactoryConfigurationError {
      ParseAction action = new ParseAction() {
          public Document parse(final DocumentBuilder parser) throws SAXException, IOException {
              InputSource inputSource = new InputSource(inputStream);
              inputSource.setSystemId("dummy://log4j.dtd");
              return parser.parse(inputSource);
          }
          public String toString() { 
              return "input stream [" + inputStream.toString() + "]"; 
          }
      };
      doConfigure(action, repository);
  }
  public
  void doConfigure(final Reader reader, LoggerRepository repository) 
                                          throws FactoryConfigurationError {
      ParseAction action = new ParseAction() {
          public Document parse(final DocumentBuilder parser) throws SAXException, IOException {
              InputSource inputSource = new InputSource(reader);
              inputSource.setSystemId("dummy://log4j.dtd");
              return parser.parse(inputSource);
          }
          public String toString() { 
              return "reader [" + reader.toString() + "]"; 
          }
      };
    doConfigure(action, repository);
  }
  protected
  void doConfigure(final InputSource inputSource, LoggerRepository repository) 
                                          throws FactoryConfigurationError {
      if (inputSource.getSystemId() == null) {
          inputSource.setSystemId("dummy://log4j.dtd");
      }
      ParseAction action = new ParseAction() {
          public Document parse(final DocumentBuilder parser) throws SAXException, IOException {
              return parser.parse(inputSource);
          }
          public String toString() { 
              return "input source [" + inputSource.toString() + "]"; 
          }
      };
      doConfigure(action, repository);
    }
  private final void doConfigure(final ParseAction action, final LoggerRepository repository)
         throws FactoryConfigurationError {
    DocumentBuilderFactory dbf = null;
    this.repository = repository;
    try { 
      LogLog.debug("System property is :"+
  	                        OptionConverter.getSystemProperty(dbfKey, 
								  null)); 
      dbf = DocumentBuilderFactory.newInstance();
      LogLog.debug("Standard DocumentBuilderFactory search succeded.");
      LogLog.debug("DocumentBuilderFactory is: "+dbf.getClass().getName());
    } catch(FactoryConfigurationError fce) {
      Exception e = fce.getException();
      LogLog.debug("Could not instantiate a DocumentBuilderFactory.", e);
      throw fce;
    }
    try {
      dbf.setValidating(true);
      DocumentBuilder docBuilder = dbf.newDocumentBuilder();
      docBuilder.setErrorHandler(new SAXErrorHandler());      
      docBuilder.setEntityResolver(new Log4jEntityResolver());
      Document doc = action.parse(docBuilder);     
      parse(doc.getDocumentElement());
    } catch (Exception e) {
        if (e instanceof InterruptedException || e instanceof InterruptedIOException) {
            Thread.currentThread().interrupt();
        }
      LogLog.error("Could not parse "+ action.toString() + ".", e);
    }
  }
  public void doConfigure(Element element, LoggerRepository repository) {
    this.repository = repository;
    parse(element);
  }
  static
  public
  void configure(String filename) throws FactoryConfigurationError {
    new DOMConfigurator().doConfigure(filename, 
				      LogManager.getLoggerRepository());
  }
  static
  public
  void configure(URL url) throws FactoryConfigurationError {
    new DOMConfigurator().doConfigure(url, LogManager.getLoggerRepository());
  }
  protected
  void parse(Element element) {
    String rootElementName = element.getTagName();
    if (!rootElementName.equals(CONFIGURATION_TAG)) {
      if(rootElementName.equals(OLD_CONFIGURATION_TAG)) {
	LogLog.warn("The <"+OLD_CONFIGURATION_TAG+
		     "> element has been deprecated.");
	LogLog.warn("Use the <"+CONFIGURATION_TAG+"> element instead.");
      } else {
	LogLog.error("DOM element is - not a <"+CONFIGURATION_TAG+"> element.");
	return;
      }
    }
    String debugAttrib = subst(element.getAttribute(INTERNAL_DEBUG_ATTR));
    LogLog.debug("debug attribute= \"" + debugAttrib +"\".");
    if(!debugAttrib.equals("") && !debugAttrib.equals("null")) {      
      LogLog.setInternalDebugging(OptionConverter.toBoolean(debugAttrib, true));
    } else {
      LogLog.debug("Ignoring " + INTERNAL_DEBUG_ATTR + " attribute.");
    }
    String resetAttrib = subst(element.getAttribute(RESET_ATTR));
    LogLog.debug("reset attribute= \"" + resetAttrib +"\".");
    if(!("".equals(resetAttrib))) {
         if (OptionConverter.toBoolean(resetAttrib, false)) {
             repository.resetConfiguration();
         }
    }
    String confDebug = subst(element.getAttribute(CONFIG_DEBUG_ATTR));
    if(!confDebug.equals("") && !confDebug.equals("null")) {      
      LogLog.warn("The \""+CONFIG_DEBUG_ATTR+"\" attribute is deprecated.");
      LogLog.warn("Use the \""+INTERNAL_DEBUG_ATTR+"\" attribute instead.");
      LogLog.setInternalDebugging(OptionConverter.toBoolean(confDebug, true));
    }
    String thresholdStr = subst(element.getAttribute(THRESHOLD_ATTR));
    LogLog.debug("Threshold =\"" + thresholdStr +"\".");
    if(!"".equals(thresholdStr) && !"null".equals(thresholdStr)) {
      repository.setThreshold(thresholdStr);
    }
    String   tagName = null;
    Element  currentElement = null;
    Node     currentNode = null;
    NodeList children = element.getChildNodes();
    final int length = children.getLength();
    for (int loop = 0; loop < length; loop++) {
      currentNode = children.item(loop);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	currentElement = (Element) currentNode;
	tagName = currentElement.getTagName();
	if (tagName.equals(CATEGORY_FACTORY_TAG) || tagName.equals(LOGGER_FACTORY_TAG)) {
	  parseCategoryFactory(currentElement);
	}
      }
    }
    for (int loop = 0; loop < length; loop++) {
      currentNode = children.item(loop);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	currentElement = (Element) currentNode;
	tagName = currentElement.getTagName();
	if (tagName.equals(CATEGORY) || tagName.equals(LOGGER)) {
	  parseCategory(currentElement);
	} else if (tagName.equals(ROOT_TAG)) {
	  parseRoot(currentElement);
	} else if(tagName.equals(RENDERER_TAG)) {
	  parseRenderer(currentElement);
    } else if(tagName.equals(THROWABLE_RENDERER_TAG)) {
        if (repository instanceof ThrowableRendererSupport) {
            ThrowableRenderer tr = parseThrowableRenderer(currentElement);
            if (tr != null) {
                ((ThrowableRendererSupport) repository).setThrowableRenderer(tr);
            }
        }
    } else if (!(tagName.equals(APPENDER_TAG)
            || tagName.equals(CATEGORY_FACTORY_TAG)
            || tagName.equals(LOGGER_FACTORY_TAG))) {
        quietParseUnrecognizedElement(repository, currentElement, props);
    }
      }
    }
  }
  protected
  String subst(final String value) {
      return subst(value, props);
  }
    public static String subst(final String value, final Properties props) {
        try {
            return OptionConverter.substVars(value, props);
        } catch (IllegalArgumentException e) {
            LogLog.warn("Could not perform variable substitution.", e);
            return value;
        }
    }
    public static void setParameter(final Element elem,
                                    final PropertySetter propSetter,
                                    final Properties props) {
        String name = subst(elem.getAttribute("name"), props);
        String value = (elem.getAttribute("value"));
        value = subst(OptionConverter.convertSpecialChars(value), props);
        propSetter.setProperty(name, value);
    }
    public static Object parseElement(final Element element,
                                             final Properties props,
                                             final Class expectedClass) throws Exception {
        String clazz = subst(element.getAttribute("class"), props);
        Object instance = OptionConverter.instantiateByClassName(clazz,
                expectedClass, null);
        if (instance != null) {
            PropertySetter propSetter = new PropertySetter(instance);
            NodeList children = element.getChildNodes();
            final int length = children.getLength();
            for (int loop = 0; loop < length; loop++) {
                Node currentNode = children.item(loop);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element currentElement = (Element) currentNode;
                    String tagName = currentElement.getTagName();
                    if (tagName.equals("param")) {
                        setParameter(currentElement, propSetter, props);
                    } else {
                         parseUnrecognizedElement(instance, currentElement, props);
                    }
                }
            }
            return instance;
        }
        return null;
    }
}
class XMLWatchdog extends FileWatchdog {
    XMLWatchdog(String filename) {
    super(filename);
  }
  public
  void doOnChange() {
    new DOMConfigurator().doConfigure(filename, 
				      LogManager.getLoggerRepository());
  }
}
