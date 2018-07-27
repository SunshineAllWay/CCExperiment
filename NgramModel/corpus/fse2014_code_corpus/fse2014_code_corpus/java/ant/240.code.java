package org.apache.tools.ant.taskdefs;
import java.util.Enumeration;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.property.LocalProperties;
import org.apache.tools.ant.util.StringUtils;
public class Parallel extends Task
                      implements TaskContainer {
    private static final int NUMBER_TRIES = 100;
    public static class TaskList implements TaskContainer {
        private List tasks = new ArrayList();
        public void addTask(Task nestedTask) {
            tasks.add(nestedTask);
        }
    }
    private Vector nestedTasks = new Vector();
    private final Object semaphore = new Object();
    private int numThreads = 0;
    private int numThreadsPerProcessor = 0;
    private long timeout;
    private volatile boolean stillRunning;
    private boolean timedOut;
    private boolean failOnAny;
    private TaskList daemonTasks;
    private StringBuffer exceptionMessage;
    private int numExceptions = 0;
    private Throwable firstException;
    private Location firstLocation;
    public void addDaemons(TaskList daemonTasks) {
        if (this.daemonTasks != null) {
            throw new BuildException("Only one daemon group is supported");
        }
        this.daemonTasks = daemonTasks;
    }
    public void setPollInterval(int pollInterval) {
    }
    public void setFailOnAny(boolean failOnAny) {
        this.failOnAny = failOnAny;
    }
    public void addTask(Task nestedTask) {
        nestedTasks.addElement(nestedTask);
    }
    public void setThreadsPerProcessor(int numThreadsPerProcessor) {
        this.numThreadsPerProcessor = numThreadsPerProcessor;
    }
    public void setThreadCount(int numThreads) {
        this.numThreads = numThreads;
    }
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    public void execute() throws BuildException {
        updateThreadCounts();
        if (numThreads == 0) {
            numThreads = nestedTasks.size();
        }
        spinThreads();
    }
    private void updateThreadCounts() {
        if (numThreadsPerProcessor != 0) {
            numThreads = Runtime.getRuntime().availableProcessors() *
                    numThreadsPerProcessor;
        }
    }
    private void processExceptions(TaskRunnable[] runnables) {
        if (runnables == null) {
            return;
        }
        for (int i = 0; i < runnables.length; ++i) {
            Throwable t = runnables[i].getException();
            if (t != null) {
                numExceptions++;
                if (firstException == null) {
                    firstException = t;
                }
                if (t instanceof BuildException
                    && firstLocation == Location.UNKNOWN_LOCATION) {
                    firstLocation = ((BuildException) t).getLocation();
                }
                exceptionMessage.append(StringUtils.LINE_SEP);
                exceptionMessage.append(t.getMessage());
            }
        }
    }
    private void spinThreads() throws BuildException {
        final int numTasks = nestedTasks.size();
        TaskRunnable[] runnables = new TaskRunnable[numTasks];
        stillRunning = true;
        timedOut = false;
        boolean interrupted = false;
        int threadNumber = 0;
        for (Enumeration e = nestedTasks.elements(); e.hasMoreElements();
             threadNumber++) {
            Task nestedTask = (Task) e.nextElement();
            runnables[threadNumber]
                = new TaskRunnable(nestedTask);
        }
        final int maxRunning = numTasks < numThreads ? numTasks : numThreads;
        TaskRunnable[] running = new TaskRunnable[maxRunning];
        threadNumber = 0;
        ThreadGroup group = new ThreadGroup("parallel");
        TaskRunnable[] daemons = null;
        if (daemonTasks != null && daemonTasks.tasks.size() != 0) {
            daemons = new TaskRunnable[daemonTasks.tasks.size()];
        }
        synchronized (semaphore) {
        }
        synchronized (semaphore) {
            if (daemons != null) {
                for (int i = 0; i < daemons.length; ++i) {
                    daemons[i] = new TaskRunnable((Task) daemonTasks.tasks.get(i));
                    Thread daemonThread =  new Thread(group, daemons[i]);
                    daemonThread.setDaemon(true);
                    daemonThread.start();
                }
            }
            for (int i = 0; i < maxRunning; ++i) {
                running[i] = runnables[threadNumber++];
                Thread thread =  new Thread(group, running[i]);
                thread.start();
            }
            if (timeout != 0) {
                Thread timeoutThread = new Thread() {
                    public synchronized void run() {
                        try {
                            wait(timeout);
                            synchronized (semaphore) {
                                stillRunning = false;
                                timedOut = true;
                                semaphore.notifyAll();
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                };
                timeoutThread.start();
            }
            try {
                outer: while (threadNumber < numTasks && stillRunning) {
                    for (int i = 0; i < maxRunning; i++) {
                        if (running[i] == null || running[i].isFinished()) {
                            running[i] = runnables[threadNumber++];
                            Thread thread = new Thread(group, running[i]);
                            thread.start();
                            continue outer;
                        }
                    }
                    semaphore.wait();
                }
                outer2: while (stillRunning) {
                    for (int i = 0; i < maxRunning; ++i) {
                        if (running[i] != null && !running[i].isFinished()) {
                            semaphore.wait();
                            continue outer2;
                        }
                    }
                    stillRunning = false;
                }
            } catch (InterruptedException ie) {
                interrupted = true;
            }
            if (!timedOut && !failOnAny) {
                killAll(running);
            }
        }
        if (interrupted) {
            throw new BuildException("Parallel execution interrupted.");
        }
        if (timedOut) {
            throw new BuildException("Parallel execution timed out");
        }
        exceptionMessage = new StringBuffer();
        numExceptions = 0;
        firstException = null;
        firstLocation = Location.UNKNOWN_LOCATION;
        processExceptions(daemons);
        processExceptions(runnables);
        if (numExceptions == 1) {
            if (firstException instanceof BuildException) {
                throw (BuildException) firstException;
            } else {
                throw new BuildException(firstException);
            }
        } else if (numExceptions > 1) {
            throw new BuildException(exceptionMessage.toString(),
                                     firstLocation);
        }
    }
    private void killAll(TaskRunnable[] running) {
        boolean oneAlive;
        int tries = 0;
        do {
            oneAlive = false;
            for (int i = 0; i < running.length; i++) {
                if (running[i] != null && !running[i].isFinished()) {
                    running[i].interrupt();
                    Thread.yield();
                    oneAlive = true;
                }
            }
            if (oneAlive) {
                tries++;
                Thread.yield();
            }
        } while (oneAlive && tries < NUMBER_TRIES);
    }
    private class TaskRunnable implements Runnable {
        private Throwable exception;
        private Task task;
        private boolean finished;
        private volatile Thread thread;
        TaskRunnable(Task task) {
            this.task = task;
        }
        public void run() {
            try {
                LocalProperties.get(getProject()).copy();
                thread = Thread.currentThread();
                task.perform();
            } catch (Throwable t) {
                exception = t;
                if (failOnAny) {
                    stillRunning = false;
                }
            } finally {
                synchronized (semaphore) {
                    finished = true;
                    semaphore.notifyAll();
                }
            }
        }
        public Throwable getException() {
            return exception;
        }
        boolean isFinished() {
            return finished;
        }
        void interrupt() {
            thread.interrupt();
        }
    }
}
