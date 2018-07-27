package org.apache.tools.ant;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Enumeration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
public class XmlLogger implements BuildLogger {
    private int msgOutputLevel = Project.MSG_DEBUG;
    private PrintStream outStream;
    private static DocumentBuilder builder = getDocumentBuilder();
    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }
    private static final String BUILD_TAG = "build";
    private static final String TARGET_TAG = "target";
    private static final String TASK_TAG = "task";
    private static final String MESSAGE_TAG = "message";
    private static final String NAME_ATTR = "name";
    private static final String TIME_ATTR = "time";
    private static final String PRIORITY_ATTR = "priority";
    private static final String LOCATION_ATTR = "location";
    private static final String ERROR_ATTR = "error";
    private static final String STACKTRACE_TAG = "stacktrace";
    private Document doc = builder.newDocument();
    private Hashtable tasks = new Hashtable();
    private Hashtable targets = new Hashtable();
    private Hashtable threadStacks = new Hashtable();
    private TimedElement buildElement = null;
    private static class TimedElement {
        private long startTime;
        private Element element;
        public String toString() {
            return element.getTagName() + ":" + element.getAttribute("name");
        }
    }
    public XmlLogger() {
    }
    public void buildStarted(BuildEvent event) {
        buildElement = new TimedElement();
        buildElement.startTime = System.currentTimeMillis();
        buildElement.element = doc.createElement(BUILD_TAG);
    }
    public void buildFinished(BuildEvent event) {
        long totalTime = System.currentTimeMillis() - buildElement.startTime;
        buildElement.element.setAttribute(TIME_ATTR, DefaultLogger.formatTime(totalTime));
        if (event.getException() != null) {
            buildElement.element.setAttribute(ERROR_ATTR, event.getException().toString());
            Throwable t = event.getException();
            Text errText = doc.createCDATASection(StringUtils.getStackTrace(t));
            Element stacktrace = doc.createElement(STACKTRACE_TAG);
            stacktrace.appendChild(errText);
            synchronizedAppend(buildElement.element, stacktrace);
        }
        String outFilename = event.getProject().getProperty("XmlLogger.file");
        if (outFilename == null) {
            outFilename = "log.xml";
        }
        String xslUri = event.getProject().getProperty("ant.XmlLogger.stylesheet.uri");
        if (xslUri == null) {
            xslUri = "log.xsl";
        }
        Writer out = null;
        try {
            OutputStream stream = outStream;
            if (stream == null) {
                stream = new FileOutputStream(outFilename);
            }
            out = new OutputStreamWriter(stream, "UTF8");
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            if (xslUri.length() > 0) {
                out.write("<?xml-stylesheet type=\"text/xsl\" href=\"" + xslUri + "\"?>\n\n");
            }
            new DOMElementWriter().write(buildElement.element, out, 0, "\t");
            out.flush();
        } catch (IOException exc) {
            throw new BuildException("Unable to write log file", exc);
        } finally {
            FileUtils.close(out);
        }
        buildElement = null;
    }
    private Stack getStack() {
        Stack threadStack = (Stack) threadStacks.get(Thread.currentThread());
        if (threadStack == null) {
            threadStack = new Stack();
            threadStacks.put(Thread.currentThread(), threadStack);
        }
        return threadStack;
    }
    public void targetStarted(BuildEvent event) {
        Target target = event.getTarget();
        TimedElement targetElement = new TimedElement();
        targetElement.startTime = System.currentTimeMillis();
        targetElement.element = doc.createElement(TARGET_TAG);
        targetElement.element.setAttribute(NAME_ATTR, target.getName());
        targets.put(target, targetElement);
        getStack().push(targetElement);
    }
    public void targetFinished(BuildEvent event) {
        Target target = event.getTarget();
        TimedElement targetElement = (TimedElement) targets.get(target);
        if (targetElement != null) {
            long totalTime = System.currentTimeMillis() - targetElement.startTime;
            targetElement.element.setAttribute(TIME_ATTR, DefaultLogger.formatTime(totalTime));
            TimedElement parentElement = null;
            Stack threadStack = getStack();
            if (!threadStack.empty()) {
                TimedElement poppedStack = (TimedElement) threadStack.pop();
                if (poppedStack != targetElement) {
                    throw new RuntimeException("Mismatch - popped element = " + poppedStack
                            + " finished target element = " + targetElement);
                }
                if (!threadStack.empty()) {
                    parentElement = (TimedElement) threadStack.peek();
                }
            }
            if (parentElement == null) {
                synchronizedAppend(buildElement.element, targetElement.element);
            } else {
                synchronizedAppend(parentElement.element,
                                   targetElement.element);
            }
        }
        targets.remove(target);
    }
    public void taskStarted(BuildEvent event) {
        TimedElement taskElement = new TimedElement();
        taskElement.startTime = System.currentTimeMillis();
        taskElement.element = doc.createElement(TASK_TAG);
        Task task = event.getTask();
        String name = event.getTask().getTaskName();
        if (name == null) {
            name = "";
        }
        taskElement.element.setAttribute(NAME_ATTR, name);
        taskElement.element.setAttribute(LOCATION_ATTR, event.getTask().getLocation().toString());
        tasks.put(task, taskElement);
        getStack().push(taskElement);
    }
    public void taskFinished(BuildEvent event) {
        Task task = event.getTask();
        TimedElement taskElement = (TimedElement) tasks.get(task);
        if (taskElement == null) {
            throw new RuntimeException("Unknown task " + task + " not in " + tasks);
        }
        long totalTime = System.currentTimeMillis() - taskElement.startTime;
        taskElement.element.setAttribute(TIME_ATTR, DefaultLogger.formatTime(totalTime));
        Target target = task.getOwningTarget();
        TimedElement targetElement = null;
        if (target != null) {
            targetElement = (TimedElement) targets.get(target);
        }
        if (targetElement == null) {
            synchronizedAppend(buildElement.element, taskElement.element);
        } else {
            synchronizedAppend(targetElement.element, taskElement.element);
        }
        Stack threadStack = getStack();
        if (!threadStack.empty()) {
            TimedElement poppedStack = (TimedElement) threadStack.pop();
            if (poppedStack != taskElement) {
                throw new RuntimeException("Mismatch - popped element = " + poppedStack
                        + " finished task element = " + taskElement);
            }
        }
        tasks.remove(task);
    }
    private TimedElement getTaskElement(Task task) {
        TimedElement element = (TimedElement) tasks.get(task);
        if (element != null) {
            return element;
        }
        for (Enumeration e = tasks.keys(); e.hasMoreElements();) {
            Task key = (Task) e.nextElement();
            if (key instanceof UnknownElement) {
                if (((UnknownElement) key).getTask() == task) {
                    return (TimedElement) tasks.get(key);
                }
            }
        }
        return null;
    }
    public void messageLogged(BuildEvent event) {
        int priority = event.getPriority();
        if (priority > msgOutputLevel) {
            return;
        }
        Element messageElement = doc.createElement(MESSAGE_TAG);
        String name = "debug";
        switch (priority) {
            case Project.MSG_ERR:
                name = "error";
                break;
            case Project.MSG_WARN:
                name = "warn";
                break;
            case Project.MSG_INFO:
                name = "info";
                break;
            default:
                name = "debug";
                break;
        }
        messageElement.setAttribute(PRIORITY_ATTR, name);
        Throwable ex = event.getException();
        if (Project.MSG_DEBUG <= msgOutputLevel && ex != null) {
            Text errText = doc.createCDATASection(StringUtils.getStackTrace(ex));
            Element stacktrace = doc.createElement(STACKTRACE_TAG);
            stacktrace.appendChild(errText);
            synchronizedAppend(buildElement.element, stacktrace);
        }
        Text messageText = doc.createCDATASection(event.getMessage());
        messageElement.appendChild(messageText);
        TimedElement parentElement = null;
        Task task = event.getTask();
        Target target = event.getTarget();
        if (task != null) {
            parentElement = getTaskElement(task);
        }
        if (parentElement == null && target != null) {
            parentElement = (TimedElement) targets.get(target);
        }
        if (parentElement != null) {
            synchronizedAppend(parentElement.element, messageElement);
        } else {
            synchronizedAppend(buildElement.element, messageElement);
        }
    }
    public void setMessageOutputLevel(int level) {
        msgOutputLevel = level;
    }
    public void setOutputPrintStream(PrintStream output) {
        this.outStream = new PrintStream(output, true);
    }
    public void setEmacsMode(boolean emacsMode) {
    }
    public void setErrorPrintStream(PrintStream err) {
    }
    private void synchronizedAppend(Node parent, Node child) {
        synchronized(parent) {
            parent.appendChild(child);
        }
    }
}
