package org.apache.log4j.net;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.Properties;
public class JMSAppender extends AppenderSkeleton {
  String securityPrincipalName;
  String securityCredentials;
  String initialContextFactoryName;
  String urlPkgPrefixes;
  String providerURL;
  String topicBindingName;
  String tcfBindingName;
  String userName;
  String password;
  boolean locationInfo;
  TopicConnection  topicConnection;
  TopicSession topicSession;
  TopicPublisher  topicPublisher;
  public
  JMSAppender() {
  }
  public
  void setTopicConnectionFactoryBindingName(String tcfBindingName) {
    this.tcfBindingName = tcfBindingName;
  }
  public
  String getTopicConnectionFactoryBindingName() {
    return tcfBindingName;
  }
  public
  void setTopicBindingName(String topicBindingName) {
    this.topicBindingName = topicBindingName;
  }
  public
  String getTopicBindingName() {
    return topicBindingName;
  }
  public
  boolean getLocationInfo() {
    return locationInfo;
  }
  public void activateOptions() {
    TopicConnectionFactory  topicConnectionFactory;
    try {
      Context jndi;
      LogLog.debug("Getting initial context.");
      if(initialContextFactoryName != null) {
	Properties env = new Properties( );
	env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryName);
	if(providerURL != null) {
	  env.put(Context.PROVIDER_URL, providerURL);
	} else {
	  LogLog.warn("You have set InitialContextFactoryName option but not the "
		     +"ProviderURL. This is likely to cause problems.");
	}
	if(urlPkgPrefixes != null) {
	  env.put(Context.URL_PKG_PREFIXES, urlPkgPrefixes);
	}
	if(securityPrincipalName != null) {
	  env.put(Context.SECURITY_PRINCIPAL, securityPrincipalName);
	  if(securityCredentials != null) {
	    env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
	  } else {
	    LogLog.warn("You have set SecurityPrincipalName option but not the "
			+"SecurityCredentials. This is likely to cause problems.");
	  }
	}	
	jndi = new InitialContext(env);
      } else {
	jndi = new InitialContext();
      }
      LogLog.debug("Looking up ["+tcfBindingName+"]");
      topicConnectionFactory = (TopicConnectionFactory) lookup(jndi, tcfBindingName);
      LogLog.debug("About to create TopicConnection.");
      if(userName != null) {
	topicConnection = topicConnectionFactory.createTopicConnection(userName, 
								       password); 
      } else {
	topicConnection = topicConnectionFactory.createTopicConnection();
      }
      LogLog.debug("Creating TopicSession, non-transactional, "
		   +"in AUTO_ACKNOWLEDGE mode.");
      topicSession = topicConnection.createTopicSession(false,
							Session.AUTO_ACKNOWLEDGE);
      LogLog.debug("Looking up topic name ["+topicBindingName+"].");
      Topic topic = (Topic) lookup(jndi, topicBindingName);
      LogLog.debug("Creating TopicPublisher.");
      topicPublisher = topicSession.createPublisher(topic);
      LogLog.debug("Starting TopicConnection.");
      topicConnection.start();
      jndi.close();
    } catch(JMSException e) {
      errorHandler.error("Error while activating options for appender named ["+name+
			 "].", e, ErrorCode.GENERIC_FAILURE);
    } catch(NamingException e) {
      errorHandler.error("Error while activating options for appender named ["+name+
			 "].", e, ErrorCode.GENERIC_FAILURE);
    } catch(RuntimeException e) {
      errorHandler.error("Error while activating options for appender named ["+name+
			 "].", e, ErrorCode.GENERIC_FAILURE);
    }
  }
  protected Object lookup(Context ctx, String name) throws NamingException {
    try {
      return ctx.lookup(name);
    } catch(NameNotFoundException e) {
      LogLog.error("Could not find name ["+name+"].");
      throw e;
    }
  }
  protected boolean checkEntryConditions() {
    String fail = null;
    if(this.topicConnection == null) {
      fail = "No TopicConnection";
    } else if(this.topicSession == null) {
      fail = "No TopicSession";
    } else if(this.topicPublisher == null) {
      fail = "No TopicPublisher";
    }
    if(fail != null) {
      errorHandler.error(fail +" for JMSAppender named ["+name+"].");
      return false;
    } else {
      return true;
    }
  }
  public synchronized void close() {
    if(this.closed)
      return;
    LogLog.debug("Closing appender ["+name+"].");
    this.closed = true;
    try {
      if(topicSession != null)
	topicSession.close();
      if(topicConnection != null)
	topicConnection.close();
    } catch(JMSException e) {
      LogLog.error("Error while closing JMSAppender ["+name+"].", e);
    } catch(RuntimeException e) {
      LogLog.error("Error while closing JMSAppender ["+name+"].", e);
    }
    topicPublisher = null;
    topicSession = null;
    topicConnection = null;
  }
  public void append(LoggingEvent event) {
    if(!checkEntryConditions()) {
      return;
    }
    try {
      ObjectMessage msg = topicSession.createObjectMessage();
      if(locationInfo) {
	event.getLocationInformation();
      }
      msg.setObject(event);
      topicPublisher.publish(msg);
    } catch(JMSException e) {
      errorHandler.error("Could not publish message in JMSAppender ["+name+"].", e,
			 ErrorCode.GENERIC_FAILURE);
    } catch(RuntimeException e) {
      errorHandler.error("Could not publish message in JMSAppender ["+name+"].", e,
			 ErrorCode.GENERIC_FAILURE);
    }
  }
  public String getInitialContextFactoryName() {
    return initialContextFactoryName;    
  }
  public void setInitialContextFactoryName(String initialContextFactoryName) {
    this.initialContextFactoryName = initialContextFactoryName;
  }
  public String getProviderURL() {
    return providerURL;    
  }
  public void setProviderURL(String providerURL) {
    this.providerURL = providerURL;
  }
  String getURLPkgPrefixes( ) {
    return urlPkgPrefixes;
  }
  public void setURLPkgPrefixes(String urlPkgPrefixes ) {
    this.urlPkgPrefixes = urlPkgPrefixes;
  }
  public String getSecurityCredentials() {
    return securityCredentials;    
  }
  public void setSecurityCredentials(String securityCredentials) {
    this.securityCredentials = securityCredentials;
  }
  public String getSecurityPrincipalName() {
    return securityPrincipalName;    
  }
  public void setSecurityPrincipalName(String securityPrincipalName) {
    this.securityPrincipalName = securityPrincipalName;
  }
  public String getUserName() {
    return userName;    
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }
  public String getPassword() {
    return password;    
  }
  public void setPassword(String password) {
    this.password = password;
  }
  public void setLocationInfo(boolean locationInfo) {
    this.locationInfo = locationInfo;
  }
  protected TopicConnection  getTopicConnection() {
    return topicConnection;
  }
  protected TopicSession  getTopicSession() {
    return topicSession;
  }
  protected TopicPublisher  getTopicPublisher() {
    return topicPublisher;
  }
  public boolean requiresLayout() {
    return false;
  }
}
