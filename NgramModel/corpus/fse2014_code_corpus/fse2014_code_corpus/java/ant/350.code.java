package org.apache.tools.ant.taskdefs.email;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.mail.MailMessage;
class PlainMailer extends Mailer {
    public void send() {
        try {
            MailMessage mailMessage = new MailMessage(host, port);
            mailMessage.from(from.toString());
            Enumeration e;
            boolean atLeastOneRcptReached = false;
            e = replyToList.elements();
            while (e.hasMoreElements()) {
                mailMessage.replyto(e.nextElement().toString());
            }
            e = toList.elements();
            while (e.hasMoreElements()) {
                String to = e.nextElement().toString();
                try {
                    mailMessage.to(to);
                    atLeastOneRcptReached = true;
                } catch (IOException ex) {
                    badRecipient(to, ex);
                }
            }
            e = ccList.elements();
            while (e.hasMoreElements()) {
                String to = e.nextElement().toString();
                try {
                    mailMessage.cc(to);
                    atLeastOneRcptReached = true;
                } catch (IOException ex) {
                    badRecipient(to, ex);
                }
            }
            e = bccList.elements();
            while (e.hasMoreElements()) {
                String to = e.nextElement().toString();
                try {
                    mailMessage.bcc(to);
                    atLeastOneRcptReached = true;
                } catch (IOException ex) {
                    badRecipient(to, ex);
                }
            }
            if (!atLeastOneRcptReached) {
                throw new BuildException("Couldn't reach any recipient");
            }
            if (subject != null) {
                mailMessage.setSubject(subject);
            }
            mailMessage.setHeader("Date", getDate());
            if (message.getCharset() != null) {
                mailMessage.setHeader("Content-Type", message.getMimeType()
                    + "; charset=\"" + message.getCharset() + "\"");
            } else {
                mailMessage.setHeader("Content-Type", message.getMimeType());
            }
            if (headers != null) {
                e = headers.elements();
                while (e.hasMoreElements()) {
                    Header h = (Header) e.nextElement();
                    mailMessage.setHeader(h.getName(), h.getValue());
                }
            }
            PrintStream out = mailMessage.getPrintStream();
            message.print(out);
            e = files.elements();
            while (e.hasMoreElements()) {
                attach((File) e.nextElement(), out);
            }
            mailMessage.sendAndClose();
        } catch (IOException ioe) {
            throw new BuildException("IO error sending mail", ioe);
        }
    }
    protected void attach(File file, PrintStream out)
         throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new BuildException("File \"" + file.getName()
                 + "\" does not exist or is not "
                 + "readable.");
        }
        if (includeFileNames) {
            out.println();
            String filename = file.getName();
            int filenamelength = filename.length();
            out.println(filename);
            for (int star = 0; star < filenamelength; star++) {
                out.print('=');
            }
            out.println();
        }
        int length;
        final int maxBuf = 1024;
        byte[] buf = new byte[maxBuf];
        FileInputStream finstr = new FileInputStream(file);
        try {
            BufferedInputStream in = new BufferedInputStream(finstr, buf.length);
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        } finally {
            finstr.close();
        }
    }
    private void badRecipient(String rcpt, IOException reason) {
        String msg = "Failed to send mail to " + rcpt;
        if (shouldIgnoreInvalidRecipients()) {
            msg += " because of :" + reason.getMessage();
            if (task != null) {
                task.log(msg, Project.MSG_WARN);
            } else {
                System.err.println(msg);
            }
        } else {
            throw new BuildException(msg, reason);
        }
    }
}
