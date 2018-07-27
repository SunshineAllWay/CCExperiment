package org.apache.tools.ant.taskdefs.email;
import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.ClasspathUtils;
public class EmailTask extends Task {
    private static final int SMTP_PORT = 25;
    public static final String AUTO = "auto";
    public static final String MIME = "mime";
    public static final String UU = "uu";
    public static final String PLAIN = "plain";
    public static class Encoding extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {AUTO, MIME, UU, PLAIN};
        }
    }
    private String encoding = AUTO;
    private String host = "localhost";
    private Integer port = null;
    private String subject = null;
    private Message message = null;
    private boolean failOnError = true;
    private boolean includeFileNames = false;
    private String messageMimeType = null;
    private EmailAddress from = null;
    private Vector replyToList = new Vector();
    private Vector toList = new Vector();
    private Vector ccList = new Vector();
    private Vector bccList = new Vector();
    private Vector headers = new Vector();
    private Path attachments = null;
    private String charset = null;
    private String user = null;
    private String password = null;
    private boolean ssl = false;
    private boolean starttls = false;
    private boolean ignoreInvalidRecipients = false;
    public void setUser(String user) {
        this.user = user;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setSSL(boolean ssl) {
        this.ssl = ssl;
    }
    public void setEnableStartTLS(boolean b) {
        this.starttls = b;
    }
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding.getValue();
    }
    public void setMailport(int port) {
        this.port = new Integer(port);
    }
    public void setMailhost(String host) {
        this.host = host;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public void setMessage(String message) {
        if (this.message != null) {
            throw new BuildException("Only one message can be sent in an "
                 + "email");
        }
        this.message = new Message(message);
        this.message.setProject(getProject());
    }
    public void setMessageFile(File file) {
        if (this.message != null) {
            throw new BuildException("Only one message can be sent in an "
                 + "email");
        }
        this.message = new Message(file);
        this.message.setProject(getProject());
    }
    public void setMessageMimeType(String type) {
        this.messageMimeType = type;
    }
    public void addMessage(Message message) throws BuildException {
        if (this.message != null) {
            throw new BuildException(
                "Only one message can be sent in an email");
        }
        this.message = message;
    }
    public void addFrom(EmailAddress address) {
        if (this.from != null) {
            throw new BuildException("Emails can only be from one address");
        }
        this.from = address;
    }
    public void setFrom(String address) {
        if (this.from != null) {
            throw new BuildException("Emails can only be from one address");
        }
        this.from = new EmailAddress(address);
    }
    public void addReplyTo(EmailAddress address) {
        this.replyToList.add(address);
    }
    public void setReplyTo(String address) {
        this.replyToList.add(new EmailAddress(address));
    }
    public void addTo(EmailAddress address) {
        toList.addElement(address);
    }
    public void setToList(String list) {
        StringTokenizer tokens = new StringTokenizer(list, ",");
        while (tokens.hasMoreTokens()) {
            toList.addElement(new EmailAddress(tokens.nextToken()));
        }
    }
    public void addCc(EmailAddress address) {
        ccList.addElement(address);
    }
    public void setCcList(String list) {
        StringTokenizer tokens = new StringTokenizer(list, ",");
        while (tokens.hasMoreTokens()) {
            ccList.addElement(new EmailAddress(tokens.nextToken()));
        }
    }
    public void addBcc(EmailAddress address) {
        bccList.addElement(address);
    }
    public void setBccList(String list) {
        StringTokenizer tokens = new StringTokenizer(list, ",");
        while (tokens.hasMoreTokens()) {
            bccList.addElement(new EmailAddress(tokens.nextToken()));
        }
    }
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
    public void setFiles(String filenames) {
        StringTokenizer t = new StringTokenizer(filenames, ", ");
        while (t.hasMoreTokens()) {
            createAttachments()
                .add(new FileResource(getProject().resolveFile(t.nextToken())));
        }
    }
    public void addFileset(FileSet fs) {
        createAttachments().add(fs);
    }
    public Path createAttachments() {
        if (attachments == null) {
            attachments = new Path(getProject());
        }
        return attachments.createPath();
    }
    public Header createHeader() {
        Header h = new Header();
        headers.add(h);
        return h;
    }
    public void setIncludefilenames(boolean includeFileNames) {
        this.includeFileNames = includeFileNames;
    }
    public boolean getIncludeFileNames() {
        return includeFileNames;
    }
    public void setIgnoreInvalidRecipients(boolean b) {
        ignoreInvalidRecipients = b;
    }
    public void execute() {
        Message savedMessage = message;
        try {
            Mailer mailer = null;
            boolean autoFound = false;
            if (encoding.equals(MIME)
                 || (encoding.equals(AUTO) && !autoFound)) {
                try {
                    Class.forName("javax.activation.DataHandler");
                    Class.forName("javax.mail.internet.MimeMessage");
                    mailer = (Mailer) ClasspathUtils.newInstance(
                            "org.apache.tools.ant.taskdefs.email.MimeMailer",
                            EmailTask.class.getClassLoader(), Mailer.class);
                    autoFound = true;
                    log("Using MIME mail", Project.MSG_VERBOSE);
                } catch (BuildException e) {
                    logBuildException("Failed to initialise MIME mail: ", e);
                }
            }
            if (!autoFound && ((user != null) || (password != null))
                && (encoding.equals(UU) || encoding.equals(PLAIN))) {
                throw new BuildException("SMTP auth only possible with MIME mail");
            }
            if (!autoFound  && (ssl || starttls)
                && (encoding.equals(UU) || encoding.equals(PLAIN))) {
                throw new BuildException("SSL and STARTTLS only possible with"
                                         + " MIME mail");
            }
            if (encoding.equals(UU)
                 || (encoding.equals(AUTO) && !autoFound)) {
                try {
                    mailer = (Mailer) ClasspathUtils.newInstance(
                            "org.apache.tools.ant.taskdefs.email.UUMailer",
                            EmailTask.class.getClassLoader(), Mailer.class);
                    autoFound = true;
                    log("Using UU mail", Project.MSG_VERBOSE);
                } catch (BuildException e) {
                    logBuildException("Failed to initialise UU mail: ", e);
                }
            }
            if (encoding.equals(PLAIN)
                 || (encoding.equals(AUTO) && !autoFound)) {
                mailer = new PlainMailer();
                autoFound = true;
                log("Using plain mail", Project.MSG_VERBOSE);
            }
            if (mailer == null) {
                throw new BuildException("Failed to initialise encoding: "
                     + encoding);
            }
            if (message == null) {
                message = new Message();
                message.setProject(getProject());
            }
            if (from == null || from.getAddress() == null) {
                throw new BuildException("A from element is required");
            }
            if (toList.isEmpty() && ccList.isEmpty() && bccList.isEmpty()) {
                throw new BuildException("At least one of to, cc or bcc must "
                     + "be supplied");
            }
            if (messageMimeType != null) {
                if (message.isMimeTypeSpecified()) {
                    throw new BuildException("The mime type can only be "
                         + "specified in one location");
                }
                message.setMimeType(messageMimeType);
            }
            if (charset != null) {
                if (message.getCharset() != null) {
                    throw new BuildException("The charset can only be "
                         + "specified in one location");
                }
                message.setCharset(charset);
            }
            Vector files = new Vector();
            if (attachments != null) {
                Iterator iter = attachments.iterator();
                while (iter.hasNext()) {
                    Resource r = (Resource) iter.next();
                    files.addElement(((FileProvider) r.as(FileProvider.class))
                                     .getFile());
                }
            }
            log("Sending email: " + subject, Project.MSG_INFO);
            log("From " + from, Project.MSG_VERBOSE);
            log("ReplyTo " + replyToList, Project.MSG_VERBOSE);
            log("To " + toList, Project.MSG_VERBOSE);
            log("Cc " + ccList, Project.MSG_VERBOSE);
            log("Bcc " + bccList, Project.MSG_VERBOSE);
            mailer.setHost(host);
            if (port != null) {
                mailer.setPort(port.intValue());
                mailer.setPortExplicitlySpecified(true);
            } else {
                mailer.setPort(SMTP_PORT);
                mailer.setPortExplicitlySpecified(false);
            }
            mailer.setUser(user);
            mailer.setPassword(password);
            mailer.setSSL(ssl);
            mailer.setEnableStartTLS(starttls);
            mailer.setMessage(message);
            mailer.setFrom(from);
            mailer.setReplyToList(replyToList);
            mailer.setToList(toList);
            mailer.setCcList(ccList);
            mailer.setBccList(bccList);
            mailer.setFiles(files);
            mailer.setSubject(subject);
            mailer.setTask(this);
            mailer.setIncludeFileNames(includeFileNames);
            mailer.setHeaders(headers);
            mailer.setIgnoreInvalidRecipients(ignoreInvalidRecipients);
            mailer.send();
            int count = files.size();
            log("Sent email with " + count + " attachment"
                 + (count == 1 ? "" : "s"), Project.MSG_INFO);
        } catch (BuildException e) {
            logBuildException("Failed to send email: ", e);
            if (failOnError) {
                throw e;
            }
        } catch (Exception e) {
          log("Failed to send email: " + e.getMessage(), Project.MSG_WARN);
          if (failOnError) {
            throw new BuildException(e);
          }
        } finally {
            message = savedMessage;
        }
    }
    private void logBuildException(String reason, BuildException e) {
        Throwable t = e.getCause() == null ? e : e.getCause();
        log(reason + t.getMessage(), Project.MSG_WARN);
    }
    public void setCharset(String charset) {
        this.charset = charset;
    }
    public String getCharset() {
        return charset;
    }
}
