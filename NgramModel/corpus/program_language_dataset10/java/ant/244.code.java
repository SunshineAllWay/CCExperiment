package org.apache.tools.ant.taskdefs;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
class ProcessDestroyer implements Runnable {
    private static final int THREAD_DIE_TIMEOUT = 20000;
    private HashSet processes = new HashSet();
    private Method addShutdownHookMethod;
    private Method removeShutdownHookMethod;
    private ProcessDestroyerImpl destroyProcessThread = null;
    private boolean added = false;
    private boolean running = false;
    private class ProcessDestroyerImpl extends Thread {
        private boolean shouldDestroy = true;
        public ProcessDestroyerImpl() {
            super("ProcessDestroyer Shutdown Hook");
        }
        public void run() {
            if (shouldDestroy) {
                ProcessDestroyer.this.run();
            }
        }
        public void setShouldDestroy(boolean shouldDestroy) {
            this.shouldDestroy = shouldDestroy;
        }
    }
    ProcessDestroyer() {
        try {
            Class[] paramTypes = {Thread.class};
            addShutdownHookMethod =
                Runtime.class.getMethod("addShutdownHook", paramTypes);
            removeShutdownHookMethod =
                Runtime.class.getMethod("removeShutdownHook", paramTypes);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addShutdownHook() {
        if (addShutdownHookMethod != null && !running) {
            destroyProcessThread = new ProcessDestroyerImpl();
            Object[] args = {destroyProcessThread};
            try {
                addShutdownHookMethod.invoke(Runtime.getRuntime(), args);
                added = true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t != null && t.getClass() == IllegalStateException.class) {
                    running = true;
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
    private void removeShutdownHook() {
        if (removeShutdownHookMethod != null && added && !running) {
            Object[] args = {destroyProcessThread};
            try {
                Boolean removed =
                    (Boolean) removeShutdownHookMethod.invoke(
                        Runtime.getRuntime(),
                        args);
                if (!removed.booleanValue()) {
                    System.err.println("Could not remove shutdown hook");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t != null && t.getClass() == IllegalStateException.class) {
                    running = true;
                } else {
                    e.printStackTrace();
                }
            }
            destroyProcessThread.setShouldDestroy(false);
            if (!destroyProcessThread.getThreadGroup().isDestroyed()) {
                destroyProcessThread.start();
            }
            try {
                destroyProcessThread.join(THREAD_DIE_TIMEOUT);
            } catch (InterruptedException ie) {
            }
            destroyProcessThread = null;
            added = false;
        }
    }
    public boolean isAddedAsShutdownHook() {
        return added;
    }
    public boolean add(Process process) {
        synchronized (processes) {
            if (processes.size() == 0) {
                addShutdownHook();
            }
            return processes.add(process);
        }
    }
    public boolean remove(Process process) {
        synchronized (processes) {
            boolean processRemoved = processes.remove(process);
            if (processRemoved && processes.size() == 0) {
                removeShutdownHook();
            }
            return processRemoved;
        }
    }
    public void run() {
        synchronized (processes) {
            running = true;
            Iterator e = processes.iterator();
            while (e.hasNext()) {
                ((Process) e.next()).destroy();
            }
        }
    }
}
