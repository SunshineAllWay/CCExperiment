import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.helpers.LogLog;
import java.util.Hashtable;
import java.util.Properties;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
public class JMSQueueAppender extends AppenderSkeleton {
    protected QueueConnection queueConnection;
    protected QueueSession queueSession;
    protected QueueSender queueSender;
    protected Queue queue;
    String initialContextFactory;
    String providerUrl;
    String queueBindingName;
    String queueConnectionFactoryBindingName;
    public 
	JMSQueueAppender() {
    }
    public void setInitialContextFactory(String initialContextFactory) {
	this.initialContextFactory = initialContextFactory;
    }
    public String getInitialContextFactory() {
	return initialContextFactory;
    }
    public void setProviderUrl(String providerUrl) {
	this.providerUrl = providerUrl;
    }
    public String getProviderUrl() {
	return providerUrl;
    }
    public void setQueueConnectionFactoryBindingName(String queueConnectionFactoryBindingName) {
	this.queueConnectionFactoryBindingName = queueConnectionFactoryBindingName;
    }
    public String getQueueConnectionFactoryBindingName() {
	return queueConnectionFactoryBindingName;
    }
    public void setQueueBindingName(String queueBindingName) {
	this.queueBindingName = queueBindingName;
    }
    public String getQueueBindingName() {
	return queueBindingName;
    }
    public void activateOptions() {
	QueueConnectionFactory queueConnectionFactory;
	try {
	    Context ctx = getInitialContext();      
	    queueConnectionFactory = (QueueConnectionFactory) ctx.lookup(queueConnectionFactoryBindingName);
	    queueConnection = queueConnectionFactory.createQueueConnection();
	    queueSession = queueConnection.createQueueSession(false,
							      Session.AUTO_ACKNOWLEDGE);
	    Queue queue = (Queue) ctx.lookup(queueBindingName);
	    queueSender = queueSession.createSender(queue);
	    queueConnection.start();
	    ctx.close();      
	} catch(Exception e) {
	    errorHandler.error("Error while activating options for appender named ["+name+
			       "].", e, ErrorCode.GENERIC_FAILURE);
	}
    }
    protected InitialContext getInitialContext() throws NamingException {
	try {
	    Hashtable ht = new Hashtable();
	    ht.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
	    ht.put(Context.PROVIDER_URL, providerUrl);
	    return (new InitialContext(ht));
	} catch (NamingException ne) {
	    LogLog.error("Could not get initial context with ["+initialContextFactory + "] and [" + providerUrl + "]."); 
	    throw ne;
	}
    }
    protected boolean checkEntryConditions() {
	String fail = null;
	if(this.queueConnection == null) {
	    fail = "No QueueConnection";
	} else if(this.queueSession == null) {
	    fail = "No QueueSession";
	} else if(this.queueSender == null) {
	    fail = "No QueueSender";
	} 
	if(fail != null) {
	    errorHandler.error(fail +" for JMSQueueAppender named ["+name+"].");      
	    return false;
	} else {
	    return true;
	}
    }
    public synchronized 
	void close() {
	if(this.closed) 
	    return;
	LogLog.debug("Closing appender ["+name+"].");
	this.closed = true;    
	try {
	    if(queueSession != null) 
		queueSession.close();	
	    if(queueConnection != null) 
		queueConnection.close();
	} catch(Exception e) {
	    LogLog.error("Error while closing JMSQueueAppender ["+name+"].", e);	
	}   
	queueSender = null;
	queueSession = null;
	queueConnection = null;
    }
    public void append(LoggingEvent event) {
	if(!checkEntryConditions()) {
	    return;
	}
	try {
	    ObjectMessage msg = queueSession.createObjectMessage();
	    msg.setObject(event);
	    queueSender.send(msg);
	} catch(Exception e) {
	    errorHandler.error("Could not send message in JMSQueueAppender ["+name+"].", e, 
			       ErrorCode.GENERIC_FAILURE);
	}
    }
    public boolean requiresLayout() {
	return false;
    }  
}