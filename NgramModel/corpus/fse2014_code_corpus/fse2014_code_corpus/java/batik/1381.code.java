package org.apache.batik.util;
import java.awt.EventQueue;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
public class EventDispatcher {
    public interface Dispatcher {
        void dispatch(Object listener,
                             Object event);
    }
    public static void fireEvent(final Dispatcher dispatcher,
                                 final List listeners,
                                 final Object evt,
                                 final boolean useEventQueue) {
        if (useEventQueue && !EventQueue.isDispatchThread()) {
            Runnable r = new Runnable() {
                    public void run() {
                        fireEvent(dispatcher, listeners, evt, useEventQueue);
                    }
                };
            try {
                EventQueue.invokeAndWait(r);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return;
        }
        Object [] ll = null;
        Throwable err = null;
        int retryCount = 10;
        while (--retryCount != 0) {
            try {
                synchronized (listeners) {
                    if (listeners.size() == 0)
                        return;
                    ll = listeners.toArray();
                    break;
                }
            } catch(Throwable t) {
                err = t;
            }
        }
        if (ll == null) {
            if (err != null)
                err.printStackTrace();
            return;
        }
        dispatchEvent(dispatcher, ll, evt);
    }
    protected static void dispatchEvent(final Dispatcher dispatcher,
                                        final Object [] ll,
                                        final Object evt) {
        ThreadDeath td = null;
        try {
            for (int i = 0; i < ll.length; i++) {
                try {
                    Object l;
                    synchronized (ll) {
                        l = ll[i];
                        if (l == null) continue;
                        ll[i] = null;
                    }
                    dispatcher.dispatch(l, evt);
                } catch (ThreadDeath t) {
                    td = t;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (ThreadDeath t) {
            td = t;
        } catch (Throwable t) {
            if (ll[ll.length-1] != null)
                dispatchEvent(dispatcher, ll, evt);
            t.printStackTrace();
        }
        if (td != null) throw td;
    }
}
